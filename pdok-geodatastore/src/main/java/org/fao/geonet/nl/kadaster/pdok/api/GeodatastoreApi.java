package org.fao.geonet.nl.kadaster.pdok.api;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.schema.Info;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanLuis on 30/06/2015.
 */
@Controller
@ReadWriteController
@RequestMapping("/{lang}/geodatastore")
public class GeodatastoreApi {
    private static final String QUERY_SERVICE= "q";
    private static final String ISO_19139 = "iso19139";

    public GeodatastoreApi() {
        System.out.println("New GeodatastoreApi instance");
    }

    /**
     *
     * @param identifier
     * @param model
     * @return
     */
    @RequestMapping(value = "/api/dataset/{identifier}", method = RequestMethod.POST)
    public @ResponseBody MetadataResponseBean uploadDataset(@PathVariable("identifier") String identifier, Model model) {
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



}
