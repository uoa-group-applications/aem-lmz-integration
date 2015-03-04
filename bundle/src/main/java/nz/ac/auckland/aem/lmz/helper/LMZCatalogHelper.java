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
     * @return a list of resource types
     * @throws RepositoryException
     */
    public List<String> getCatalogResourceTypes() throws RepositoryException {
        Session jcrSession = this.context.getCurrentNode().getSession();
        if (!jcrSession.nodeExists(getCatalogComponentBasePath())) {
            LOG.error("No such node `{}`, returning empty list", getCatalogComponentBasePath());
            return null;
        }

        Node baseNode = jcrSession.getNode(getCatalogComponentBasePath());
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
     * @return the path at which this catalog can be found
     */
    protected String getCatalogComponentBasePath() {
        return "/apps/lmzconfig/components/" + getUniqueCatalogIdentifier();
    }

    /**
     * @return the uuid for this catalog
     */
    public String getUniqueCatalogIdentifier() {
        return this.context.getProperties().get(PARAM_UUID, (String) null);
    }
}
