package nz.ac.auckland.aem.lmz.services;

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


}
