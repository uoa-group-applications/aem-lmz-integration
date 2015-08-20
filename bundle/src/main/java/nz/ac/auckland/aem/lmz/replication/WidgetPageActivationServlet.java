package nz.ac.auckland.aem.lmz.replication;

import nz.ac.auckland.aem.lmz.helper.CatalogServletHelper;
import nz.ac.auckland.aem.lmz.services.CatalogService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: Marnix Cook <m.cook@auckland.ac.nz>
 *
 * This servlet is able to replicate all the pages that have components from
 * a specific widget catalog on them.
 */
@SlingServlet(paths = {"/bin/activateWidgetPagesForCatalog"}, methods = {"POST"})
public class WidgetPageActivationServlet extends SlingAllMethodsServlet {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(WidgetPageActivationServlet.class);

    @Reference private CatalogService catalog;
    @Reference private CatalogServletHelper servletHelper;


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

        LOG.info("Entering 'activateWidgetPagesForCatalog' servlet");

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

        catalog.replicatePages(catalogName);
        response.sendRedirect(servletHelper.getRedirectTo(request));
    }



}
