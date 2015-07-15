package org.fao.geonet.nl.kadaster.pdok.api;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.http.ServletPathFinder;
import jeeves.services.ReadWriteController;
import nl.kadaster.pdok.bussiness.MetadataParametersBean;
import nl.kadaster.pdok.bussiness.MetadataUtil;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.ServiceNotAllowedEx;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.services.metadata.XslProcessing;
import org.fao.geonet.services.metadata.XslProcessingReport;
import org.fao.geonet.services.resources.handlers.IResourceUploadHandler;
import org.fao.geonet.services.schema.Info;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasProfile;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;

/**
 * Created by JuanLuis on 30/06/2015.
 */
@Controller
@ReadWriteController
@RequestMapping("/{lang}/geodatastore")
public class GeodatastoreApi {
    private static final String QUERY_SERVICE= "q";
    private static final String ISO_19139 = "iso19139";
    public static final String TITLE_KEY = "title";
    public static final String LINEAGE_KEY = "lineage";
    public static final String RESOLUTION_KEY = "resolution";
    public static final String ABSTRACT_KEY = "abstract";
    public static final String USER_LIMITATION_KEY = "userLimitation";
    public static final String FORMAT_KEY = "format";

    @Autowired
    private MetadataUtil metadataUtil;
    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    private DataManager metadataManager;

    @Autowired
    private UserGroupRepository userGroupRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private SettingManager settingManager;
    @Autowired
    private XslProcessing xslProcessing;
    @Autowired
    ServletContext servletContext;



    public GeodatastoreApi() {
        System.out.println("New GeodatastoreApi instance");

    }


