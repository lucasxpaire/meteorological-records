<%@ tag pageEncoding="UTF-8" %>
<%@ include file="../jsp/cabecalho.jspf"%>

<%@ attribute name="path" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="items" required="true" rtexprvalue="true" type="java.lang.Object" %>
<%@ attribute name="itemValue" required="false" type="java.lang.String" %>
<%@ attribute name="itemLabel" required="false" type="java.lang.String" %>
<%@ attribute name="descricao" required="true" type="java.lang.String" %>

<form:label path="${path}">${label}:</form:label>
<form:select path="${path}" id="${path}" cssClass="form-control">
    <form:option value="" label="${descricao}" disabled="true" />

    <c:choose>
        <c:when test="${not empty itemValue}">
            <form:options items="${items}" itemValue="${itemValue}" itemLabel="${itemLabel}"/>
        </c:when>
        <c:otherwise>
            <form:options items="${items}"/>
        </c:otherwise>
    </c:choose>

</form:select>
<form:errors path="${path}" element="div" cssClass="alerta-erro-formulario" />
