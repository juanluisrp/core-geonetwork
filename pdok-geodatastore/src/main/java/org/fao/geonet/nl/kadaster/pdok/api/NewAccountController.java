package org.fao.geonet.nl.kadaster.pdok.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import nl.kadaster.pdok.bussiness.MailUtils;
import nl.kadaster.pdok.bussiness.RegisterBean;
import nl.kadaster.pdok.bussiness.ValidationResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by juanluisrp on 04/11/2015.
 */
@Controller
public class NewAccountController {
    private static final String GDS_LOG = "geodatastore.register";
    private static final String REGISTRATION_EMAIL_XSLT = "/templates/registration-email-transform.xsl";
    @Autowired private ServiceManager serviceManager;
    @Autowired private MailUtils mailUtils;
    @Autowired private SettingManager settingManager;
    @Autowired private Validator validator;
    @Value("#{geodatastoreProperties[registrationEmailAddress]}")
    private String registrationEmailAddress;

    @RequestMapping(value = "/{lang}/gdsRegister", method = RequestMethod.POST)
    public @ResponseBody
    ValidationResponse register(@PathVariable("lang") String lang,
                                @Valid @RequestPart("registerBean") RegisterBean registerBean, BindingResult bindingResult,
                                @RequestPart("logo") MultipartFile logo,
                                HttpServletRequest request) {


        ValidationResponse response = new ValidationResponse();
        response.setStatus("SUCCESS");
        ServiceContext context = serviceManager.createServiceContext("geodatastore.register", lang, request);
        try {
            List<String> fieldsWithErrors = new ArrayList<>();
            if (logo.isEmpty()) {
                fieldsWithErrors.add("logo");
            }
            for (FieldError error : bindingResult.getFieldErrors()) {
                fieldsWithErrors.add(error.getField());
            }
            if (!fieldsWithErrors.isEmpty()) {
                response.setStatus("ERROR");
                response.setErrorMessageList(fieldsWithErrors);
            } else {
                if (!sendEmail(registerBean, logo)) {
                    response.setStatus("ERROR");
                    response.setGlobalError("emailNotSent");
                }
            }
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setGlobalError(e.getClass().getCanonicalName());
        }




        return response;
    }

    private boolean sendEmail(RegisterBean registerBean, MultipartFile logo) throws IOException {
        Map<String, String> mailTemplateParameters = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        Map<String, Object> objectAsMap = objectMapper.convertValue(registerBean, Map.class);
        for (Map.Entry<String, Object> entry : objectAsMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                mailTemplateParameters.put(key, (String) value);
            }
        }

        List<Path> attachmentList = new ArrayList<>();
        boolean sent = false;
        Path logoDir = Files.createTempDirectory("logo");
        String uploadedFileName = logo.getOriginalFilename();
        String extension = "";
        if (uploadedFileName.lastIndexOf('.') > -1) {
            extension = StringUtils.substring(uploadedFileName, uploadedFileName.lastIndexOf('.'));
        }
        Path logoPath = Files.createFile(logoDir.resolve("logo" + extension));
        File logoFile = logoPath.toFile();
        try {
            logo.transferTo(logoFile);
            attachmentList.add(logoPath);
            sent = mailUtils.sendHtmlEmailWithAttachments(registrationEmailAddress, mailTemplateParameters, attachmentList, REGISTRATION_EMAIL_XSLT);
        } catch (Exception e) {
            Log.error(GDS_LOG, "Error sending registration email", e);
            sent = false;
        }
        finally {
            FileUtils.deleteQuietly(logoDir.toFile());
        }
        if (!sent) {
            Log.error(GDS_LOG, "The registration email cannot be sent. Please review the mail server settings in the database");
        }
        return sent;
    }
}
