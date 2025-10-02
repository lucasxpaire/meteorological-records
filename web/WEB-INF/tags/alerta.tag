<%@tag pageEncoding="UTF-8" %>

<%@ attribute name="alerta" required="false" type="java.lang.String" %>
<%@ attribute name="css" required="true" type="java.lang.String" %>

<%@ include file="../jsp/cabecalho.jspf"%>

<c:if test="${not empty alerta}">
    <div class="${css}">
            ${fn:escapeXml(alerta)}
    </div>
</c:if>