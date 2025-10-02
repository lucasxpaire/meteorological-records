<%@tag pageEncoding="UTF-8" %>

<%@ attribute name="id" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="css" required="true" type="java.lang.String" %>
<%@ attribute name="type" required="true" type="java.lang.String" %>
<%@ attribute name="checked" required="true" type="java.lang.Boolean" %>

<%@ include file="../jsp/cabecalho.jspf"%>

<label class="${css}">
    <input type="${type}" id="${id}" <c:if test="${checked}">checked</c:if> > ${label}
</label>
