package nz.ac.auckland.aem.lmz.replication;

import nz.ac.auckland.aem.lmz.services.CatalogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

/**
 * @author Marnix Cook
 *
 * This servlet is able to replicate a complete widget catalog from the authoring environment to the
 * publication environment.
 */
@SlingServlet(paths = "/bin/replicateWidgetCatalog", methods = "GET")
public class CatalogReplicationServlet extends SlingSafeMethodsServlet {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(CatalogReplicationServlet.class);
    public static final String PARAM_REDIRECT_TO = "redirectTo";
    public static final String PARAM_CATALOG_NAME = "catalog";

    /**
     * Settings service contains current runmodes
     */
    @Reference private SlingSettingsService settings;

    /**
     * Service that will helps us to replicate the catalog
     */
    @Reference private CatalogService catalog;


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
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {


        LOG.info("Entering 'replicateWidgetCatalog' servlet");

        // make sure we're on the authoring environment
        if (!isAuthoringEnvironment()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            LOG.info("Cannot run this on the publication environment, returning 404.");
            return;
        }

        // make sure the necesary request parameters are there
        if (!hasNecessaryRequestParameters(request)) {
            LOG.warn("The request parameters are insufficient, aborting.");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }

        String catalogName = this.getCatalogName(request);
        if (!catalog.exists(catalogName)) {
            outputError(response, "No such catalog found: " + catalogName);
            return;
        }

        catalog.replicate(catalogName);
        response.sendRedirect(getRedirectTo(request));
    }

    /**
     * Output an error message
     *
     * @param response is the response object to use
     * @param msg is the message to output
     */
    protected void outputError(SlingHttpServletResponse response, String msg) {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
            writer.println("<html><body><h1>Error</h1><p>" + msg + "</p></body></html>");
        }
        catch (IOException ioEx) {
            LOG.error("Could not write to response object", ioEx);
        }
    }

    protected String getCatalogName(SlingHttpServletRequest request) {
        return request.getParameter(PARAM_CATALOG_NAME);
    }

    protected String getRedirectTo(SlingHttpServletRequest request) {
        return request.getParameter(PARAM_REDIRECT_TO);
    }

    /**
     * @return true if it's being run on the authoring environment
     */
    protected boolean isAuthoringEnvironment() {
        Set<String> modes = settings.getRunModes();
        return modes.contains("author");
    }

    /**
     * @return true if the request has the necessary parameters
     */
    protected boolean hasNecessaryRequestParameters(SlingHttpServletRequest request) {
        return
            StringUtils.isNotBlank(getCatalogName(request)) &&
            StringUtils.isNotBlank(getRedirectTo(request));
    }
}
