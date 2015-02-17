package nz.ac.auckland.aem.lmz.core;

import com.adobe.granite.xss.XSSAPI;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import java.util.Set;

/**
 * @author Marnix Cook
 *
 * This class is a simple interface between the objects that are defined in the JSPs
 * by the <cq:defineObjects/> taglibrary and the
 */
public class ComponentBeanContext {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private SlingBindings bindings;
    private Component component;
    private com.day.cq.wcm.api.components.ComponentContext componentContext;
    private Design currentDesign;
    private Node currentNode;
    private Page currentPage;
    private Style currentStyle;
    private Designer designer;
    private EditContext editContext;
    private Logger log;
    private PageManager pageManager;
    private InheritanceValueMap pageProperties;
    private ValueMap properties;
    private Resource resource;
    private Design resourceDesign;
    private Page resourcePage;
    private ResourceResolver resourceResolver;
    private SlingScriptHelper sling;
    private SlingHttpServletRequest slingRequest;
    private SlingHttpServletResponse slingResponse;
    private XSSAPI xssAPI;
    private TagManager tagManager;

    /**
     * Mass-initialize all the data members using the page context that is passed
     * into the constructor.
     *
     * @param pageContext is queried for all the different objects
     */
    public ComponentBeanContext(HttpServletRequest request, HttpServletResponse response, PageContext pageContext) {
        bindings = (SlingBindings)  pageContext.getAttribute("bindings");
        component = (Component)  pageContext.getAttribute("component");
        componentContext = (com.day.cq.wcm.api.components.ComponentContext)  pageContext.getAttribute("componentContext");
        currentDesign = (Design)  pageContext.getAttribute("currentDesign");
        currentNode = (Node)  pageContext.getAttribute("currentNode");
        currentPage = (Page)  pageContext.getAttribute("currentPage");
        currentStyle = (Style)  pageContext.getAttribute("currentStyle");
        designer = (Designer)  pageContext.getAttribute("designer");
        editContext = (EditContext)  pageContext.getAttribute("editContext");
        log = (Logger)  pageContext.getAttribute("log");
        pageManager = (PageManager)  pageContext.getAttribute("pageManager");
        pageProperties = (InheritanceValueMap)  pageContext.getAttribute("pageProperties");
        properties = (ValueMap)  pageContext.getAttribute("properties");
        resource = (Resource)  pageContext.getAttribute("resource");
        resourceDesign = (Design)  pageContext.getAttribute("resourceDesign");
        resourcePage = (Page)  pageContext.getAttribute("resourcePage");
        resourceResolver = (ResourceResolver)  pageContext.getAttribute("resourceResolver");
        sling = (SlingScriptHelper)  pageContext.getAttribute("sling");
        slingRequest = (SlingHttpServletRequest)  pageContext.getAttribute("slingRequest");
        slingResponse = (SlingHttpServletResponse)  pageContext.getAttribute("slingResponse");
        xssAPI = (XSSAPI)  pageContext.getAttribute("xssAPI");

        tagManager = resourceResolver.adaptTo(TagManager.class);
        this.request = request;
        this.response = response;
    }


    public String mappedUrl(String path) {
        return resourceResolver.map(path);
    }

    /**
     * This function will encode a SQL parameter by replacing the '
     * and % with their correct encoded counter parts
     *
     * @param jcrSql is the variable to encode
     * @return is the encoded variable
     */
    public String escapeJcrSql(String jcrSql) {
        if (jcrSql == null) {
            return null;
        }
        return jcrSql.replace("'", "''").replace("%", "%%");
    }


    public QueryManager getQueryManager() throws RepositoryException {
        return getCurrentNode().getSession().getWorkspace().getQueryManager();
    }

    public SlingBindings getBindings() {
        return bindings;
    }

    public Component getComponent() {
        return component;
    }

    public com.day.cq.wcm.api.components.ComponentContext getComponentContext() {
        return componentContext;
    }

    public Design getCurrentDesign() {
        return currentDesign;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public Page getCurrentPage() {
        return currentPage;
    }

    public Style getCurrentStyle() {
        return currentStyle;
    }

    public Designer getDesigner() {
        return designer;
    }

    public EditContext getEditContext() {
        return editContext;
    }

    public Logger getLog() {
        return log;
    }

    public PageManager getPageManager() {
        return pageManager;
    }

    public InheritanceValueMap getPageProperties() {
        return pageProperties;
    }

    public ValueMap getProperties() {
        return properties;
    }

    public Resource getResource() {
        return resource;
    }

    public Design getResourceDesign() {
        return resourceDesign;
    }

    public Page getResourcePage() {
        return resourcePage;
    }

    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public SlingScriptHelper getSling() {
        return sling;
    }

    public SlingHttpServletRequest getSlingRequest() {
        return slingRequest;
    }

    public SlingHttpServletResponse getSlingResponse() {
        return slingResponse;
    }

    public XSSAPI getXssAPI() {
        return xssAPI;
    }

    public TagManager getTagManager() {
        return tagManager;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public boolean isAuthor() {
        SlingSettingsService settingsService = getSling().getService(SlingSettingsService.class);
        Set<String> modes = settingsService.getRunModes();
        return modes.contains("author");
    }

    public boolean isPublish() {
        return !isAuthor();
    }
}
