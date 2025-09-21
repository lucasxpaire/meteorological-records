<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="proprietarios"/>
        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <tags:conteudoCondicional condicao="${not empty proprietario}" textoCondicaoVerdadeira="Edição de Proprietário" textoCondicaoFalsa="Cadastro de Proprietário" tagHtml="h1" />
                <a href="gerenciarProprietarios.html" class="botao botao-novo">Voltar</a>
            </div>

            <c:if test="${not empty resultado}">
                <div class="alerta alerta-sucesso">
                        ${resultado}
                </div>
            </c:if>

            <div class="formulario-container">
                <tags:conteudoCondicional condicao="${not empty proprietario}" textoCondicaoVerdadeira="Altere os dados necessários do proprietário" textoCondicaoFalsa="Insira os dados no formulário abaixo para cadastrar um proprietário" tagHtml="h3" />

                <form:form modelAttribute="ProprietarioCommand" method="post" action="cadastroProprietario.html">
                    <form:hidden path="id"/>

                    <tags:inputForm path="nome" label="Nome" placeholder="Digite o nome completo" />
                    <tags:inputForm path="cpf" label="Cpf" placeholder="Digite o cpf" />
                    <tags:inputForm path="telefone" label="Telefone" placeholder="Digite o telefone" />
                    <tags:selectForm path="corId" label="Cor" items="${cores}" itemValue="id" itemLabel="nome" descricao="Selecione uma cor" />

                    <input type="submit" value="<tags:conteudoCondicional condicao="${not empty proprietario}" textoCondicaoVerdadeira="Alterar" textoCondicaoFalsa="Cadastrar" />" class="botao botao-novo"/>

                </form:form>
            </div>
        </main>
    </div>
    <script>
        VMasker(document.getElementById("cpf")).maskPattern("999.999.999-99");
        VMasker(document.getElementById("telefone")).maskPattern("(99) 99999-9999");
    </script>
</tags:corpo>