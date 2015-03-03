package nz.ac.auckland.aem.lmz.services;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import nz.ac.auckland.aem.lmz.helper.LMZCatalogHelper;
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
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
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
            replicateRemove(catalogName);

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
     * Remove the catalog on the publication environment through replicate remove call
     *
     * @param catalogName is the catalog to remove
     * @throws ReplicationException
     * @throws RepositoryException
     */
    public void replicateRemove(String catalogName) throws ReplicationException, RepositoryException {
        Resource catalog = getCatalogResource(catalogName);
        if (catalog == null) {
            LOG.warn("Cannot replicate a catalog that does not exist, aborting.");
            return;
        }

        replicateRemovePath(catalog.adaptTo(Node.class).getSession(), catalog.getPath());
    }

    /**
     * Remove replicate the path from the publication servers
     *
     * @param session the JCR session to operate on
     * @param path is the path to remove
     * @throws ReplicationException
     * @throws RepositoryException
     */
    @Override
    public void replicateRemovePath(Session session, String path) throws ReplicationException, RepositoryException {
        replicator.replicate(session, ReplicationActionType.DELETE, path);
    }

    /**
     * Get the catalog component node for the catalog with the identifier <code>uuid</code>
     *
     * @param session is the session
     * @param uuid is the uuid to go looking for
     * @return the node, or null when not found,
     * @throws RepositoryException
     */
    @Override
    public Node findCatalogComponentWithUuid(Session session, String uuid) throws RepositoryException {
        QueryManager qMgr = session.getWorkspace().getQueryManager();
        Query query = qMgr.createQuery(
                "SELECT child.* FROM [nt:unstructured] as child WHERE child.[catalog-uuid] = '" + uuid + "'",
                Query.JCR_SQL2
        );

        QueryResult qResult = query.execute();
        NodeIterator nodeIterator = qResult.getNodes();

        // no results?
        if (nodeIterator.getSize() == 0L) {
            LOG.error("There is no node with this uuid, aborting");
            return null;
        }

        // more than one result?
        if (nodeIterator.getSize() > 1L) {
            LOG.error("There were multiple catalogs with the same UUID `{}` (shouldn't happen), aborting", uuid);
            return null;
        }

        return nodeIterator.nextNode();
    }

    /**
     * Get all the child resources of the current resource and return a list of resources
     *
     * @param node is the node to start looking at
     * @return a list of all the nodes from this point forward
     */
    protected List<Node> getRecursiveChildren(Node node) {
        List<Node> list = new ArrayList<Node>();

        // don't replicate policy nodes, they'll kill the replication agent.
        if (isPolicyNode(node)) {
            return list;
        }

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
     * @return true if this is a policy node
     */
    protected boolean isPolicyNode(Node node) {
        try {
            return "rep:policy".equals(node.getName());
        } catch (RepositoryException repEx) {
            LOG.error("Couldn't check node name", repEx);
        }
        return false;
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
