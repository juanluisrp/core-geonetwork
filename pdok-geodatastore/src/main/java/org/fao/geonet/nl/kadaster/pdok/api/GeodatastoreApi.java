package org.fao.geonet.nl.kadaster.pdok.api;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.http.ServletPathFinder;
import jeeves.services.ReadWriteController;
import nl.kadaster.pdok.bussiness.*;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.*;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.nl.kadaster.pdok.api.converter.StringToMetadataParameterBeanConverter;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.services.metadata.Publish;
import org.fao.geonet.services.metadata.XslProcessing;
import org.fao.geonet.services.metadata.XslProcessingReport;
import org.fao.geonet.services.resources.handlers.IResourceUploadHandler;
import org.fao.geonet.services.schema.Info;
import org.fao.geonet.services.util.SearchDefaults;
import org.fao.geonet.util.MailUtil;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasProfile;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;

/**
 * Created by JuanLuis on 30/06/2015.
 */
@Controller
@ReadWriteController
@RequestMapping("/{lang}/api/v1")
public class GeodatastoreApi  {
    public static final String TITLE_KEY = "title";
    public static final String LINEAGE_KEY = "lineage";
    public static final String RESOLUTION_KEY = "resolution";
    public static final String ABSTRACT_KEY = "abstract";
    public static final String FORMAT_KEY = "format";
    public static final String UUID_KEY_ = "uuid";
    public static final String PUBLICATION_DATE_KEY = "publicationDate";
    public static final String METADATA_MODIFIED_DATE_KEY = "metadataModifiedDate";
    public static final String ORGANISATION_EMAIL_KEY = "organisationEmail";
    public static final String ORGANISATION_NAME_KEY = "organisationName";
    public static final String KEYWORDS_KEY = "keywords";
    public static final String KEYWORD_SEPARATOR_KEY = "keywordSeparator";
    public static final String TOPICS_KEY = "topics";
    public static final String TOPIC_SEPARATOR_KEY = "topicSeparator";
    public static final String GEOGRAPHIC_IDENTIFIER_KEY = "geographicIdentifier";
    public static final String GEOGRAPHIC_URI_KEY = "locationUri";
    public static final String BBOX_WEST_LONGITUDE_KEY = "bboxWestLongitude";
    public static final String BBOX_EAST_LONGITUDE_KEY = "bboxEastLongitude";
    public static final String BBOX_SOUTH_LATITUDE_KEY = "bboxSouthLatitude";
    public static final String BBOX_NORTH_LATITUDE_KEY = "bboxNorthLatitude";
    public static final String FILE_NAME_KEY = "fileName";
    public static final String DOWNLOAD_URI_KEY = "downloadUri";
    public static final String THUMBNAIL_URI_KEY = "thumbnailUri";
    private static final String GDS_LOG = "pdok.geodatastore.api";
    private static final String QUERY_SERVICE= "q";
    private static final String ISO_19139 = "iso19139";
    private static final String LICENSE_KEY = "license";
    private static final String PUBLISH_EMAIL_XSLT = "/templates/publish-email-transform.xsl";
    @Autowired
    ServletContext servletContext;
    private ServiceConfig serviceConfig = new ServiceConfig();
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
    private SearchManager searchManager;
    @Autowired
    private StringToMetadataParameterBeanConverter metadataConverter;
    @Autowired
    private AccessManager accessManager;
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private GeonetworkDataDirectory geonetworkDataDirectory;
    @Autowired
    private Publish publishController;
    @Value("#{geodatastoreProperties[locationThesaurusName]}")
    private String locationThesaurus;
    @Autowired LocationManager locationManager;
    @Autowired
    private MailUtils mailUtils;



    public GeodatastoreApi() {
        System.out.println("New GeodatastoreApi instance");

    }


