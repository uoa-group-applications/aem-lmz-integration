package nz.ac.auckland.aem.lmz.replication;

import nz.ac.auckland.aem.lmz.helper.CatalogServletHelperImpl;
import nz.ac.auckland.aem.lmz.services.CatalogService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Marnix Cook
 *
 * This servlet is able to replicate a complete widget catalog from the authoring environment to the
 * publication environment.
 */
@SlingServlet(paths = "/bin/replicateWidgetCatalog", methods = "POST")
public class CatalogReplicationServlet extends SlingAllMethodsServlet {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(CatalogReplicationServlet.class);


    /**
     * Service that will helps us to replicate the catalog
     */
    @Reference private CatalogService catalog;

    @Reference private CatalogServletHelperImpl servletHelper;

    /**
     * Implementation of this servlet. It will go ahead and publish a certain widget catalog from
     * the authoring environment to the publication environment.
     *
     * @param request is the request
     * @param response is the response
     *
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        LOG.info("Entering 'replicateWidgetCatalog' servlet");

        // make sure we're on the authoring environment
        if (!servletHelper.isAuthoringEnvironment()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            LOG.info("Cannot run this on the publication environment, returning 404.");
            return;
        }

        // make sure the necesary request parameters are there
        if (!servletHelper.hasNecessaryRequestParameters(request)) {
            LOG.warn("The request parameters are insufficient, aborting.");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }

        String catalogName = servletHelper.getCatalogName(request);
        if (!catalog.exists(catalogName)) {
            servletHelper.outputError(response, "No such catalog found: " + catalogName);
            return;
        }

        catalog.replicate(catalogName);
        response.sendRedirect(servletHelper.getRedirectTo(request));
    }


}
