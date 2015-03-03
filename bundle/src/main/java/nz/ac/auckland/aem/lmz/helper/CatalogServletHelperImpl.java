package nz.ac.auckland.aem.lmz.helper;

import nz.ac.auckland.aem.lmz.replication.CatalogConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

/**
 * @author Marnix Cook
 *
 * This class contains helper methods for the servlets in this package.
 */
@Service
@Component(immediate = true)
public class CatalogServletHelperImpl implements CatalogServletHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogServletHelperImpl.class);

    /**
     * Settings service contains current runmodes
     */
    @Reference private SlingSettingsService settings;

    /**
     * Output an error message
     *
     * @param response is the response object to use
     * @param msg is the message to output
     */
    public void outputError(SlingHttpServletResponse response, String msg) {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
            writer.println("<html><body><h1>Error</h1><p>" + msg + "</p></body></html>");
        }
        catch (IOException ioEx) {
            LOG.error("Could not write to response object", ioEx);
        }
    }

    /**
     * @return true if it's being run on the authoring environment
     */
    public boolean isAuthoringEnvironment() {
        Set<String> modes = settings.getRunModes();
        return modes.contains("author");
    }

    /**
     * @return the catalog name from the request
     */
    public String getCatalogName(SlingHttpServletRequest request) {
        return request.getParameter(CatalogConstants.PARAM_CATALOG_NAME);
    }

    /**
     * @return the redirect name from the request
     */
    public String getRedirectTo(SlingHttpServletRequest request) {
        return request.getParameter(CatalogConstants.PARAM_REDIRECT_TO);
    }

    /**
     * @return true if the request has the necessary parameters
     */
    public boolean hasNecessaryRequestParameters(SlingHttpServletRequest request) {
        return
            StringUtils.isNotBlank(getCatalogName(request)) &&
            StringUtils.isNotBlank(getRedirectTo(request));
    }

}
