<%@ tag pageEncoding="UTF-8" %>
<%@ include file="../jsp/cabecalho.jspf"%>

<%@ attribute name="id" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="placeholder" required="false" type="java.lang.String" %>
<%@ attribute name="type" required="true" type="java.lang.String" %>

<label for="${id}">${label}</label>
<input type="${type}" id="${id}" placeholder="${placeholder}">