    /**
     * Create a new dataset with the received file, some default data and some calculated data from the received file.
     * @param dataset the file
     * @param lang dataset language
     * @param request the HttpServletRequest
     * @return a {@link ResponseEntity} with {@link MetadataParametersBean} with the dataset properties and a 200 status
     * if the data was created. It has an error property. If it is <code>true</code> then there was an error when trying
     * to create the dataset and the message property should contain the cause.
     *
     * @see MetadataParametersBean
     */
    @RequestMapping(value = "/dataset", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<MetadataParametersBean> uploadDataset(@RequestParam("dataset") MultipartFile dataset,
                                                              @PathVariable("lang") String lang, HttpServletRequest request) {
        if (!dataset.isEmpty()) {
            MetadataParametersBean response = new MetadataParametersBean();
            HttpStatus status = HttpStatus.OK;

            try {
                ServiceContext context = serviceManager.createServiceContext("geodatastore.api.dataset", lang, request);
                UserSession session = context.getUserSession();
                final String username = session.getUsername();
                assert username != null;
                User user = userRepository.findOneByUsername(username);
                List<Integer> groupsIds = userGroupRepository.findGroupIds(Specifications.where(
                        hasProfile(Profile.Reviewer)).and(hasUserId(user.getId())));
                Group group;
                if (groupsIds.size() != 0) {
                    Collections.sort(groupsIds);
                    group = groupRepository.findOne(groupsIds.get(0));
                } else {
                    String message = "No Reviewer group found for user " + username + ". Groups: [" + StringUtils.join(groupsIds, ",") + "]";
                    Log.info(GDS_LOG, "UnauthorizedException - " + message);
                    throw new UnAuthorizedException(message, groupsIds);
                }

                if (StringUtils.isBlank(group.getEmail())) {
                    String message = "The group " + group.getName() + " must have an email set";
                    Log.info(GDS_LOG, message);
                    throw new ServiceNotAllowedEx(message);
                }
                String organisationEmail = group.getEmail();
				
				//metadata uses group description as organisation title, can not be empty
				String organisation = group.getDescription();
				if (organisation==""){
					Log.warning(GDS_LOG, "organisationname-cannot-be-empty: "+username);
					throw new ServiceNotAllowedEx("organisationname-cannot-be-empty",null);
				}
				
                UUID uuid = UUID.randomUUID();
                ISODate creationDate = new ISODate();
                String defaultLocation = "http://geodatastore.pdok.nl/registry/location#Nederland_country";

                Map<String, Object> templateParameters = prepareTemplateParameters(organisation, organisationEmail,
                        new ArrayList<String>(), new ArrayList<String>(), defaultLocation, "2", "5", "50", "54",
                        dataset.getContentType(), "http://example.com/geonetwork/id/dataset/" + uuid.toString(), dataset.getOriginalFilename(),
                        uuid.toString(), creationDate, "http://creativecommons.org/licenses/by/4.0/", "");

                Element metadata = metadataUtil.fillXmlTemplate(templateParameters);

                int userId = context.getUserSession().getUserIdAsInt();
                String docType = null, category = "geodatastore";
                boolean updateFixedInfo = true, indexImmediate = true;
                String createdId = metadataManager.insertMetadata(context, ISO19139SchemaPlugin.IDENTIFIER, metadata, uuid.toString(),
                        userId, Integer.toString(group.getId()), settingManager.getSiteId(), MetadataType.METADATA.codeString,
                        docType, category, creationDate.getDateAsString(), creationDate.getDateAsString(), updateFixedInfo, indexImmediate);



                metadataManager.setStatus(context, Integer.parseInt(createdId), Integer.parseInt(Params.Status.DRAFT), creationDate,
                        "Initial creation");

                String fileName = dataset.getOriginalFilename();
                String fsize = Long.toString(dataset.getSize());
                String access = "private";
                String overwrite = "no";

                IResourceUploadHandler uploadHook = (IResourceUploadHandler) context.getApplicationContext().getBean("resourceUploadHandler");
                uploadHook.onUpload(dataset.getInputStream(), context, access, overwrite, Integer.parseInt(createdId), fileName, Double.parseDouble(fsize));


                Log.info(GDS_LOG, "UPLOADED:" + fileName + "," + createdId + "," + context.getIpAddress() + "," + username);

                ServletPathFinder pathFinder = new ServletPathFinder(servletContext);
                String downloadUrl = getSiteURL(pathFinder) + "/id/dataset/" + uuid.toString()+ "/" + fileName;
                Map<String, String[]> allParams = Maps.newHashMap(request.getParameterMap());
                // Set parameter and process metadata to reference the uploaded file
                allParams.put("url", new String[]{downloadUrl});
                allParams.put("name", new String[]{dataset.getOriginalFilename()});
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
                        String message ="Not found: "
                                + report.getNotFoundMetadataCount() + ", Not owner: " + report.getNotEditableMetadataCount()
                                + ", No process found: " + report.getNoProcessFoundCount() + ".";
                        Log.info(GDS_LOG, message);
                        throw new BadParameterEx("Processing failed", message);
                    }
                } catch (Exception e) {
                    Log.warning(GDS_LOG, "Error processing the new metadata template. " + e);
                    throw e;
                }

                // Build response
                response.setIdentifier(uuid.toString());
                response.setError(false);
                //response.setExtent();
                response.setSummary((String) templateParameters.get(ABSTRACT_KEY));
                response.setLineage((String) templateParameters.get(LINEAGE_KEY));
                response.setLicense((String) templateParameters.get(LICENSE_KEY));
                // TODO set the right location
                if (StringUtils.isNotBlank((String) templateParameters.get(GEOGRAPHIC_IDENTIFIER_KEY))) {
                    KeywordBean locationResult = locationManager.getKeywordById(locationThesaurus, defaultLocation);
                    if (locationResult != null && locationResult.getDefaultValue() != null) {
                        response.setLocation(locationResult.getDefaultValue());
                    }
                    response.setLocationUri(defaultLocation);
                }
                response.setResolution((String) templateParameters.get(RESOLUTION_KEY));
                response.setStatus("draft");
                response.setTitle((String) templateParameters.get(TITLE_KEY));
                response.setUrl(downloadUrl);
                response.setFileType((String) templateParameters.get(FORMAT_KEY));
            } catch (UnAuthorizedException e) {
                Log.info(GDS_LOG, "Unauthorized access", e);
                response.setError(true);
                response.addMessage(e.getMessage() + " - " + e.getObject());
                status = HttpStatus.FORBIDDEN;
            } catch (ServiceNotAllowedEx e) {
                Log.info(GDS_LOG, "Service not allowed", e);
                response.setError(true);
                response.addMessage(e.getMessage() + " - " + e.getObject());
                status = HttpStatus.FORBIDDEN;
            } catch (BadParameterEx e) {
                Log.info(GDS_LOG, "Bad parameter", e);
                response.setError(true);
                response.addMessage(e.getMessage() + ". " + e.getObject());
                status = HttpStatus.BAD_REQUEST;
            } catch (Exception e) {
                Log.warning(GDS_LOG, "General exception", e);
                response.setError(true);
                response.addMessage(e.getMessage());
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            return new ResponseEntity<>(response, status);
        } else {
            MetadataParametersBean response = new MetadataParametersBean();
            response.setError(true);
            response.addMessage("empty.file");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    private Map<String, Object> prepareTemplateParameters(String organisation, String organisationEmail, List<String> keywords,
                                                          List<String> topics, String geographicIdentifier, String bboxWestLongitude,
                                                          String bboxEastLongitude, String bboxSouthLatitude, String bboxNorthLatitude,
                                                          String format, String downloadUri, String fileName, String uuid, ISODate creationDate, String license, String title) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(ORGANISATION_NAME_KEY, organisation);
        parameters.put(ORGANISATION_EMAIL_KEY, organisationEmail);

        parameters.put(METADATA_MODIFIED_DATE_KEY, creationDate.getDateAsString());
        parameters.put(LINEAGE_KEY, "");
        parameters.put(TITLE_KEY, title);
        parameters.put(PUBLICATION_DATE_KEY, creationDate.getDateAsString());

        parameters.put(UUID_KEY_, uuid);
        parameters.put(ABSTRACT_KEY, "");
        parameters.put(THUMBNAIL_URI_KEY, "");

        String keywordList = Joiner.on("#").join(keywords);
        parameters.put(KEYWORDS_KEY, keywordList);
        parameters.put(KEYWORD_SEPARATOR_KEY, "#");
        //parameters.put(USE_LIMITATION_KEY, "None");
        //parameters.put(RESOLUTION_KEY, "10000");

        String topicList = Joiner.on("#").join(topics);
        parameters.put(TOPICS_KEY, topicList);
        parameters.put(TOPIC_SEPARATOR_KEY, "#");
        if (StringUtils.isNotBlank(geographicIdentifier) && geographicIdentifier.contains("#")){
            String[] splitedUri = StringUtils.split(geographicIdentifier, '#');
            String geographicCode = splitedUri[1];
            parameters.put(GEOGRAPHIC_URI_KEY, geographicIdentifier); // location
            parameters.put(GEOGRAPHIC_IDENTIFIER_KEY, geographicCode);
        }
        parameters.put(BBOX_WEST_LONGITUDE_KEY, bboxWestLongitude);
        parameters.put(BBOX_EAST_LONGITUDE_KEY, bboxEastLongitude);
        parameters.put(BBOX_SOUTH_LATITUDE_KEY, bboxSouthLatitude);
        parameters.put(BBOX_NORTH_LATITUDE_KEY, bboxNorthLatitude);

        parameters.put(FORMAT_KEY, format);
        parameters.put(DOWNLOAD_URI_KEY, downloadUri);
        parameters.put(FILE_NAME_KEY, fileName);
        parameters.put(LICENSE_KEY,license);


        return parameters;
    }

    /**
     * Update an existing metadata record.
     * @param identifier
     * @return
     */
    @RequestMapping(value = "/dataset/{identifier}", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<Object> updateDataset(@PathVariable("lang") String lang,
                                         @PathVariable("identifier") String identifier,
                                         @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                                         @RequestParam(value = "metadata", required = false) String metadata,
                                         @RequestParam(value = "publish", defaultValue = "true", required = false) Boolean publish,
                                         HttpServletRequest request) {
        MetadataParametersBean response = new MetadataParametersBean();
        HttpStatus status;
        ServiceContext context = serviceManager.createServiceContext("geodatastore.api.dataset", lang, request);

        try {
            String metadataId = metadataManager.getMetadataId(identifier);
            if (StringUtils.isBlank(metadataId)) {
                Log.info(GDS_LOG, "Metadata with UUID " + identifier + " not found");
                throw new MetadataNotFoundEx(identifier);
            }

            UserSession session = context.getUserSession();
            final String username = session.getUsername();
            assert username != null;
            //String organisation = session.getOrganisation();
            
            User user = userRepository.findOneByUsername(username);
            List<Integer> groupsIds = userGroupRepository.findGroupIds(Specifications.where(
                    hasProfile(Profile.Reviewer)).and(hasUserId(user.getId())));

            Group group;
            if (groupsIds.size() != 0 && accessManager.canEdit(context, metadataId)) {
                Collections.sort(groupsIds);
                group = groupRepository.findOne(groupsIds.get(0));
            } else {
                String message = "No Reviewer group found for user " + username + " in groups " + StringUtils.join(groupsIds, ", ");
                Log.warning(GDS_LOG, message);
                throw new UnAuthorizedException(message, groupsIds);
            }
			
			//organisation uses group description field, if empty no metadata creation is possible
			String organisation = group.getDescription();
			if (organisation==""){
                Log.warning(GDS_LOG, "organisationname-cannot-be-empty: "+username);
                throw new UnAuthorizedException("organisationname-cannot-be-empty",null);
            }
			
            if (StringUtils.isBlank(group.getEmail())) {
                String message = "The group " + group.getName() + " must have an email set";
                Log.warning(GDS_LOG, message);
                throw new ServiceNotAllowedEx(message);
            }
            String organisationEmail = group.getEmail();

            if (metadata != null) {
                MetadataParametersBean metadataParameter = metadataConverter.convert(metadata);
                Map<String, Object> templateParameters = new HashMap<>();
                ISODate changeDate = new ISODate();
                if (metadataParameter != null) {
                    templateParameters = prepareTemplateParameters(metadataParameter, organisation, organisationEmail, changeDate.getDateAsString());
                }


                Element oldMetadata = metadataManager.getMetadataNoInfo(context, metadataId);
                Element newMetadata = metadataUtil.updateMetadataContents(templateParameters, oldMetadata);

                boolean updateFixedInfo = true, indexImmediate = false, validate = false, updateTimespamp = false;
                Metadata createdMd = metadataManager.updateMetadata(context, metadataId, newMetadata, validate,
                        updateFixedInfo, indexImmediate, lang, changeDate.getDateAsString(), updateTimespamp);
                Log.debug(GDS_LOG, "Metadata " + createdMd.getId() + " updated");
            }

            if (thumbnail != null && !thumbnail.isEmpty()) {
                Log.debug(GDS_LOG, "Adding thumbnail to metadata " + metadataId);
                //--- create destination directory
                Path metadataPublicDatadir = Lib.resource.getDir(context, Params.Access.PUBLIC, metadataId);
                Files.createDirectories(metadataPublicDatadir);

                removeOldThumbnail(context ,metadataId, "normal", false);

                //--- move uploaded file to destination directory
                Files.copy(thumbnail.getInputStream(), metadataPublicDatadir.resolve(thumbnail.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
                boolean small = false, indexAfterChange = false;
                metadataManager.setThumbnail(context, metadataId, small, thumbnail.getOriginalFilename(), indexAfterChange);
                Log.debug(GDS_LOG, "Thumbnail to metadata " + metadataId + " successfully added");
            }

            metadataManager.indexMetadata(metadataId, true);
            LuceneSearcher searcher = (LuceneSearcher) searchManager.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
            Element queryParameters = new Element(Jeeves.Elem.REQUEST);
            queryParameters.addContent(new Element(Geonet.SearchResult.FAST).setText("index"));
            queryParameters.addContent(new Element(Geonet.IndexFieldNames.UUID).setText(identifier));
            queryParameters = SearchDefaults.getDefaultSearch(context, queryParameters);
            searcher.search(context, queryParameters, serviceConfig);
            Element results = searcher.present(context, queryParameters, serviceConfig);
            SearchResponse searchResponse = new SearchResponse(locationManager, locationThesaurus);
            searchResponse.initFromXml(results);

            MetadataParametersBean result  = new MetadataParametersBean();
            if (searchResponse.getCount() > 0 && searchResponse.getMetadata().size() > 0) {
                result = searchResponse.getMetadata().get(0);
                /*if (StringUtils.isNotBlank(result.getLocationUri())) {
                    KeywordBean locationBean = locationManager.getKeywordById(locationThesaurus, result.getLocationUri());
                    if (locationBean != null) {
                        result.setLocation(locationBean.getDefaultValue());
                    }
                }*/
            }

            if (publish && result.isValid()) {
                Log.debug(GDS_LOG, "Publishing metadata " + metadataId);
                Publish.PublishReport report =  publishController.publish(lang, request, metadataId, false);
                if (report.getPublished() > 0 || report.getUnmodified() > 0 ) {
                    metadataManager.setStatus(context, Integer.parseInt(metadataId),
                            Integer.parseInt(Params.Status.APPROVED), new ISODate(),
                            "Publish dataset");
                    metadataManager.indexMetadata(metadataId, true);
                    result.setStatus("published");
                    Log.debug(GDS_LOG, "Metadata " + metadataId + " successfully published");
                    String userEmail = user.getEmail();

                    try {
                        if (StringUtils.isBlank(userEmail)) {
                            Log.warning(GDS_LOG, "Cannot send published email to user " + user.getUsername()
                                    + " because there is not associated email address");
                        } else  {
                            Map<String, String> mailTemplateParameters = new HashMap<>();
                            mailTemplateParameters.put("site", settingManager.getSiteName());
                            mailTemplateParameters.put("siteURL", settingManager.getSiteURL("dut"));
                            mailTemplateParameters.put("userName", user.getName());
                            mailTemplateParameters.put("datasetTile", result.getTitle());

                            boolean sent = mailUtils.sendHtmlEmail(userEmail, mailTemplateParameters, PUBLISH_EMAIL_XSLT);
                            if (!sent) {
                                Log.error(GDS_LOG, "The publish email cannot be sent. Please review the mail server settings in the database");
                            }
                        }
                    } catch (Exception e) {
                        Log.error(GDS_LOG, "Error sending publish email to " + userEmail, e);
                    }
                } else if (report.getDisallowed() > 0) {
                    response = result;
                    String message = "You cannot publish data. You must be at least Reviewer in the group {id="
                            + group.getId() +", name=" + group.getName() + "}";
                    Log.warning(GDS_LOG, message);
                    throw new UnAuthorizedException(message, null);
                }


            }

            return new ResponseEntity<Object>(result, HttpStatus.OK);

        } catch (IllegalArgumentException iae) {
            Log.info(GDS_LOG, "Bad or not present parameter in the request", iae);
            response.setIdentifier(identifier);
            response.setError(true);
            response.addMessage("update.bad.metadata.json.parameter");
            status = HttpStatus.BAD_REQUEST;
        } catch (MetadataNotFoundEx mnfe) {
            Log.info(GDS_LOG, "Metadata " + identifier + " not found", mnfe);
            response.setIdentifier(identifier);
            response.setError(true);
            response.addMessage("error.metadata.notfound");
            status = HttpStatus.NOT_FOUND;
        } catch (UnAuthorizedException ue) {
            Log.info(GDS_LOG, "Access to the metadata " + identifier + " forbidden", ue);
            response.setIdentifier(identifier);
            response.setError(true);
            response.addMessage("error-forbidden");
            status = HttpStatus.FORBIDDEN;
        } catch (ServiceNotAllowedEx sne) {
            Log.warning(GDS_LOG, "Please configure the email for the user's group", sne);
            response.setIdentifier(identifier);
            response.setError(true);
            response.addMessage("error-noemail");
            status = HttpStatus.FORBIDDEN;
        } catch (Exception e) {
            Log.error("General error at updateDataset method", e);
            context.error(e);
            response.setIdentifier(identifier);
            response.setError(true);
            response.addMessage("update.server.error");
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<Object>(response, status);
    }

    private Map<String, Object> prepareTemplateParameters(MetadataParametersBean metadataParameter, String organisation, String organisationEmail, String changeDate) {
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put(ORGANISATION_NAME_KEY, organisation);
        parametersMap.put(ORGANISATION_EMAIL_KEY, organisationEmail);
        parametersMap.put(METADATA_MODIFIED_DATE_KEY, changeDate);

        if (metadataParameter.getTitle() != null) {
            parametersMap.put(TITLE_KEY, metadataParameter.getTitle());
        }
        if (metadataParameter.getSummary() != null) {
            parametersMap.put(ABSTRACT_KEY, metadataParameter.getSummary());
        }
        if (metadataParameter.getKeywords() != null /*&& metadataParameter.getKeywords().size() > 0*/) {
            String keywordSeparator = "#";
            String keywordList = Joiner.on(keywordSeparator).join(metadataParameter.getKeywords());
            parametersMap.put(KEYWORD_SEPARATOR_KEY, keywordSeparator);
            parametersMap.put(KEYWORDS_KEY, keywordList);
        }
        if (metadataParameter.getTopicCategories() != null /*&& metadataParameter.getTopicCategories().size() > 0*/) {
            String topicSeparator = "#";
            List<String> purgedTopicCatList = new ArrayList<>(metadataParameter.getTopicCategories());
            purgedTopicCatList.removeAll(Collections.singleton(null));
            String topicList = Joiner.on(topicSeparator).join(purgedTopicCatList);
            parametersMap.put(TOPIC_SEPARATOR_KEY, topicSeparator);
            parametersMap.put(TOPICS_KEY, topicList);
        }
        // Location
        if (StringUtils.isNotBlank(metadataParameter.getLocationUri())) {
            // TODO recover coordinates from the service and pass them to the template
            String[] splitLocationUri = StringUtils.split(metadataParameter.getLocationUri(), "#");
            if (splitLocationUri.length == 2) {
                String location = splitLocationUri[1];
                KeywordBean locationKeyword =  locationManager.getKeywordById(locationThesaurus, metadataParameter.getLocationUri());
                if (locationKeyword != null) {
                    parametersMap.put(GEOGRAPHIC_URI_KEY, metadataParameter.getLocationUri());
                    parametersMap.put(GEOGRAPHIC_IDENTIFIER_KEY, location);
                    parametersMap.put(BBOX_WEST_LONGITUDE_KEY, locationKeyword.getCoordWest());
                    parametersMap.put(BBOX_EAST_LONGITUDE_KEY, locationKeyword.getCoordEast());
                    parametersMap.put(BBOX_SOUTH_LATITUDE_KEY, locationKeyword.getCoordSouth());
                    parametersMap.put(BBOX_NORTH_LATITUDE_KEY, locationKeyword.getCoordNorth());
                } else {
                    // reset the location URI
                    Log.info(GDS_LOG, "Location id " + location + " not found in thesaurus " + locationThesaurus
                            + " and language " + locationManager.getDefaultLanguage());
                    parametersMap.remove(GEOGRAPHIC_IDENTIFIER_KEY);
                    parametersMap.remove(GEOGRAPHIC_URI_KEY);
                }
            } else {
                Log.info(GDS_LOG, "Location URI isn't compounded by firstpart#secondpart. Ignoring the value.");
            }

        }
        if (metadataParameter.getLineage() != null) {
            parametersMap.put(LINEAGE_KEY, metadataParameter.getLineage());
        }
        if (metadataParameter.getLicense() != null) {
            parametersMap.put(LICENSE_KEY, metadataParameter.getLicense());
        }
        if (metadataParameter.getResolution() != null) {
            parametersMap.put(RESOLUTION_KEY, metadataParameter.getResolution());
        }

        return parametersMap;
    }

    @RequestMapping(value = "/dataset/{identifier}", method = RequestMethod.DELETE)
    public  @ResponseBody
    ResponseEntity<Object> deleteDataset(@PathVariable("identifier") String identifier, @PathVariable("lang") String lang,
                                         HttpServletRequest request) {
        ServiceContext context = serviceManager.createServiceContext("geodatastore.api.dataset", lang, request);
        Map<String, Object> responseMap = new HashMap<>();
        HttpStatus status;

        try {
            // If send a non existing uuid, Utils.getIdentifierFromParameters returns null
            Metadata metadata = metadataRepository.findOneByUuid(identifier);
            if (metadata == null) {
                Log.info(GDS_LOG, "DeleteDataset: metadata not found (uuid=" + identifier + ")");
                throw new MetadataNotFoundEx("Metadata internal identifier or UUID not found.");
            }
            String id = metadataManager.getMetadataId(identifier);

            // Check permissions
            UserSession session = context.getUserSession();
            final String username = session.getUsername();
            if (!accessManager.canEdit(context, id)) {
                Log.info(GDS_LOG, "DeleteDataset: user " + username + " cannot edit the metadata id=" + id);
                throw new OperationNotAllowedEx("The user " + username + " cannot edit the metadata " + id);
            }

            //-----------------------------------------------------------------------
            //--- remove the metadata directory including the public and private directories.
            IO.deleteFileOrDirectory(Lib.resource.getMetadataDir(geonetworkDataDirectory, id));

            //-----------------------------------------------------------------------
            //--- delete metadata and return status
            metadataManager.deleteMetadata(context, id);
            Log.debug(GDS_LOG, "Metadata id=" + id + " has been deleted");
            responseMap.put("error", false);
            responseMap.put("messages", new ArrayList<String>());
            responseMap.put("identifier", identifier);
            status = HttpStatus.OK;

        } catch (MetadataNotFoundEx e) {
            Log.info(GDS_LOG, "Metadata not found", e);
            responseMap.put("error", true);
            List<String> messages = new ArrayList<>();
            messages.add("dataset.delete.notfound");
            responseMap.put("messages", messages);
            responseMap.put("identifier", identifier);
            status = HttpStatus.NOT_FOUND;
        } catch (OperationNotAllowedEx onae) {
            Log.info(GDS_LOG, "The user doesn't have enough permissions to delete the metadata " + identifier, onae);
            responseMap.put("error", true);
            List<String> messages = new ArrayList<>();
            messages.add("error.metadata.delete.permission");
            responseMap.put("messages", messages);
            responseMap.put("identifier", identifier);
            status = HttpStatus.FORBIDDEN;
        }

        catch (Exception e) {
            Log.error(GDS_LOG, "Unknown error deleting the metadata " + identifier, e);
            context.error(e);
            responseMap.put("error", true);
            responseMap.put("identifier", identifier);
            List<String> messages = new ArrayList<>();
            messages.add("update.server.error");
            responseMap.put("messages", messages);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<Object>(responseMap, status);
    }

    @RequestMapping(value="/registry")
    public List<String> getAvailableCodelists() {
        List<String> result = new ArrayList<>();
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

        final ServiceContext context = serviceManager.createServiceContext("geodatastore.registry", lang, request);
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

                return new ResponseEntity<>(Xml.getString(responseXML), responseHeaders, HttpStatus.OK);
            } else  {
                responseHeaders.setContentType(new MediaType("application", "json"));
                return new ResponseEntity<>(Xml.getJSON(responseXML), responseHeaders, HttpStatus.OK);
            }
        } else {
            responseHeaders.setContentType(new MediaType("application", "json"));
            return new ResponseEntity<>(Xml.getJSON(responseXML), responseHeaders, HttpStatus.OK);
        }
    }

    /**
     * Search user datasets.
     * @return
     */
    @RequestMapping(value = "/datasets", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<Object> uploadDataset(
            @PathVariable("lang") String lang,
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "sortBy", defaultValue="changeDate") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
            @RequestParam(value = "from", defaultValue = "1") Integer from,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "status", defaultValue = "published") String status,
            @RequestParam(value = "summaryOnly", defaultValue = "false") Boolean summaryOnly,
            HttpServletRequest request) {

        try {
            String statusParam = Params.Status.APPROVED;
            if ("draft".equals(status)) {
                statusParam = Params.Status.DRAFT;
            }
            MetaSearcher searcher = searchManager.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
            ServiceContext context = serviceManager.createServiceContext("geodatastore.api", lang, request);
            UserSession session = context.getUserSession();
            Element parametersAsXml = buildSearchXmlParameters(context, q, sortBy, sortOrder, from, pageSize, statusParam);
            // FIXME Why save search parameters in session?
            session.setProperty(Geonet.Session.SEARCH_REQUEST, parametersAsXml.clone());
            searcher.search(context, parametersAsXml, serviceConfig);
            if (!summaryOnly) {
                Element results = searcher.present(context, parametersAsXml, serviceConfig);
                SearchResponse searchResponse = new SearchResponse(locationManager, locationThesaurus);
                searchResponse.initFromXml(results);

                return new ResponseEntity<>((Object) searchResponse, HttpStatus.OK);
            } else {
                // summary only
                Element summary = searcher.getSummary();
                SearchResponse searchResponse = new SearchResponse();
                searchResponse.initFromSummary(summary);

                return new ResponseEntity<>((Object) searchResponse, HttpStatus.OK);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }


        return new ResponseEntity<>((Object) "{}",  HttpStatus.OK);
    }

    /**
     * Builds an Element that can be used by the searcher with the parameters passed to the method.
     * It also add the default values for missing parameters.
     *
     * @param context Service context
     * @param q query string.
     * @param sortBy field name to order by.
     * @param sortOrder "asc" or "desc" order.
     * @param from first element to return.
     * @param pageSize elements per page.
     * @param status draft or published.
     * @return the query for the searcher.
     */
    private Element buildSearchXmlParameters(ServiceContext context, String q, String sortBy, String sortOrder, Integer from,
                                             Integer pageSize, String status) {
		UserSession session = context.getUserSession();
        final String username = session.getUsername();										 
		User user = userRepository.findOneByUsername(username);
        List<Integer> groupsIds = userGroupRepository.findGroupIds(Specifications.where(
            hasProfile(Profile.Reviewer)).and(hasUserId(user.getId())));
					
        Element queryParameters = new Element(Jeeves.Elem.REQUEST);
        queryParameters.addContent(new Element(Geonet.IndexFieldNames.ANY).setText(q));
        queryParameters.addContent(new Element(Geonet.SearchResult.SORT_BY).setText(sortBy));
        if (StringUtils.isNotBlank(sortOrder) && sortOrder.equals("asc")) {
            // This is not a bug. Ascending order needs "reverse" to be passed to Lucene.
            queryParameters.addContent(new Element(Geonet.SearchResult.SORT_ORDER).setText("reverse"));
        }
        queryParameters.addContent(new Element(Geonet.SearchResult.HITS_PER_PAGE).setText(Integer.toString(pageSize)));
        queryParameters.addContent(new Element("from").setText(Integer.toString(from)));
        queryParameters.addContent(new Element("to").setText(Integer.toString(from + pageSize - 1)));
        queryParameters.addContent(new Element(Geonet.IndexFieldNames.CAT).setText("geodatastore"));
        queryParameters.addContent(new Element(Geonet.IndexFieldNames.STATUS).setText(status));
        queryParameters.addContent(new Element(Geonet.SearchResult.FAST).setText("index"));
        queryParameters.addContent(new Element("_isTemplate").setText("n"));
        queryParameters.addContent(new Element("type").setText("dataset"));
		queryParameters.addContent(new Element("_groupOwner").setText(StringUtils.join(groupsIds, " or ")));

        return SearchDefaults.getDefaultSearch(context, queryParameters);
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

    private void removeOldThumbnail(ServiceContext context, String id, String type, boolean indexAfterChange) throws Exception {

        Element result = metadataManager.getThumbnails(context, id);

        if (result == null)
            throw new IllegalArgumentException("Metadata not found --> " + id);

        result = result.getChild(type);

        //--- if there is no thumbnail, we return

        if (result == null)
            return;

        //-----------------------------------------------------------------------
        //--- remove thumbnail

        metadataManager.unsetThumbnail(context, id, type.equals("small"), indexAfterChange);

        // FIXME physically remove the thumbnail file from the filesystem.

        //--- remove file

        /*String file = Lib.resource.getDir(context, Params.Access.PUBLIC, id) + getFileName(result.getText());
        if (!new File(file).delete()) {
            context.error("Error while deleting thumbnail : "+file);
        }*/
    }
}
