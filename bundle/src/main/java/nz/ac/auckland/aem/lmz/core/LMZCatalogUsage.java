package nz.ac.auckland.aem.lmz.core;

import com.day.cq.wcm.api.Page;
import nz.ac.auckland.aem.lmz.dto.UsageLocation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marnix Cook
 */
public class LMZCatalogUsage {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LMZCatalogUsage.class);

    /**
     * Context
     */
    private ComponentBeanContext context;

    /**
     * Catalog helper
     */
    private LMZCatalogHelper catHelper;

    /**
     * Store the locations;
     */
    private List<UsageLocation> locations;

    /**
     * Initialize data-members
     *
     * @param context the content to use for data queries
     */
    public LMZCatalogUsage(ComponentBeanContext context) {
        this.context = context;
        this.catHelper = getHelperInstance(context);
    }

    /**
     * @return true if any of the components in the catalog are in use
     */
    public boolean isInUse() throws RepositoryException {
        return this.getLocations() != null && this.getLocations().size() > 0;
    }

    /**
     * @return all the uses of the component in the current environment
     */
    public List<UsageLocation> getLocations() throws RepositoryException {
        if (this.locations != null) {
            return this.locations;
        }

        return this.locations = getLocations(this.catHelper.getCatalogResourceTypes());
    }

    /**
     * Get all locations for all endpoints
     *
     * @param resourceTypes a list of resource types to go looking for.
     * @return is a list of all endpoints
     */
    protected List<UsageLocation> getLocations(List<String> resourceTypes) throws RepositoryException {
        List<UsageLocation> locations = new ArrayList<UsageLocation>();

        for (String endpoint : resourceTypes) {

            List<UsageLocation> usages = this.getLocationsForResourceType(endpoint);
            if (usages == null) {
                continue;
            }

            locations.addAll(usages);
        }

        return locations;
    }

    /**
     * Retrieve all uses for the resource type in <code>resourceType</code>
     *
     * @param resourceType is the resourceType to query for
     * @return
     */
    protected List<UsageLocation> getLocationsForResourceType(String resourceType) throws RepositoryException {
        NodeIterator resultIterator = getNodeIteratorFromQueryFor(resourceType);

        if (resultIterator == null) {
            return null;
        }

        List<UsageLocation> usages = new ArrayList<UsageLocation>();

        // iterate over all results
        while (resultIterator.hasNext()) {
            Node resultNode = resultIterator.nextNode();

            // careful, might be null.
            if (resultNode == null) {
                continue;
            }

            String nodePagePath = getPagePath(resultNode.getPath());
            Page page = this.context.getResourceResolver().getResource(nodePagePath).adaptTo(Page.class);

            usages.add(
                new UsageLocation(
                        resourceType,
                        page.getPath() + ".html",
                        getPageTitle(page)
                )
            );
        }

        // return result list
        return usages;
    }

    protected String getPageTitle(Page page) {
        if (StringUtils.isNotBlank(page.getNavigationTitle())) {
            return page.getNavigationTitle();
        } else if (StringUtils.isNotBlank(page.getPageTitle())) {
            return page.getPageTitle();
        } else if (StringUtils.isNotBlank(page.getTitle())) {
            return page.getTitle();
        } else {
            return page.getName();
        }
    }

    /**
     * @return the page path by splitting on jcr:content
     */
    protected String getPagePath(String path) {
        return path.split("/jcr:content")[0];
    }

    /**
     * This method executes a query and returns  node iterator of the result set. The query
     * will find all nodes that are using the component node
     *
     * @param resourceType the resource type
     * @return the node iterator of the result set
     *
     * @throws RepositoryException
     */
    protected NodeIterator getNodeIteratorFromQueryFor(String resourceType) throws RepositoryException {
        String queryText =
                "SELECT child.* FROM [nt:unstructured] as child " +
                "WHERE child.[sling:resourceType] = '" + resourceType + "'";

        QueryManager qMgr = this.context.getQueryManager();
        Query query = qMgr.createQuery(queryText, Query.JCR_SQL2);
        QueryResult qResult = query.execute();

        return qResult.getNodes();
    }

    /**
     * @return a fresh instance of the lmz catalog helper (extracted for testing purposes)
     */
    protected LMZCatalogHelper getHelperInstance(ComponentBeanContext context) {
        return new LMZCatalogHelper(context);
    }

}
