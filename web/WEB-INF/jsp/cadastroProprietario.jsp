<%@ page pageEncoding="UTF-8" %>

<%@ include file="cabecalho.jspf" %>

<c:url var="urlGerenciarProprietarios" value="gerenciarProprietarios.html"/>
<c:url var="urlCadastroProprietario" value="cadastroProprietario.html"/>
<c:set var="label"><tags:alternarTexto condicao='${not empty proprietario}' textoCondicaoVerdadeira='Alterar' textoCondicaoFalsa='Cadastrar'/></c:set>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:menuLateral paginaAtiva="proprietarios"/>
        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <tags:alternarTexto condicao="${not empty proprietario}" textoCondicaoVerdadeira="Edição de proprietário" textoCondicaoFalsa="Cadastro de proprietário" tagHtml="h1" />
                <tags:botao label="Voltar" css="botao botao-novo" href="${urlGerenciarProprietarios}"/>
            </div>

            <div class="formulario-container">
                <tags:alerta css="alerta-sucesso" alerta="${sucesso}" />

                <tags:alternarTexto condicao="${not empty proprietario}" textoCondicaoVerdadeira="Altere os dados necessários do proprietário" textoCondicaoFalsa="Insira os dados no formulário abaixo para cadastrar um proprietário" tagHtml="h3" />
                
                <form:form modelAttribute="ProprietarioCommand" method="post" action="${urlCadastroProprietario}">
                    <form:hidden path="id"/>

                    <tags:inputFormulario path="nome" label="Nome" placeholder="Digite o nome completo" />
                    <tags:inputFormulario path="cpf" label="CPF" placeholder="Digite o CPF" />
                    <tags:inputFormulario path="telefone" label="Telefone" placeholder="Digite o telefone" />
                    <tags:selectFormulario path="corId" label="Cor" items="${cores}" itemValue="id" itemLabel="nome" descricao="Selecione uma cor" />

                    <div class="formulario-acoes">
                        <tags:botao label="${label}" css="botao botao-novo" type="submit"/>
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