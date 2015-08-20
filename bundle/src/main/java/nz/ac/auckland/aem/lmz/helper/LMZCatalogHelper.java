package nz.ac.auckland.aem.lmz.helper;

import nz.ac.auckland.aem.lmz.core.ComponentBeanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marnix Cook
 *
 * Contains helper functions that are useful for catalog related classes
 */
public class LMZCatalogHelper {

    /**
     * Property where endpoints are located
     */
    public static final String PARAM_WIDGET_ENDPOINTS = "widgetEndpoints";
    public static final String PARAM_UUID = "catalog-uuid";

    private static final Logger LOG = LoggerFactory.getLogger(LMZCatalogHelper.class);

    /**
     * Context
     */
    private ComponentBeanContext context;

    /**
     * Initialize data-members
     *
     * @param context the component context
     */
    public LMZCatalogHelper(ComponentBeanContext context) {
        this.context = context;
    }

    /**
     * @return the widget list
     */
    public String[] getWidgetList() throws RepositoryException {
        Node current = this.context.getCurrentNode();
        if (current == null || !current.hasProperty(PARAM_WIDGET_ENDPOINTS)) {
            return null;
        }

        Property prop = current.getProperty(PARAM_WIDGET_ENDPOINTS);
        if (prop.isMultiple()) {
            return this.context.getProperties().get(PARAM_WIDGET_ENDPOINTS, (String[]) null);
        } else {
            return new String[] {
                this.context.getProperties().get(PARAM_WIDGET_ENDPOINTS, (String) null)
            };
        }
    }

    /**
     * This method retrieves a list of resource types
     *
     * @param jcrSession is the jcr session
     * @return a list of resource types
     * @throws RepositoryException
     */
    public List<String> getCatalogResourceTypes(Session jcrSession, String catalogBasePath) throws RepositoryException {
        if (!jcrSession.nodeExists(catalogBasePath)) {
            LOG.error("No such node `{}`, returning empty list", catalogBasePath);
            return null;
        }

        Node baseNode = jcrSession.getNode(catalogBasePath);
        NodeIterator iterator = baseNode.getNodes();

        List<String> resourceTypes = new ArrayList<String>();
        while (iterator.hasNext()) {
            Node componentNode = iterator.nextNode();

            // add to list without /apps/ prefix
            resourceTypes.add(
                componentNode.getPath().substring("/apps/".length())
            );
        }
        return resourceTypes;

    }

    /**
     * This method retrieves a list of resource types
     *
     * @return a list of resource types
     * @throws RepositoryException
     */
    public List<String> getCatalogResourceTypes() throws RepositoryException {

        return this.getCatalogResourceTypes(
                this.context.getCurrentNode().getSession(),
                this.getCatalogComponentBasePath(getUniqueCatalogIdentifier())
        );
    }


    /**
     * @return the path at which this catalog can be found
     */
    public String getCatalogComponentBasePath(String uuid) {
        return "/apps/lmzconfig/components/" + uuid;
    }

    /**
     * @return the uuid for this catalog
     */
    public String getUniqueCatalogIdentifier() {
        return this.context.getProperties().get(PARAM_UUID, (String) null);
    }
}
