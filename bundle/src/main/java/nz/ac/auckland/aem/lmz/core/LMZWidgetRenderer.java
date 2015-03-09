package nz.ac.auckland.aem.lmz.core;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author gregkw
 * @author Marnix Cook
 *
 * The widget renderer is able to generate an instance of the WidgetRenderContext object
 * that contains relevant information regarding the configuration query strings. It may
 * contain information of the HTML that is be renderered, depending on the run mode.
 */
public class LMZWidgetRenderer {

    public static final String GROUP_ADMIN = "administrators";
    private static Logger logger = LoggerFactory.getLogger(LMZWidgetRenderer.class);

    public static final String METADATA_URL = "md_url";
    public static final String METADATA_VERSION = "md_version";

    /**
     * Bean context
     */
    private ComponentBeanContext context;

    /**
     * Initialize data-members
     *
     * @param context is the bean context
     */
    public LMZWidgetRenderer(ComponentBeanContext context) {
        this.context = context;
    }

    public boolean isAdministrator() throws RepositoryException {
        UserManager usrMgr = this.context.getResourceResolver().adaptTo(UserManager.class);
        Session session = this.context.getCurrentNode().getSession();
        Authorizable auth = usrMgr.getAuthorizable(session.getUserID());

        Iterator<Group> authGroups = auth.memberOf();
        while (authGroups.hasNext()) {
            Group grp = authGroups.next();
            if (grp.getID().equals(GROUP_ADMIN)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return a string->string map of all the configuration properties that have been added to
     * the component by the author.
     */
    private Map<String, String> getConfigurationMap() {
        Map<String, String> confMap = new HashMap<String, String>();

        // iterate over all properties
        for (Map.Entry<String, Object> entry : this.context.getProperties().entrySet()) {

            // valid configuration property?
            if (isConfigurationProperty(entry.getKey())) {
                confMap.put(entry.getKey(), (String) entry.getValue());
            }
        }
        return confMap;
    }


    /**
     * @return true if the property name does not have certain prefixes
     */
    protected boolean isConfigurationProperty(String propertyName) {
        return
            !propertyName.startsWith("jcr:") && !propertyName.startsWith("sling:") &&
            !propertyName.startsWith("md_") && !propertyName.startsWith("cq:");
    }


    /**
     * Setup a rendering context by creating the object and setup the view request url
     *
     * @return the widget render context, or null when something went wrong.
     */
    public WidgetRenderContext getRenderContext() {

        if (StringUtils.isBlank(this.getMetadataEndpointUrl())) {
            return null;
        }

        // setup the context
        WidgetRenderContext rContext =
                new WidgetRenderContext(
                        this.getMetadataEndpointUrl(),
                        this.getConfigurationMap(),
                        this.getWidgetVersionProperty()
                    );

        // go get the widget content if we're looking at the authoring server
        if (this.context.isAuthor()) {
            rContext.fetchWidgetContent();
        }

        return rContext;
    }

    public boolean isEsiInclude() {
        return this.context.isPublish();
    }

    /**
     * @return true if the component has been set to maintenance mode
     */
    public boolean isDisabled() {
        return this.context.getComponent().getProperties().get("maintenanceMode", false);
    }

    /**
     * @return the maintenance message
     */
    public String getMaintenanceMessage() {
        return this.context.getComponent().getProperties().get("maintenanceMessage", (String) null);
    }

    /**
     * @return the metadata end point location
     */
    public String getMetadataEndpointUrl() {
        return context.getProperties().get(METADATA_URL, String.class);
    }


    /**
     * @return the widget version property
     */
    protected String getWidgetVersionProperty() {
        return this.context.getProperties().get(METADATA_VERSION, "");
    }

}
