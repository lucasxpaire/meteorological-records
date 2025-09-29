<%@ tag pageEncoding="UTF-8" %>
<%@ tag trimDirectiveWhitespaces="true" %>
<%@ include file="../jsp/cabecalho.jspf"%>

<%@ attribute name="condicao" required="true" type="java.lang.Boolean" %>
<%@ attribute name="textoCondicaoVerdadeira" required="true" type="java.lang.String" %>
<%@ attribute name="textoCondicaoFalsa" required="true" type="java.lang.String" %>
<%@ attribute name="tagHtml" required="false" type="java.lang.String" description="Elemento HTML para envolver o texto." %>

<c:set var="textoFinal">
    <c:choose>
        <c:when test="${condicao}">${textoCondicaoVerdadeira}</c:when>
        <c:otherwise>${textoCondicaoFalsa}</c:otherwise>
    </c:choose>
</c:set>

<c:choose>
    <c:when test="${not empty tagHtml}">
        <${tagHtml}>${fn:escapeXml(textoFinal)}</${tagHtml}>
    </c:when>
    <c:otherwise>
        ${fn:escapeXml(textoFinal)}
    </c:otherwise>
</c:choose>