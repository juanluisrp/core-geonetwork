package org.fao.geonet.nl.kadaster.pdok.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Value("#{geodatastoreProperties[registrationEmailAddress]}")
    private String registrationEmailAddress;

    @RequestMapping("/{lang}/gdsRegister")
    public @ResponseBody
    ValidationResponse register(@PathVariable("lang") String lang, HttpServletRequest request, @RequestParam("logo") MultipartFile logo,
                                @Valid RegisterBean registerBean, BindingResult bindingResult) {
        ValidationResponse response = new ValidationResponse();
        response.setStatus("SUCCESS");
        ServiceContext context = serviceManager.createServiceContext("geodatastore.register", lang, request);
        try {
            if (logo.isEmpty()) {
                bindingResult.addError(new FieldError("registerBean", "logo", "notNull"));
            }
            if (bindingResult.getErrorCount() > 0) {
                response.setStatus("ERROR");
                List<String> fieldsWithErrors = new ArrayList<>();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    fieldsWithErrors.add(error.getField());
                }
                response.setErrorMessageList(fieldsWithErrors);
            } else {
                if (!sendEmail(registerBean, logo)) {
                    response.setStatus("ERROR");
                }
            }
        } catch (Exception e) {
            response.setStatus("ERROR");
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
