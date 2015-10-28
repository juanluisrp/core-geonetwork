package nl.kadaster.pdok.bussiness;

import com.google.common.collect.Lists;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.MailUtil;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by juanluisrp on 26/10/2015.
 */
@Service
public class MailUtils {
    @Autowired
    private SettingManager settingManager;

    public boolean sendHtmlEmail(String userEmail, Map<String, String> templateParameters, String publishEmailXslt) throws Exception {

        Element mail = getContent(templateParameters, publishEmailXslt);
        String subject = mail.getChildText("subject");
        Element messageElement = mail.getChild("content");
        StringBuilder messageSb = new StringBuilder("");
        for (Object e : messageElement.getContent()) {
            if (e instanceof Text) {
                messageSb.append(((Text) e).getText());
            }
            if (e instanceof Element) {
                messageSb.append(Xml.getString((Element) e));
            }
        }
        String message = messageSb.toString();

        boolean sent = MailUtil.sendHtmlMail(Lists.newArrayList(userEmail), subject, message, settingManager);
        return sent;
    }

    /**
     * Replace the template parameters in template using the XSLT transform stylesheet.
     *
     * @param templateParameters parameters for the transform XSLT.
     * @param transformPath path to the XSLT transform to apply.
     * @return the email content. It must contain an <code>&lt;subject&gt;</code> element and a
     * <code>&lt;content&gt;</code> nodes.
     */
    private Element getContent(Map<String, String> templateParameters, String transformPath) throws Exception {
        Element root = new Element("root");

        for (Map.Entry<String, String> entry : templateParameters.entrySet()) {
            root.addContent(new Element(entry.getKey()).setText(entry.getValue()));
        }

        // Find XSLT
        Resource xsltResource = new ClassPathResource(transformPath);
        URI xsltUri = xsltResource.getURI();

        // Apply XSLT with parameters to the template.
        Element email = Xml.transform(root, Paths.get(xsltUri));

        return email;
    }
}
