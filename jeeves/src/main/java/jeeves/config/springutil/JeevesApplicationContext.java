package jeeves.config.springutil;

import java.io.IOException;

import jeeves.server.overrides.ConfigurationOverrides;
import jeeves.server.sources.http.JeevesServlet;

import org.jdom.JDOMException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.web.FilterInvocation;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class JeevesApplicationContext extends XmlWebApplicationContext {
	
    private String appPath;
    
    /**
     * Node name when running more than one instance
     */
    private String node;
    
    private final ConfigurationOverrides _configurationOverrides;
    
    public JeevesApplicationContext() {
        this(ConfigurationOverrides.DEFAULT);
    }
    
    public JeevesApplicationContext(final ConfigurationOverrides configurationOverrides) {
        this._configurationOverrides = configurationOverrides;
        addApplicationListener(new ApplicationListener<ApplicationEvent>() {
            @Override
            public void onApplicationEvent(ApplicationEvent event) {
                try {
                    if (event instanceof ContextRefreshedEvent) {
                        
                        if (event.getSource() instanceof FilterInvocation){
                            FilterInvocation filter = (FilterInvocation)event.getSource();
                            node = JeevesServlet.getNode(filter.getRequest().getServletPath());
                        }
                        configurationOverrides.applyNonImportSpringOverides(JeevesApplicationContext.this, getServletContext(), appPath, node);
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    RuntimeException e2 = new RuntimeException();
                    e2.initCause(e);
                    throw e2;
                }
            }
        });
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }
    public void setNode(String node) {
        this.node= node;
    }
    
	@Override
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
        reader.setValidating(false);
        super.loadBeanDefinitions(reader);
        try {
            this._configurationOverrides.importSpringConfigurations(reader, 
                    (ConfigurableBeanFactory) reader.getBeanFactory(),
                    getServletContext(), appPath, node);
        } catch (JDOMException e) {
            throw new IOException(e);
        }
    }
}
