<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>
<%@ page import="nz.ac.auckland.aem.lmz.core.ComponentBeanContext" %>
<%!

    /**
     * This function html4-escapes the provided string/object.
     * @param body The string/object to encode.
     * @return A properly encoded html4 string.
     * @see StringEscapeUtils#escapeHtml4
     */
    protected String escapeBody(Object body) {
        if (body == null) {
            return null;
        }

        return StringEscapeUtils.escapeHtml4(body.toString());
    }

%><%
    org.apache.sling.api.scripting.SlingBindings _bindings = (org.apache.sling.api.scripting.SlingBindings)  pageContext.getAttribute("bindings");
    com.day.cq.wcm.api.components.Component _component = (com.day.cq.wcm.api.components.Component)  pageContext.getAttribute("component");
    com.day.cq.wcm.api.components.ComponentContext _componentContext = (com.day.cq.wcm.api.components.ComponentContext)  pageContext.getAttribute("componentContext");
    com.day.cq.wcm.api.designer.Design _currentDesign = (com.day.cq.wcm.api.designer.Design)  pageContext.getAttribute("currentDesign");
    javax.jcr.Node _currentNode = (javax.jcr.Node)  pageContext.getAttribute("currentNode");
    com.day.cq.wcm.api.Page _currentPage = (com.day.cq.wcm.api.Page)  pageContext.getAttribute("currentPage");
    com.day.cq.wcm.api.designer.Style _currentStyle = (com.day.cq.wcm.api.designer.Style)  pageContext.getAttribute("currentStyle");
    com.day.cq.wcm.api.designer.Designer _designer = (com.day.cq.wcm.api.designer.Designer)  pageContext.getAttribute("designer");
    com.day.cq.wcm.api.components.EditContext _editContext = (com.day.cq.wcm.api.components.EditContext)  pageContext.getAttribute("editContext");
    org.slf4j.Logger _log = (org.slf4j.Logger)  pageContext.getAttribute("log");
    com.day.cq.wcm.api.PageManager _pageManager = (com.day.cq.wcm.api.PageManager)  pageContext.getAttribute("pageManager");
    com.day.cq.commons.inherit.InheritanceValueMap _pageProperties = (com.day.cq.commons.inherit.InheritanceValueMap)  pageContext.getAttribute("pageProperties");
    org.apache.sling.api.resource.ValueMap _properties = (org.apache.sling.api.resource.ValueMap)  pageContext.getAttribute("properties");
    org.apache.sling.api.resource.Resource _resource = (org.apache.sling.api.resource.Resource)  pageContext.getAttribute("resource");
    com.day.cq.wcm.api.designer.Design _resourceDesign = (com.day.cq.wcm.api.designer.Design)  pageContext.getAttribute("resourceDesign");
    com.day.cq.wcm.api.Page _resourcePage = (com.day.cq.wcm.api.Page)  pageContext.getAttribute("resourcePage");
    org.apache.sling.api.resource.ResourceResolver _resourceResolver = (org.apache.sling.api.resource.ResourceResolver)  pageContext.getAttribute("resourceResolver");
    org.apache.sling.api.scripting.SlingScriptHelper _sling = (org.apache.sling.api.scripting.SlingScriptHelper)  pageContext.getAttribute("sling");
    org.apache.sling.api.SlingHttpServletRequest _slingRequest = (org.apache.sling.api.SlingHttpServletRequest)  pageContext.getAttribute("slingRequest");
    org.apache.sling.api.SlingHttpServletResponse _slingResponse = (org.apache.sling.api.SlingHttpServletResponse)  pageContext.getAttribute("slingResponse");
    com.adobe.granite.xss.XSSAPI _xssAPI = (com.adobe.granite.xss.XSSAPI)  pageContext.getAttribute("xssAPI");
    com.day.cq.tagging.TagManager _tagManager = _resourceResolver.adaptTo(com.day.cq.tagging.TagManager.class);

    ComponentBeanContext _beanContext = new ComponentBeanContext(request, response, pageContext);
%>