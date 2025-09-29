<%@ tag pageEncoding="UTF-8" %>
<%@ include file="../jsp/cabecalho.jspf"%>


<%@ attribute name="path" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="placeholder" required="false" type="java.lang.String" %>

<form:label path="${path}">${label}</form:label>
<form:input path="${path}" placeholder="${placeholder}" />
<form:errors path="${path}" cssClass="alerta-erro-formulario"/>