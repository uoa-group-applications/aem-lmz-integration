package nz.ac.auckland.aem.lmz.core;

import nz.ac.auckland.aem.lmz.lmzconfigdialog.LMZConfigDialogFactory;
import nz.ac.auckland.lmzwidget.configuration.model.WidgetConfiguration;
import nz.ac.auckland.lmzwidget.configuration.parser.WidgetConfigurationParser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by gregkw on 17/12/14.
 *
 * This class creates a component in the JCR based on the information is gathers and parses
 * from an LMZ widget configuration endpoint.
 */
public class LMZWidgetComponentCreator {

    private static Logger logger = LoggerFactory.getLogger(LMZWidgetComponentCreator.class);

    /**
     * Http client
     */
    private HttpClient client = new HttpClient();

    /**
     * Context
     */
    private ComponentBeanContext context;

    /**
     * Initialize data-members
     */
    public LMZWidgetComponentCreator(ComponentBeanContext context) {
        this.context = context;
    }


    /**
     *  Fetch the configuration from the widget endpoint
     *
     *  @return true if fetch was successful, otherwise return false.
     */
    public WidgetConfiguration fetchConfiguration(String endpoint) {

        String configurationUrl = getConfigurationUrl(endpoint);

        // no host defined yet
        if (StringUtils.isBlank(configurationUrl)) {
            logger.info("No configuration URL was found, fetching skipped.");
            return null;
        }

        logger.info("Requesting configuration URL: " + configurationUrl);

        HttpMethod method = null;
        try {
            method = new GetMethod(configurationUrl);
            method.setFollowRedirects(true);
            client.executeMethod(method);
            String responseBody = method.getResponseBodyAsString();

            return toWidgetConfiguration(responseBody);
        } catch (IOException ioEx) {
            logger.error("Could not finish the configuration request", ioEx);
        } catch (IllegalStateException isEx) {
            logger.error("This URL specified is incorrect, aborting.", isEx);
        }
        finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return null;
    }


    /**
     *  Persist a widget configuration properly.
     *
     *  @param widgetConfig is the configuration that is to be persisted
     */
    public void persistWidgetConfig(String endpointUrl, WidgetConfiguration widgetConfig) {

        String catalogName = getSanitizedCatalogName();

        LMZConfigDialogFactory dialogFactory = getDialogFactoryInstance();
        dialogFactory.setWidgetConfiguration(widgetConfig);
        dialogFactory.setWidgetURL(sanitizeRequestUrl(endpointUrl));

        try {
            Session session = context.getResourceResolver().adaptTo(Session.class);

            String componentName = getCamelcaseComponentName(widgetConfig);

            // make sure the node location the component is going to sit in, exists.
            Node root = ensureCatalogExists(catalogName);

            // if there already was a component there? remove it.
            if (root.hasNode(componentName)) {
                session.removeItem("/apps/lmzconfig/components/" + catalogName + "/" + componentName);
            }

            Node componentNode = root.addNode(componentName, "cq:Component");
            componentNode.setProperty("allowedParents", "*/parsys");
            componentNode.setProperty("componentGroup", getRawCatalogName());
            componentNode.setProperty("jcr:title", widgetConfig.getWidget().getDescription());
            componentNode.setProperty("requestUrl", sanitizeRequestUrl(endpointUrl));
            componentNode.setProperty("maintenanceMode", isInMaintenanceMode());
            componentNode.setProperty("maintenanceMessage", getMaintenanceMessage());
            componentNode.setProperty("sling:resourceSuperType", "/apps/lmz-integration/components/lmzwidget-base");

            dialogFactory.createCQDialog(componentNode);

            session.save();

            // copy the cq edit configuration node
            Node ed = session.getNode("/apps/lmz-integration/components/lmzcatalogcomponent/cq:editConfig");
            session.getWorkspace().copy(ed.getPath(), componentNode.getPath() + "/cq:editConfig");

        } catch (RepositoryException rEx) {
            logger.error("A repository exception occured", rEx);
        }
    }

    /**
     * @return the dialog factory instance (extracted for testing purposes)
     */
    protected LMZConfigDialogFactory getDialogFactoryInstance() {
        return new LMZConfigDialogFactory();
    }

