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

                    <tags:inputForm path="nome" label="Nome" placeholder="Digite o nome completo" />
                    <tags:inputForm path="cpf" label="Cpf" placeholder="Digite o cpf" />
                    <tags:inputForm path="telefone" label="Telefone" placeholder="Digite o telefone" />
                    <tags:selectForm path="cor.id" label="Cor" items="${cores}" itemValue="id" itemLabel="nome" descricao="Selecione uma cor" />

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