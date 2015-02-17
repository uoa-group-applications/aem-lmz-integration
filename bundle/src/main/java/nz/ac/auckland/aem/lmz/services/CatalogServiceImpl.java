package nz.ac.auckland.aem.lmz.services;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marnix Cook
 *
 * The implementation of the catalog service interface. The methods take care of
 * the replication of the widget catalogs.
 */
@Service
@Component(immediate = true)
public class CatalogServiceImpl implements CatalogService {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CatalogServiceImpl.class);

    /**
     * Resolver factory can be queried for resources
     */
    @Reference private ResourceResolverFactory resourceResolverFactory;

    /**
     * Resource resolver
     */
    private ResourceResolver resourceResolver;

    /**
     * Replicator is a service that allows us to replicate paths
     */
    @Reference private Replicator replicator;

    /**
     * @return true if the catalog exists
     */
    @Override
    public boolean exists(String catalogName) {
        return getCatalogResource(catalogName) != null;
    }

    /**
     * Replicate the catalog if it exists
     *
     * @param catalogName is the catalog to replicate
     */
    @Override
    public void replicate(String catalogName) {
        Resource catalog = getCatalogResource(catalogName);
        if (catalog == null) {
            LOG.warn("Cannot replicate a catalog that does not exist, aborting.");
            return;
        }

        // get all the nodes we need to replicate
        List<Node> allNodes = this.getRecursiveChildren(catalog.adaptTo(Node.class));

        try {
            // first we remove the existing components
            replicator.replicate(catalog.adaptTo(Node.class).getSession(), ReplicationActionType.DELETE, catalog.getPath());

            // then we reactivate them
            for (Node node : allNodes) {
                LOG.info("Replicating catalog element at: " + node.getPath());

                replicator.replicate(
                        catalog.adaptTo(Node.class).getSession(),
                        ReplicationActionType.ACTIVATE,
                        node.getPath()
                );
            }
        }
        catch (ReplicationException repEx) {
            LOG.error("An exception during replication happened", repEx);
        }
        catch (RepositoryException repEx) {
            LOG.error("An exception during repository access happened", repEx);
        }
    }

    /**
     * Get all the child resources of the current resource and return a list of resources
     *
     * @param node is the node to start looking at
     * @return a list of all the nodes from this point forward
     */
    protected List<Node> getRecursiveChildren(Node node) {
        List<Node> list = new ArrayList<Node>();

        list.add(node);

        try {
            // has kiddos? iterator over them
            if (node.hasNodes()) {
                NodeIterator nIt = node.getNodes();
                while (nIt.hasNext()) {
                    Node childNode = nIt.nextNode();

                    // add recursive results to current list.
                    list.addAll(getRecursiveChildren(childNode));
                }
            }
        }
        catch (Exception ex) {
            LOG.error("An error occurred when retrieving child nodes", ex);
        }

        return list;
    }

    /**
     * @return the resource for a specific catalog name
     */
    protected Resource getCatalogResource(String catalogName) {
        return getResourceResolver().getResource("/apps/lmzconfig/components/" + catalogName);
    }


    /**
     * @return the resource resolver instance
     */
    protected ResourceResolver getResourceResolver() {
        if (this.resourceResolver == null) {
            try {
                this.resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            } catch (Exception ex) {
                LOG.error("Could not retrieve the resource resolver", ex);
                return null;
            }
        }
        return this.resourceResolver;
    }
}
