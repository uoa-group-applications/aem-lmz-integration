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
    paths = {"/bin/deleteWidgetCatalog.do"},
    methods = {"GET"}
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

        // make sure the necesary request parameters are there
        if (!servlet.hasNecessaryRequestParameters(request)) {
            LOG.warn("The request parameters are insufficient, aborting.");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }

        String catalogName = servlet.getCatalogName(request);
        if (!catalog.exists(catalogName)) {
            servlet.outputError(response, "No such catalog found: " + catalogName);
            return;
        }

        Session jcrSession = request.getResourceResolver().adaptTo(Session.class);

        try {
            Node catalogComponent = catalog.findCatalogComponentWithUuid(jcrSession, catalogName);
            LOG.info("Found catalog component on path: `{}`", catalogComponent.getPath());

            // remove catalog component from author
            catalogComponent.remove();

            // remove generated components from author
            jcrSession.getNode("/apps/lmzconfig/components/" + catalogName).remove();

            // save changes
            jcrSession.save();

            // remove from publish
            catalog.replicateRemove(catalogName);
            catalog.replicateRemovePath(jcrSession, catalogComponent.getPath());
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

    protected String getCatalogName(SlingHttpServletRequest request) {
        return request.getParameter(CatalogConstants.PARAM_CATALOG_NAME);
    }
}
