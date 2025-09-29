<%@tag pageEncoding="UTF-8" %>
<%@ include file="../jsp/cabecalho.jspf"%>

<%@ attribute name="alerta" required="false" type="java.lang.String" %>
<%@ attribute name="css" required="true" type="java.lang.String" %>

<c:if test="${not empty alerta}">
    <div class="${css}">
            ${fn:escapeXml(alerta)}
    </div>
</c:if>