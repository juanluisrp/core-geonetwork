package nl.kadaster.pdok.bussiness;

import com.google.common.collect.Multimap;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AddElemValue;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * Created by JuanLuis on 09/07/2015.
 */
@Service
public class MetadataUtil {

    public static final String TEMPLATE_FILE = "templates/pdok-datastore-template.xml";
    private static final java.lang.String NEW_FILE_STYLESHEET_FILE = "geodatastore-new-upload.xsl" ;
    @Autowired
    private SchemaManager schemaManager;


     public Element fillXmlTemplate(Map<String, Object> parameters) throws Exception {
         Resource resource = new ClassPathResource(TEMPLATE_FILE);
         InputStream resourceInputStream = resource.getInputStream();
         Element template = Xml.loadStream(resourceInputStream);
         Path etlMergeStylesheet = schemaManager.getSchemaDir(ISO19139SchemaPlugin.IDENTIFIER).resolve("process/")
                 .resolve(NEW_FILE_STYLESHEET_FILE);
         Element result = Xml.transform(template, etlMergeStylesheet, parameters);

         return result;

     }

}