    @RequestMapping(value = "/api/dataset", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<Object> uploadDataset(@RequestParam("files[]") MultipartFile file,
                                                              @PathVariable("lang") String lang, HttpServletRequest request,
                                                              Model model) throws Exception {
        if (!file.isEmpty()) {
            MetadataParametersBean response = new MetadataParametersBean();
            HttpStatus status = HttpStatus.OK;

            try {
                ServiceContext context = serviceManager.createServiceContext("geodatastore.api.dataset", lang, request);
                UserSession session = context.getUserSession();
                final String username = session.getUsername();
                assert username != null;
                String organisation = session.getOrganisation();
                User user = userRepository.findOneByUsername(username);
                List<Integer> groupsIds = userGroupRepository.findGroupIds(Specifications.where(
                        hasProfile(Profile.Editor)).and(hasUserId(user.getId())));
                Group group = null;
                if (groupsIds.size() != 0) {
                    Collections.sort(groupsIds);
                    group = groupRepository.findOne(groupsIds.get(0));
                } else {
                    throw new UnAuthorizedException("No Editor group found for user " + username, groupsIds);
                }

                if (StringUtils.isBlank(group.getEmail())) {
                    throw new ServiceNotAllowedEx("The group " + group.getName() + " must have an email set");
                }
                String organisationEmail = group.getEmail();
                UUID uuid = UUID.randomUUID();
                ISODate creationDate = new ISODate();
                Map<String, Object> templateParameters = prepareTemplateParameters(organisation, organisationEmail,
                        new ArrayList<String>(), new ArrayList<String>(), "Nederland", "2", "5", "50", "54",
                        file.getContentType(), "http://example.com/geonetwork/id/dataset/" + uuid.toString(), file.getOriginalFilename(),
                        uuid.toString(), creationDate);

                Element metadata = metadataUtil.fillXmlTemplate(templateParameters);

                int userId = context.getUserSession().getUserIdAsInt();
                String docType = null, category = "geodatastore";
                boolean updateFixedInfo = true, indexImmediate = true;
                String createdId = metadataManager.insertMetadata(context, ISO19139SchemaPlugin.IDENTIFIER, metadata, uuid.toString(),
                        userId, Integer.toString(group.getId()), settingManager.getSiteId(), MetadataType.METADATA.codeString,
                        docType, category, creationDate.toString(), creationDate.toString(), updateFixedInfo, indexImmediate);


                String fileName = file.getOriginalFilename();
                String fsize = Long.toString(file.getSize());
                String access = "private";
                String overwrite = "no";

                IResourceUploadHandler uploadHook = (IResourceUploadHandler) context.getApplicationContext().getBean("resourceUploadHandler");
                uploadHook.onUpload(file.getInputStream(), context, access, overwrite, Integer.parseInt(createdId), fileName, Double.parseDouble(fsize));


                context.info("UPLOADED:" + fileName + "," + createdId + "," + context.getIpAddress() + "," + username);

                ServletPathFinder pathFinder = new ServletPathFinder(servletContext);
                String downloadUrl = getSiteURL(pathFinder) + "/id/dataset/" + uuid.toString()+ "/" + fileName;
                Map<String, String[]> allParams = Maps.newHashMap(request.getParameterMap());
                // Set parameter and process metadata to reference the uploaded file
                allParams.put("url", new String[]{downloadUrl});
                allParams.put("name", new String[]{file.getOriginalFilename()});
                allParams.put("desc", new String[]{"Geodatastore uploaded file"});
                allParams.put("protocol", new String[]{"download"});

                String process = "onlinesrc-add";
                XslProcessingReport report = new XslProcessingReport(process);

                Element processedMetadata;
                try {
                    final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
                    processedMetadata = xslProcessing.process(context, createdId, process,
                            true, report, siteURL, allParams);
                    if (processedMetadata == null) {
                        throw new BadParameterEx("Processing failed", "Not found:"
                                + report.getNotFoundMetadataCount() + ", Not owner:" + report.getNotEditableMetadataCount()
                                + ", No process found:" + report.getNoProcessFoundCount() + ".");
                    }
                } catch (Exception e) {
                    throw e;
                }

                // Build response
                response.setIdentifier(uuid.toString());
                response.setError(false);
                //response.setExtent();
                response.setSummary((String) templateParameters.get(ABSTRACT_KEY));
                response.setLineage((String) templateParameters.get(LINEAGE_KEY));
                //response.setLicense();
                //response.setLocation();
                response.setResolution((String) templateParameters.get(RESOLUTION_KEY));
                //response.setStatus();
                response.setTitle((String) templateParameters.get(TITLE_KEY));
                response.setUrl(downloadUrl);
                response.setUseLimitation((String) templateParameters.get(USER_LIMITATION_KEY));
                response.setFileType((String) templateParameters.get(FORMAT_KEY));
            } catch (UnAuthorizedException e) {
                response.setError(true);
                response.addMessage(e.getMessage());
                status = HttpStatus.FORBIDDEN;
            } catch (ServiceNotAllowedEx e) {
                response.setError(true);
                response.addMessage(e.getMessage());
                status = HttpStatus.FORBIDDEN;
            } catch (BadParameterEx e) {
                response.setError(true);
                response.addMessage(e.getMessage() + ". " + e.getObject());
                status = HttpStatus.BAD_REQUEST;
            } catch (Exception e) {
                response.setError(true);
                response.addMessage(e.getMessage());
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            return new ResponseEntity<Object>(response, status);
        } else {
            MetadataParametersBean response = new MetadataParametersBean();
            response.setError(true);
            response.addMessage("Upload failed because the file was empty");
            return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
        }

    }

    private Map<String, Object> prepareTemplateParameters(String organisation, String organisationEmail, List<String> keywords,
                                                          List<String> topics, String geograpicIdentifier, String bboxWestLongitude,
                                                          String bboxEastLongitude, String bboxSouthLatitude, String bboxNorthLatitude,
                                                          String format, String downloadUri, String fileName, String uuid, ISODate creationDate) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("organisationName", organisation);
        parameters.put("organisationEmail", organisationEmail);


        parameters.put("metadataModifiedDate", creationDate.toString());
        parameters.put(LINEAGE_KEY, "");
        parameters.put(TITLE_KEY, fileName);
        parameters.put("publicationDate", creationDate.toString());

        parameters.put("uuid", uuid);
        parameters.put(ABSTRACT_KEY, "");
        parameters.put("thumbnailUri", "");

        String keyworkdList = Joiner.on("#").join(keywords);
        parameters.put("keywords", keyworkdList);
        parameters.put("keywordSeparator", "#");
        parameters.put(USER_LIMITATION_KEY, "None");
        parameters.put(RESOLUTION_KEY, "10000");

        String topicList = Joiner.on("#").join(topics);
        parameters.put("topics", topicList);
        parameters.put("topicSeparator", "#");
        parameters.put("geographicIdentifier", geograpicIdentifier);
        parameters.put("bboxWestLongitude", bboxWestLongitude);
        parameters.put("bboxEastLongitude", bboxEastLongitude);
        parameters.put("bboxSouthLatitude", bboxSouthLatitude);
        parameters.put("bboxNorthLatitude", bboxNorthLatitude);

        parameters.put(FORMAT_KEY, format);
        parameters.put("downloadUri", downloadUri);
        parameters.put("fileName", fileName);


        return parameters;
    }

    /**
     *
     * @param identifier
     * @param model
     * @return
     */
    @RequestMapping(value = "/api/dataset/{identifier}", method = RequestMethod.PUT)
    public @ResponseBody MetadataResponseBean updateDataset(@PathVariable("identifier") String identifier, Model model) {
        MetadataResponseBean response = new MetadataResponseBean();
        response.setIdentifier(identifier);
        return response;
    }

    @RequestMapping(value = "/api/dataset/{identifier}", method = RequestMethod.DELETE)
    public @ResponseBody MetadataResponseBean deleteDataset(@PathVariable("identifier") String identifier, Model model) {
        MetadataResponseBean response = new MetadataResponseBean();
        response.setIdentifier(identifier);
        return response;
    }

    @RequestMapping(value="/registry")
    public List<String> getAvailableCodelists() {
        List<String> result = new ArrayList<String>();
        result.add("gmd:MD_TopicCategoryCode");
        result.add("gmd:otherConstraints");
        return result;

    }

    @RequestMapping(value = "/registry/{codeList}")
    public ResponseEntity<String> getCodelistEntries(@PathVariable("lang") String lang, @PathVariable("codeList") String codeList,
                                             HttpServletRequest request, @RequestHeader(org.apache.http.HttpHeaders.ACCEPT) String accept) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);
        HttpHeaders responseHeaders = new HttpHeaders();

