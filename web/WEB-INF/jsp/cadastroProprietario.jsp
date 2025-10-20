<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<%--@elvariable id="sucesso" type="java.lang.String"--%>
<%--@elvariable id="falha" type="java.lang.String"--%>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="proprietarios"/>
        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <tags:conteudoCondicional condicao="${not empty proprietario}" textoCondicaoVerdadeira="Edição de proprietário" textoCondicaoFalsa="Cadastro de proprietário" tagHtml="h1" />
                <a href="gerenciarProprietarios.html" class="botao botao-novo">Voltar</a>
            </div>

            <div class="formulario-container">
                <tags:conteudoCondicional condicao="${not empty proprietario}" textoCondicaoVerdadeira="Altere os dados necessários do proprietário" textoCondicaoFalsa="Insira os dados no formulário abaixo para cadastrar um proprietário" tagHtml="h3" />

                <tags:alerta css="alerta-sucesso" alerta="${sucesso}" />

                <form:form modelAttribute="ProprietarioCommand" method="post" action="cadastroProprietario.html">
                    <form:hidden path="id"/>

                    <tags:inputFormulario path="nome" label="Nome" placeholder="Digite o nome completo" />
                    <tags:inputFormulario path="cpf" label="CPF" placeholder="Digite o CPF" />
                    <tags:inputFormulario path="telefone" label="Telefone" placeholder="Digite o telefone" />
                    <tags:selectFormulario path="corId" label="Cor" items="${cores}" itemValue="id" itemLabel="nome" descricao="Selecione uma cor" />

                    <div class="formulario-acoes">
                        <input type="submit" value="<tags:conteudoCondicional condicao='${not empty proprietario}' textoCondicaoVerdadeira='Alterar' textoCondicaoFalsa='Cadastrar' />" class="botao botao-novo"/>
                    </div>

                </form:form>
            </div>
        </main>
    </div>
    <script>
        VMasker(document.getElementById("cpf")).maskPattern("999.999.999-99");
        VMasker(document.getElementById("telefone")).maskPattern("(99) 99999-9999");
    </script>
</tags:corpo>