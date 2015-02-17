package nz.ac.auckland.aem.lmz.proxy;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * @author Marnix Cook
 *
 * Currently disabled because the dynamic backend configuration can be
 * handled in apache
 */
//@SlingServlet(methods = "GET", paths = "/bin/widgetProxy.do")
public class WidgetProxyServlet extends SlingSafeMethodsServlet {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(WidgetProxyServlet.class);

    /**
     * 3 second timeout
     */
    public static final int THREE_SECOND_TIMEOUT = 3000;

    /**
     * Header that tells us varnish is wanting us to do the proxying
     */
    public static final String HEADER_SECRET = "X-GoForIt";

    /**
     * The secret value we expect <code>md5('go-for-it');</code>
     */
    public static final String EXPECTED_SECRET = "e0b71ff25247987d1641804df1522470";

    private HttpClient client = new HttpClient();

    /**
     * Setup the client timeout
     */
    public WidgetProxyServlet() {
        HttpConnectionManagerParams parameters =
                client.getHttpConnectionManager().getParams();

        parameters.setConnectionTimeout(THREE_SECOND_TIMEOUT);

    }

    /**
     * GET method calls this function when the servlet is invoked
     *
     * @param request     is the request object
     * @param response    is the response object
     *
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        if (!hasGotProperSecrets(request)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // clone the request headers
        HttpMethod method = null;

        try {
            String url = reconstructUrl(request);
            LOG.info("Going to request URL: " + url);
            method = new GetMethod(url);

            // make the request headers correct
            cloneRequestHeaders(request, method);

            // execute proxy request
            client.executeMethod(method);

            // move returned headers into actual response
            cloneResponseHeaders(method, response);

            // clone status
            response.setStatus(method.getStatusCode());

            // clone output
            response.getOutputStream().write(method.getResponseBody());
        }
        finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * Determine whether the appropriate headers are here and contain the
     * correct values. If they don't, let's just pretend we're not here!
     *
     * @param request is the request to interrogate
     * @return true if proper information here.
     */
    protected boolean hasGotProperSecrets(SlingHttpServletRequest request) {
        return EXPECTED_SECRET.equals(request.getHeader(HEADER_SECRET));
    }

    /**
     * Move the proxied response headers into the actual response header
     *
     * @param method
     * @param response
     */
    protected void cloneResponseHeaders(HttpMethod method, SlingHttpServletResponse response) {
        for (Header responseHeader : method.getResponseHeaders()) {
            response.setHeader(responseHeader.getName(), responseHeader.getValue());
        }
    }

    /**
     * Copy all the request headers from the request into the method request headers
     *
     * @param request is the request header
     * @param method is the method to put the headers into
     */
    protected void cloneRequestHeaders(SlingHttpServletRequest request, HttpMethod method) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();

            // downstream doesn't need to know about the secret key
            if (name.equals(HEADER_SECRET)) {
                continue;
            }

            method.setRequestHeader(name, request.getHeader(name));
        }
    }

    private String reconstructUrl(SlingHttpServletRequest request) {
        return
            request.getParameter("src") + "view" +
            "?version=" + request.getParameter("version") +
            "&configuration=" + request.getParameter("configuration");
    }
}
