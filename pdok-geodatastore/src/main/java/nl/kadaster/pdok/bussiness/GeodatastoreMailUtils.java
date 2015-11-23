package nl.kadaster.pdok.bussiness;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.Session;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by juanluisrp on 26/10/2015.
 */
@Service
public class GeodatastoreMailUtils {
    private static final String LOG_MODULE = "geodatastore.mailutils";
    @Autowired
    private SettingManager settings;


    public boolean sendHtmlEmail(String userEmail, List<String> bccList, Map<String, String> templateParameters, String publishEmailXslt) throws Exception {

        Element mail = getContent(templateParameters, publishEmailXslt);
        String subject = mail.getChildText("subject");
        Element messageElement = mail.getChild("content");
        StringBuilder messageSb = new StringBuilder("");
        getMessage(messageElement, messageSb);
        String message = messageSb.toString();

        return sendEmail(subject, bccList, message, userEmail, new ArrayList<EmailAttachment>(0));
    }


    /**
     * Sends an email to userEmail with BCC to the addresses in bccList and the attachments set in attachmentList.
     * @param userEmail the TO address.
     * @param bccList a list of addresses to BCC.
     * @param templateParameters parameters used to fill the template XSLT.
     * @param attachmentList a list of the path of the files that will be attached to the email.
     * @param emailXslt the email content. It need to have this structure:
     *                  <code>
     *                      &lt;email&gt;
     *                          &lt;subject&gt;Example subject&lt;/subject&gt;
     *                          &lt;content&gt;Example content&lt;/content&gt;
     *                      &lt;/email&gt;
     *                  </code>
     * @return <code>true</code> if the email was sent, <code>false</code> if not.
     * @throws Exception if there is any problem with the addresses, the attachments, the email template or the email server.
     */
    public boolean sendHtmlEmailWithAttachments(String userEmail, List<String> bccList, Map<String, String> templateParameters, List<Path> attachmentList, String emailXslt) throws Exception {
        Element mail = getContent(templateParameters, emailXslt);
        String subject = mail.getChildText("subject");
        Element messageElement = mail.getChild("content");
        StringBuilder messageSb = new StringBuilder("");
        getMessage(messageElement, messageSb);
        String message = messageSb.toString();
        List<EmailAttachment> emailAttachments = new ArrayList<>(attachmentList.size());
        for (Path path : attachmentList) {
            EmailAttachment attachment = new EmailAttachment();
            attachment.setName(path.getFileName().toString());
            attachment.setPath(path.toString());
            attachment.setDescription("Organization logo");
            attachment.setDisposition(EmailAttachment.ATTACHMENT);
            emailAttachments.add(attachment);
        }
        return sendEmail(subject, bccList, message, userEmail, emailAttachments);
    }

    /**
     * Replace the template parameters in template using the XSLT transform stylesheet.
     *
     * @param templateParameters parameters for the transform XSLT.
     * @param transformPath      path to the XSLT transform to apply.
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

    private void getMessage(Element messageElement, StringBuilder messageSb) {
        for (Object e : messageElement.getContent()) {
            if (e instanceof Text) {
                messageSb.append(((Text) e).getText());
            }
            if (e instanceof Element) {
                messageSb.append(Xml.getString((Element) e));
            }
        }
    }

    public boolean sendEmail(String subject, List<String> bccList, String message, String to, List<EmailAttachment> attachment) {

        String username = settings
                .getValue("system/feedback/mailServer/username");
        String password = settings
                .getValue("system/feedback/mailServer/password");
        Boolean ssl = settings
                .getValueAsBool("system/feedback/mailServer/ssl");

        String hostName = settings.getValue("system/feedback/mailServer/host");
        Integer smtpPort = Integer.valueOf(settings
                .getValue("system/feedback/mailServer/port"));

        String from = settings.getValue("system/feedback/email");

        HtmlEmail email = new HtmlEmail();
        try {
            Authenticator authenticator = null;

            Properties javaMailProperties = new Properties();
            javaMailProperties.setProperty("mail.smtp.host", hostName);
            javaMailProperties.setProperty("mail.smtp.port", smtpPort.toString());

            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                authenticator = new DefaultAuthenticator(username, password);
                javaMailProperties.setProperty("mail.smtp.auth", "true");
            }

            if (ssl) {
                javaMailProperties.setProperty("mail.smtp.sasl.enable", "true");
                javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
                javaMailProperties.setProperty("mail.smtp.ssl.trust", "*");
                javaMailProperties.setProperty("mail.smtp.sasl.mechanisms", "PLAIN");
                javaMailProperties.setProperty("mail.smtps.port", smtpPort.toString());
            }

            Session session;
            if (authenticator != null) {
                session = Session.getInstance(javaMailProperties, authenticator);
            } else {
                session = Session.getInstance(javaMailProperties);
            }

            email.setMailSession(session);
            email.setCharset(org.apache.commons.mail.EmailConstants.UTF_8);
            email.setFrom(from);
            email.setSubject(subject);
            email.setHtmlMsg(message);
            if (bccList != null) {
                email.addBcc(bccList.toArray(new String[0]));
            }
            email.addTo(to);
            //email.addTo("juanluisrp@geocat.net", "Juan Luis Rodríguez");

            for (EmailAttachment attach : attachment) {
                email.attach(attach);
            }

            String emailId = email.send();
            Log.info(LOG_MODULE, "Email to " + to + " sent using server " + hostName + ":" + smtpPort + " with ID " + emailId);
            return true;
        } catch(EmailException e) {
            Log.error(LOG_MODULE, "Error sending email", e);
        }

        return false;
    }
}
