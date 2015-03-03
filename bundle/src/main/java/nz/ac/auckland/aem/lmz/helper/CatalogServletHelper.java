package nz.ac.auckland.aem.lmz.helper;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

/**
 * @author Marnix Cook
 *
 * The interface to the catalog servlet helper implementation
 */
public interface CatalogServletHelper {

    /**
     * Output an error message
     *
     * @param response is the response object to use
     * @param msg is the message to output
     */
    void outputError(SlingHttpServletResponse response, String msg);

    /**
     * @return true if it's being run on the authoring environment
     */
    boolean isAuthoringEnvironment();

    /**
     * @return the catalog name from the request
     */
    String getCatalogName(SlingHttpServletRequest request);

    /**
     * @return the redirect name from the request
     */
    String getRedirectTo(SlingHttpServletRequest request);

    /**
     * @return true if the request has the necessary parameters
     */
    boolean hasNecessaryRequestParameters(SlingHttpServletRequest request);
}
