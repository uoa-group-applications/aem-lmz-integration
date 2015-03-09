<%--

  LMZ Widget component.

--%>
<%@ include file="/apps/central/global/global.jsp" %>
<%@ include file="/apps/lmz-integration/global/contextObjects.jsp" %>
<%@ page import="nz.ac.auckland.aem.lmz.core.LMZWidgetRenderer" %>

<c:set var="renderer" value="<%= new LMZWidgetRenderer(_beanContext) %>" />
<c:set var="ctx" value="${renderer.renderContext}" />

<%-- only on authoring environment --%>
<c:if test="${!renderer.esiInclude}">
    <c:set var="isAdmin" value="${renderer.administrator ? 'true' : 'false'}" />
    <script type="text/javascript">
        window.UOA = window.UOA || {};
        UOA.IS_ADMIN = ${isAdmin};
    </script>
</c:if>

<c:choose>
    <c:when test="${renderer.disabled}">
        <p class="maintenance-mode">
            ${renderer.maintenanceMessage}
        </p>
    </c:when>
    <c:otherwise>
        <div class="widget-wrapper" data-base-url="${ctx.lmzBase}">
            <c:choose>
                <c:when test="${renderer.esiInclude}">
                    <div data-loading="true">
                        <esi:include src="/esi${ctx.urlContextPath}view${ctx.viewQueryString}" />
                    </div>
                </c:when>
                <c:when test="${ctx.fetchedContent && ctx.validResponse}">
                    <div data-loading="true">
                        ${ctx.html}
                    </div>
                </c:when>
                <c:when test="${ctx.fetchedContent && !ctx.validResponse}">
                    <p class="cq-warning">Invalid server response code: ${ctx.responseStatusCode}</p>
                </c:when>
                <c:otherwise>
                    <p class="cq-warning">Please configure this component</p>
                </c:otherwise>
            </c:choose>
        </div>
    </c:otherwise>
</c:choose>
