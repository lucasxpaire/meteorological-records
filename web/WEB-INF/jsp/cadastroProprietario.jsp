<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="proprietarios"/>
        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <c:choose>
                    <c:when test="${not empty proprietario}">
                        <h1>Edição de Proprietário</h1>
                    </c:when>
                    <c:otherwise>
                        <h1>Cadastro de Proprietário</h1>
                    </c:otherwise>
                </c:choose>
                <a href="gerenciarProprietarios.html" class="botao botao-novo">Voltar</a>
            </div>

            <c:if test="${not empty resultado}">
                <div class="alerta alerta-sucesso">
                        ${resultado}
                </div>
            </c:if>

            <form:errors element="div" cssClass="alerta alerta-erro"/>

            <div class="formulario-container">
                <c:choose>
                    <c:when test="${not empty proprietario}">
                        <h3>Altere os dados necessários do proprietário</h3>
                    </c:when>
                    <c:otherwise>
                        <h3>Insira os dados no formulário abaixo para cadastrar um proprietário</h3>
                    </c:otherwise>
                </c:choose>

                <form:form modelAttribute="CadastroProprietarioCommand" method="post" action="cadastroProprietario.html">
                    <form:hidden path="id"/>
                    <form:errors element="div" cssClass="alerta alerta-erro"/>

                    <form:label for="nome" path="nome">Nome:</form:label>
                    <form:input path="nome" id="nome" placeholder="Digite o nome completo" required="required"/>

                    <form:label for="cpf" path="cpf">CPF:</form:label>
                    <form:input path="cpf" id="cpf" placeholder="Digite o CPF" required="required"/>

                    <form:label for="telefone" path="telefone">Telefone:</form:label>
                    <form:input path="telefone" id="telefone" placeholder="Digite o telefone" required="required"/>

                    <form:label for="cor" path="cor.id">Cor:</form:label>
                    <form:select path="cor.id" id="cor" required="required">
                        <form:option value="" label="Selecione uma cor"/>
                        <form:options items="${cores}" itemValue="id" itemLabel="nome"/>
                    </form:select>

                    <c:choose>
                        <c:when test="${not empty proprietario}">
                            <input type="submit" value="Alterar" class="botao botao-novo"/>
                        </c:when>
                        <c:otherwise>
                            <input type="submit" value="Cadastrar" class="botao botao-novo"/>
                        </c:otherwise>
                    </c:choose>
                </form:form>
            </div>

        </main>
    </div>
    <script>
        VMasker(document.getElementById("cpf")).maskPattern("999.999.999-99");
        VMasker(document.getElementById("telefone")).maskPattern("(99) 99999-9999");
    </script>
</tags:corpo>