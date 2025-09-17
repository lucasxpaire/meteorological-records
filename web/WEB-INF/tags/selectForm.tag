<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="path" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="items" required="true" rtexprvalue="true" type="java.util.Collection" %>
<%@ attribute name="itemValue" required="true" type="java.lang.String" %>
<%@ attribute name="itemLabel" required="true" type="java.lang.String" %>
<%@ attribute name="descricao" required="true" type="java.lang.String" %>


<form:label path="${path}">${label}:</form:label>
<form:select path="${path}" id="${path}" cssClass="form-control">
    <form:option value="" label="${descricao}" disabled="true" />
    <form:options items="${items}" itemValue="${itemValue}" itemLabel="${itemLabel}"/>
</form:select>
<form:errors path="${path}" element="div" cssClass="alerta-erro" />