    /**
     * @return the camel case version of the component name
     */
    protected String getCamelcaseComponentName(WidgetConfiguration widgetConfig) {
        return LMZConfigDialogFactory.camelCaseString(widgetConfig.getWidget().getName());
    }

    /**
     * Make sure /apps/lmzconfig/components/<catalogname> exists
     *
     * @param catalogName is the catalog name to ensure that exists
     * @return the node that has been last passed
     * @throws RepositoryException
     */
    protected Node ensureCatalogExists(String catalogName) throws RepositoryException {
        Node root = this.context.getCurrentNode().getSession().getNode("/apps");

        if (!root.hasNode("lmzconfig")) {
            root = root.addNode("lmzconfig", "nt:folder");
        } else {
            root = root.getNode("lmzconfig");
        }
        if (!root.hasNode("components")) {
            root = root.addNode("components", "nt:folder");
        } else {
            root = root.getNode("components");
        }
        if (!root.hasNode(catalogName)) {
            root = root.addNode(catalogName, "nt:folder");
        } else {
            root = root.getNode(catalogName);
        }
        return root;
    }

    public String getSanitizedCatalogName() {
        return LMZConfigDialogFactory.camelCaseString(getRawCatalogName());
    }

    /**
     * @return the raw catalog name stored in the JCR
     */
    public String getRawCatalogName() {
        return this.context.getProperties().get("name", "catalogName");
    }


    /**
     * @return a widget configuration instance for the configuration json string
     */
    protected WidgetConfiguration toWidgetConfiguration(String configString) {
        WidgetConfigurationParser parser = new WidgetConfigurationParser();
        return parser.parse(configString);
    }

    /**
     * @return the request url, make sure it is always ending in a '/'
     */
    protected String sanitizeRequestUrl(String reqUrl) {
        if (!reqUrl.endsWith("/")) {
            return reqUrl + "/";
        }
        return reqUrl;
    }

    /**
     * @return the configuration URL
     */
    protected String getConfigurationUrl(String reqUrl) {
        String requestUrl = this.sanitizeRequestUrl(reqUrl);
        if (StringUtils.isNotBlank(requestUrl)) {
            return requestUrl + "/configuration";
        }
        return null;
    }


    /**
     * @return the widget list
     */
    public String[] getWidgetList() throws RepositoryException {
        Node current = this.context.getCurrentNode();
        if (current == null || !current.hasProperty("widgetEndpoints")) {
            return null;
        }

        Property prop = current.getProperty("widgetEndpoints");
        if (prop.isMultiple()) {
            return this.context.getProperties().get("widgetEndpoints", (String[]) null);
        } else {
            return new String[] {
                    this.context.getProperties().get("widgetEndpoints", (String) null)
            };
        }
    }

    /**
     * Update all the widgets and return some information around it success
     */
    public Map<String, WidgetConfiguration> updateAllWidgets() throws RepositoryException {
        String[] widgetEndpoints = getWidgetList();

        // has elements?
        if (ArrayUtils.isEmpty(widgetEndpoints)) {
            return null;
        }

        Map<String, WidgetConfiguration> result = new LinkedHashMap<String, WidgetConfiguration>();

        // iterate over endpoints
        for (String endpoint : widgetEndpoints) {

            if (StringUtils.isBlank(endpoint)) {
                continue;
            }

            // fetch configuration
            WidgetConfiguration widgetConfig = fetchConfiguration(endpoint);

            // if configuration found, persist component.
            if (widgetConfig != null) {
                persistWidgetConfig(endpoint, widgetConfig);
            }

            result.put(endpoint, widgetConfig);
        }

        return result;
    }

    /**
     * @return true if the maintenance mode has been enabled
     */
    public boolean isInMaintenanceMode() {
        return this.context.getProperties().get("maintenanceMode", false);
    }

    /**
     * @return the maintenance message or null when not setup
     */
    public String getMaintenanceMessage() {
        return this.context.getProperties().get("maintenanceMessage", (String) null);
    }

    public boolean shouldSynchronize() {
        return this.context.isAuthor();
    }
}
