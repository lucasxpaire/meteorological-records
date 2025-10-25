<%@ tag pageEncoding="UTF-8" %>

<%@ include file="../jsp/cabecalho.jspf"%>

<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="css" required="false" type="java.lang.String" %>
<%@ attribute name="type" required="false" type="java.lang.String" %>
<%@ attribute name="href" required="false" type="java.lang.String" %>
<%@ attribute name="id" required="false" type="java.lang.String" %>
<%@ attribute name="icone" required="false" type="java.lang.String" %>
<%@ attribute name="onclick" required="false" type="java.lang.String" %>

<c:if test="${empty type and empty href}">
    <c:set var="buttonType" value="submit" />
</c:if>
<c:if test="${not empty type}">
    <c:set var="buttonType" value="${type}" />
</c:if>

<c:set var="css" value="${css}" />
<c:if test="${not empty icone}">
    <c:set var="css" value="${css}" />
</c:if>

<c:choose>
    <c:when test="${not empty href}">
        <a href="${href}" <c:if test="${not empty id}">id="${id}"</c:if> class="${css}" <c:if test="${not empty onclick}">onclick="${onclick}"</c:if>>
            <c:if test="${not empty icone}"><img src="${icone}" alt=""/></c:if> ${label}
        </a>
    </c:when>
    <c:otherwise>
        <button <c:if test="${not empty id}">id="${id}"</c:if> type="${buttonType}" class="${css}" <c:if test="${not empty onclick}">onclick="${onclick}"</c:if>>
            <c:if test="${not empty icone}"><img src="${icone}" alt=""/></c:if> ${label}
        </button>
    </c:otherwise>
</c:choose>