        final ServiceContext context = serviceManager.createServiceContext("xml.schema.info", lang, request);
        Info info = new Info();
        Element parameters = new Element("request");
        Element codelist = new Element("codelist");
        codelist.setAttribute("schema", ISO_19139);
        codelist.setAttribute("name", codeList);
        parameters.addContent(codelist.detach());
        Element responseXML =  info.exec(parameters, context);
        if (accept != null) {
            if (accept.toLowerCase().contains(MediaType.APPLICATION_XML_VALUE)) {
                responseHeaders.setContentType(new MediaType("application", "xml"));

                return new ResponseEntity<String>(Xml.getString(responseXML), responseHeaders, HttpStatus.OK);
            } else  {
                responseHeaders.setContentType(new MediaType("application", "json"));
                return new ResponseEntity<String>(Xml.getJSON(responseXML), responseHeaders, HttpStatus.OK);
            }
        } else {
            responseHeaders.setContentType(new MediaType("application", "json"));
            return new ResponseEntity<String>(Xml.getJSON(responseXML), responseHeaders, HttpStatus.OK);
        }
    }

   /**
     * Return complete site URL including language
     * eg. http://localhost:8080/geonetwork/srv/eng
     *
     * @return
     */
    private @Nonnull String getSiteURL(ServletPathFinder servletPathFinder) {
        String baseURL = servletPathFinder.getBaseUrl();
        String protocol = settingManager.getValue(Geonet.Settings.SERVER_PROTOCOL);
        String host    = settingManager.getValue(Geonet.Settings.SERVER_HOST);
        String port    = settingManager.getValue(Geonet.Settings.SERVER_PORT);

        return protocol + "://" + host + (port.equals("80") ? "" : ":" + port) + baseURL;
    }




}
