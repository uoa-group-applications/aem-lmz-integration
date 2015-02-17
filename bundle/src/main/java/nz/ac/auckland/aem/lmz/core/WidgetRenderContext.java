package nz.ac.auckland.aem.lmz.core;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author Marnix Cook
 */
public class WidgetRenderContext {

    /**
     * Http Client
     */
    private HttpClient client = new HttpClient();

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(WidgetRenderContext.class);

    /**
     * Contains HTML fetched from widget view endpoint for on author
     */
    private String html;

    /**
     * Is the location at which the widget lives
     */
    private String widgetUrl;

    /**
     * Is the location of the view endpoint in the widget
     */
    private String viewUrl;

    /**
     * The version and configuration parameter query string
     */
    private String viewQueryString;

    /**
     * The response code of the http response for retrieving the widget content
     */
    private int responseStatusCode;

    /**
     * Lmz base
     */
    private String lmzBase;

    /**
     * Initialize data-members
     *
     * @param widgetUrl is the widget url
     */
    public WidgetRenderContext(String widgetUrl, Map<String, String> confMap, String version) {
        this.widgetUrl = widgetUrl;
        this.viewQueryString = buildViewQueryString(confMap, version);
    }

    /**
     * @return true if the content was fetched
     */
    public boolean isFetchedContent() {
        return this.html != null;
    }

    /**
     * Fetch the widget view HTML directly from the widget server.
     *
     * @return true if the widget was fetched successfully
     */
    protected boolean fetchWidgetContent() {

        HttpMethod method = null;
        try {
            LOG.info("Requesting URL " + getViewUrl());

            // get widget info
            method = new GetMethod(getViewUrl() + getViewQueryString());
            method.setFollowRedirects(true);
            client.executeMethod(method);

            // do some logging
            for (Header header : method.getResponseHeaders()) {
                LOG.info("response Header: " + header.toExternalForm());
            }

            // get status code
            StatusLine status = method.getStatusLine();
            this.responseStatusCode = status.getStatusCode();

            LOG.info("statusResponse: valid? " + isValidResponse() + " : code: " + getResponseStatusCode());

            // not a valid response? log it.
            String responseBody = method.getResponseBodyAsString();
            if (!isValidResponse()) {
                LOG.info("Not a valid response, server response body: " + responseBody);
                return false;
            }

            this.html = responseBody;

            return true;
        } catch (IOException ioEx) {
            LOG.error("An IO Exception occured", ioEx);
        } catch (Exception ex) {
            LOG.error("An exception occurred", ex);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }

        return false;
    }

    /**
     * Build a query parameter string based on the configuration that has been
     * setup for the widget.
     *
     * @return the full request url.
     */
    protected String buildViewQueryString(Map<String, String> confMap, String version) {
        String queryString = "";

        // add the queryParameters
        queryString += "?version=" + version;
        queryString += "&configuration=" + jsonifyAndEncodeConfiguration(confMap);

        return queryString;
    }

    /**
     * @return the view url for the speciifed `widgetUrl`.
     */
    protected String getViewUrl() {
        if (viewUrl == null) {
            viewUrl = widgetUrl;

            if (!viewUrl.endsWith("/")) {
                viewUrl += "/";
            }
            if (!viewUrl.endsWith("view")) {
                viewUrl += "view";
            }

        }
        return viewUrl;
    }


    /**
     * Turn the configuration map into a JSON string and base64 encode it. This
     * will be the `configuration` parameter.
     *
     * @param confMap is the configuration map to convert
     * @return a base64 encoded json string.
     */
    protected String jsonifyAndEncodeConfiguration(Map<String, String> confMap) {
        JSONObject configParams = new JSONObject(confMap);
        String configString = configParams.toString();
        LOG.info("config params: " + configString);
        Base64 b64Enc = new Base64();
        return b64Enc.encodeToString(configString.getBytes());
    }


    /**
     * @return the base of the application
     */
    public String getLmzBase() {
        if (this.lmzBase == null) {
            try {
                URI uri = new URI(widgetUrl);
                String base = uri.getScheme() + "://" + uri.getHost();
                if (uri.getPort() > 0 && uri.getPort() != 443 && uri.getPort() != 80) {
                    base += ":" + uri.getPort();
                }

                return (this.lmzBase = base);
            } catch (URISyntaxException useEx) {
                LOG.error("Something went wrong while parsing the URI", useEx);
            }
        }
        return this.lmzBase;
    }

    /**
     * @return the context path of the widget
     */
    public String getUrlContextPath() {
        try {
            URI uri = new URI(widgetUrl);
            return uri.getPath();
        } catch (URISyntaxException useEx) {
            LOG.error("Something went wrong while parsing the URI", useEx);
        }
        return null;
    }


    public String getHtml() {
        return html;
    }


    public boolean isValidResponse() {
        return (200 <= responseStatusCode) && (300 > responseStatusCode);
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }


    public String getWidgetUrl() {
        return widgetUrl;
    }

    public String getViewQueryString() {
        return viewQueryString;
    }

}
