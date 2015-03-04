<%--

  LMZ Widget component.

--%>
<%@ include file="/apps/central/global/global.jsp" %>
<%@ include file="/apps/lmz-integration/global/contextObjects.jsp" %>
<%@ include file="/apps/central/global/resolver.jsp" %>
<%@ page import="nz.ac.auckland.aem.lmz.core.LMZWidgetComponentCreator" %>
<%@ page import="nz.ac.auckland.lmzwidget.configuration.model.WidgetConfiguration" %>
<%@ page import="org.apache.commons.lang3.ArrayUtils" %>
<%@ page import="java.util.Map" %>
<%@ page import="nz.ac.auckland.aem.lmz.core.LMZCatalogUsage" %>
<%@ page import="nz.ac.auckland.aem.lmz.helper.LMZCatalogHelper" %>

<c:set var="usage" value="<%= new LMZCatalogUsage(_beanContext) %>" />
<%

    LMZWidgetComponentCreator creator = new LMZWidgetComponentCreator(_beanContext);
    nz.ac.auckland.aem.lmz.helper.LMZCatalogHelper categoryHelper = new nz.ac.auckland.aem.lmz.helper.LMZCatalogHelper(_beanContext);

    boolean firstComponent = creator.isFirstCatalogComponent();

    if (firstComponent) {
        if (creator.shouldSynchronize()) {
            request.setAttribute("updateResults", creator.updateAllWidgets());
        } else {
            request.setAttribute("noSynch", true);
        }
    }

    request.setAttribute("maintenanceMode", creator.isInMaintenanceMode());
    request.setAttribute("replicated", "true".equals(request.getParameter("done")));
    request.setAttribute("catalogName", creator.getCatalogName());
    request.setAttribute("uniqueCatalogId", categoryHelper.getUniqueCatalogIdentifier());
    request.setAttribute("notFirstComponent", !firstComponent);
%>
<div id="<%= _currentNode.getName() %>">
    <c:choose>
        <c:when test="${notFirstComponent}">
            <div class="warning-block">
                <p>
                    Only one LMZ widget catalog component per page is allowed. Please remove
                    this component.
                </p><br/>
                <form action="/bin/deleteWidgetCatalog" method="post">
                    <input type="hidden" name="catalog" value="<%= _currentNode.getPath() %>" />
                    <input type="hidden" name="redirectTo" value="<%= mappedUrl(_currentPage.getPath() + ".html")%>" />
                    <button type="submit">Delete Catalog</button>
                </form>
            </div>
        </c:when>
        <c:when test="${noSynch}">
            <p class="cq-info">
                This page will not display its information because it is viewed on the publication
                server.
            </p>
        </c:when>

        <c:when test="${! empty updateResults }">

            <div id="pageBanner">
                <div class="txt">
                    <h1 class="b4">Catalog definitions for `${catalogName}`</h1>
                    <div class="b9">
                        <p>
                            Below you can find a status overview of the widgets that have been
                            defined to be part of the catalog. Loading this page on the authoring
                            server will make synchronize widget versions across the AEM platform.
                        </p>
                    </div>
                </div>
            </div>

            <c:if test="${maintenanceMode}">
                <p class="warning-block">
                    The catalog has been set to maintenance mode. Make sure to replicate the catalog
                    if you need this to be true in the publication environment as well.
                </p>
            </c:if>


            <table>
                <tr>
                    <th>Endpoint location</th>
                    <th>Status</th>
                </tr>
                <c:forEach items="${updateResults}" var="result" varStatus="status">
                    <tr class="${status.index % 2 == 1 ? 'odd' : 'even'}">
                        <td>
                            <pre>${result.key}</pre>
                            <c:if test="${! empty result.value}">
                                <span class="config">(<a target="_blank" href="${result.key}/configuration">config</a>)</span>
                            </c:if>
                        </td>
                        <td class="${empty result.value ? "fail" : "success"}">
                            <c:choose>
                                <c:when test="${empty result.value}">
                                    Fail.
                                </c:when>
                                <c:otherwise>
                                    Success!
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <tr class="replicate">
                    <td colspan="2" align="right">
                        <form action="/bin/replicateWidgetCatalog" method="post">
                            <input type="hidden" name="catalog" value="${uniqueCatalogId}" />
                            <input type="hidden" name="redirectTo" value="<%= mappedUrl(_currentPage.getPath() + ".html?done=true") %>" />
                            <button type="submit">Replicate Catalog</button>
                        </form>
                    </td>
                </tr>
            </table>

            <h2>Catalog component usage</h2>
            <c:choose>

                <c:when test="${usage.inUse}">
                    <table>
                        <tr>
                            <th>Component</th>
                            <th>Location</th>
                        </tr>
                        <c:forEach items="${usage.locations}" var="location">
                            <tr>
                                <td>${location.component}</td>
                                <td><a href="${location.url}">${location.pageTitle}</a></td>
                            </tr>
                        </c:forEach>
                    </table>
                    <div class="info-block">
                        Because the catalog is actively being used, you are not allowed to delete it.
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="info-block">
                        <p>
                            None of the widgets defined in this catalog are currently in use, it is therefore
                            safe to delete the catalog. To do so, use the button below.
                            <br/><br/>
                        </p>
                        <form id="deleteCatalog" action="/bin/deleteWidgetCatalog" method="post">
                            <input type="hidden" name="catalog" value="${uniqueCatalogId}" />
                            <input type="hidden" name="redirectTo" value="<%= mappedUrl(_currentPage.getPath() + ".html")%>" />
                            <button type="submit">Delete Catalog</button>
                        </form>
                    </div>
                    
                </c:otherwise>
            </c:choose>

            <c:if test="${replicated}">
                <p class="info-block flash">
                    The catalog has been replicated to the publication servers.
                </p>
                <script type="text/javascript">
                    (function($, undefined) {
                        $(document).ready(function() {
                            setTimeout(
                                function() {
                                    $(".info-block.flash").fadeOut(1000);
                                },
                                3000
                            );

                            // add a confirmation dialog to the submit button
                            $("#deleteCatalog").submit(function() {
                                if (confirm("Are you sure you want to delete the catalog?")) {
                                    return true;
                                } else {
                                    return false;
                                }
                            })
                        });

                    })(UOA.jQuery);
                </script>
            </c:if>
        </c:when>
        <c:otherwise>
            <p class="cq-warning">Please configure this component</p>
        </c:otherwise>
    </c:choose>
</div>
