package nz.ac.auckland.aem.lmz.replication;

import com.day.cq.replication.ReplicationException;
import nz.ac.auckland.aem.lmz.helper.CatalogServletHelper;
import nz.ac.auckland.aem.lmz.services.CatalogService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Marnix Cook
 */
@SlingServlet(
    paths = {"/bin/deleteWidgetCatalog"},
    methods = {"POST"}
)
public class CatalogDeleteServlet extends SlingAllMethodsServlet {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CatalogDeleteServlet.class);

    @Reference private CatalogServletHelper servlet;

    @Reference private CatalogService catalog;

    /**
     * Implementation of the GET request for deleting the catalog
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        if (!servlet.isAuthoringEnvironment()) {
            LOG.info("This servlet can only called on the authoring environment");
        }

        // make sure the necessary request parameters are there
        if (!servlet.hasNecessaryRequestParameters(request)) {
            LOG.warn("The request parameters are insufficient, aborting.");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }

        String catalogIdentifier = servlet.getCatalogName(request);
        if (!catalog.exists(catalogIdentifier)) {
            servlet.outputError(response, "No such catalog found: " + catalogIdentifier);
            return;
        }

        Session jcrSession = request.getResourceResolver().adaptTo(Session.class);

        try {
            if (isNodePathIdentifier(catalogIdentifier)) {
                deleteUnconfiguredCatalogComponent(catalogIdentifier, jcrSession);
            } else {
                deleteCatalogAndGeneratedComponents(catalogIdentifier, jcrSession);
            }
        }
        catch (RepositoryException rEx) {
            LOG.error("A repository exception occurred", rEx);
        }
        catch (ReplicationException rEx) {
            LOG.error("A replication exception occurred", rEx);
        }

        // redirect to thank you page
        response.sendRedirect(servlet.getRedirectTo(request));
    }

    /**
     * @return true if the catalog identifier is a node path (starts with '/')
     */
    protected boolean isNodePathIdentifier(String catalogIdentifier) {
        return catalogIdentifier.startsWith("/");
    }

    /**
     * This method completely deletes a catalog and its generated components and then
     * replicate deletes them on the publication server.
     *
     * @param catalogIdentifier is the uuid of the catalog to remove
     * @param jcrSession is the jcr session to talk to
     *
     * @throws RepositoryException
     * @throws ReplicationException
     */
    protected void deleteCatalogAndGeneratedComponents(String catalogIdentifier, Session jcrSession) throws RepositoryException, ReplicationException {
        Node catalogComponent = catalog.findCatalogComponentWithUuid(jcrSession, catalogIdentifier);
        LOG.info("Found catalog component on path: `{}`", catalogComponent.getPath());

        // remove from publish
        catalog.replicateRemove(catalogIdentifier);
        catalog.replicateRemovePath(jcrSession, catalogComponent.getPath());

        // remove catalog component from author
        catalogComponent.remove();

        // remove generated components from author
        jcrSession.getNode("/apps/lmzconfig/components/" + catalogIdentifier).remove();

        // save changes
        jcrSession.save();

    }

    /**
     * When a catalog isn't configured yet it's relatively simple to delete it. We'll just
     * look up the node and remove it. Before calling this method, make sure you checked that
     * the type was actually what we're expecting.
     *
     * @param nodePath is the path to the catalog we're deleting
     * @param jcrSession is the session to talk to.
     *
     * @throws RepositoryException
     */
    protected void deleteUnconfiguredCatalogComponent(String nodePath, Session jcrSession) throws RepositoryException {
        LOG.info("Deleting catalog node at `{}`", nodePath);
        jcrSession.getNode(nodePath).remove();
        jcrSession.save();
    }

}
