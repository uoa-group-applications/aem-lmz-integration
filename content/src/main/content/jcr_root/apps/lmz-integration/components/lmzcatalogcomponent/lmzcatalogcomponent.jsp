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
<%

    LMZWidgetComponentCreator creator = new LMZWidgetComponentCreator(_beanContext);

    if (creator.shouldSynchronize()) {
        request.setAttribute("updateResults", creator.updateAllWidgets());
    } else {
        request.setAttribute("noSynch", true);
    }

    request.setAttribute("maintenanceMode", creator.isInMaintenanceMode());
    request.setAttribute("replicated", "true".equals(request.getParameter("done")));
    request.setAttribute("catalogName", creator.getRawCatalogName());
    request.setAttribute("sanitizedCatalogName", creator.getSanitizedCatalogName());

%>
<c:choose>
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
                    <form action="/bin/replicateWidgetCatalog.do" method="get">
                        <input type="hidden" name="catalog" value="${sanitizedCatalogName}" />
                        <input type="hidden" name="redirectTo" value="<%= mappedUrl(_currentPage.getPath() + ".html?done=true") %>" />
                        <button type="submit">Replicate Catalog</button>
                    </form>
                </td>
            </tr>
        </table>


        <c:if test="${replicated}">
            <p class="info-block">
                The catalog has been replicated to the publication servers.
            </p>
            <script type="text/javascript">
                (function($, undefined) {
                    $(document).ready(function() {
                        setTimeout(
                            function() {
                                $(".info-block").fadeOut(1000);
                            },
                            3000
                        );
                    });
                })(UOA.jQuery);
            </script>
        </c:if>
    </c:when>
    <c:otherwise>
        <p class="cq-warning">Please configure this component</p>
    </c:otherwise>
</c:choose>

