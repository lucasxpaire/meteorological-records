<%@tag pageEncoding="UTF-8" %>

<%@ attribute name="href" required="false" type="java.lang.String" %>
<%@ attribute name="id" required="false" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="css" required="true" type="java.lang.String" %>

<%@ include file="../jsp/cabecalho.jspf"%>

<button id="${id}" class="${css}">${label}</button>