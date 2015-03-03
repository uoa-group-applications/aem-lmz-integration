package nz.ac.auckland.aem.lmz.services;

import com.day.cq.replication.ReplicationException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * @author Marnix Cook
 *
 * This is the interface to the catalog service implementation.
 */
public interface CatalogService {

    /**
     * This method determines whether the catalog with <code>catalogName</code>
     * currently exists on the server. If it does, it returns true.
     *
     * @param catalogName is the catalog name
     * @return true if catalog exists
     */
    boolean exists(String catalogName);

    /**
     * This method initiates replication of the catalog to the publication servers
     *
     * @param catalogName is the catalog to replicate
     */
    void replicate(String catalogName);

    /**
     * Remove the catalog on the publication environment through replicate remove call
     *
     * @param catalogName is the catalog to remove
     * @throws ReplicationException
     * @throws RepositoryException
     */
    void replicateRemove(String catalogName) throws ReplicationException, RepositoryException;

    /**
     * Replicate remove node on this path
     *
     * @param path is the path to remove
     * @param session the JCR session to operate on
     */
    void replicateRemovePath(Session session, String path) throws ReplicationException, RepositoryException;

    /**
     * Returns the JCR node that belongs to the category component with uuid <code>uuid</code>
     *
     * @param uuid is the uuid to go looking for
     * @return the node that belongs to it.
     */
    Node findCatalogComponentWithUuid(Session session, String uuid) throws RepositoryException;
}
