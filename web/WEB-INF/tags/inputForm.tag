<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%@ attribute name="path" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="placeholder" required="false" type="java.lang.String" %>

<form:label path="${path}">${label}</form:label>
<form:input path="${path}" placeholder="${placeholder}" />
<form:errors path="${path}" cssClass="alerta-erro"/>