<%@ page pageEncoding="UTF-8" %>

<%@ include file="cabecalho.jspf" %>

<c:url var="urlGerenciarPropriedades" value="gerenciarPropriedades.html"/>
<c:url var="urlCadastroPropriedade" value="cadastroPropriedade.html"/>
<c:url var="downloadUrl" value="/arquivo/baixar/${propriedade.arquivoPontos.id}"/>
<c:set var="label"><tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Alterar" textoCondicaoFalsa="Cadastrar"/></c:set>

<tags:corpo>
    <div>
        <tags:barraLateral paginaAtiva="propriedades" />

        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Edição de propriedade" textoCondicaoFalsa="Cadastro de propriedade" tagHtml="h1" />
                <tags:botao label="Voltar" css="botao botao-novo" href="${urlGerenciarPropriedades}"/>
            </div>

            <div class="formulario-container">
                <tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Altere os dados necessários de propriedade" textoCondicaoFalsa="Insira os dados no formulário abaixo para cadastrar uma propriedade" tagHtml="h3" />

                <tags:alerta css="alerta-sucesso" alerta="${sucesso}" />

                <form:form modelAttribute="PropriedadeCommand" method="post" action="${urlCadastroPropriedade}">
                    <form:hidden path="id" />

                    <tags:inputFormulario path="cpfProprietario" label="CPF do proprietário" placeholder="Digite o CPF do proprietário" />

                    <tags:inputFormulario path="nome" label="Nome" placeholder="Digite o nome da propriedade" />

                    <h4>Definição do polígono</h4>
                    <form:hidden path="nomeArquivoPontos" />

                    <label for="pontos">Pontos</label>
                    <form:textarea path="pontos" id="pontos" placeholder="Digite os pontos (XX,XXXX;XX,XXXX) uma por linha, ou arraste seu arquivo .txt/.csv aqui." cssStyle="width: 700px; height: 200px;"/>
                    <form:errors path="pontos" cssClass="alerta-erro-formulario" />

                    <div class="formulario-acoes-arquivo">
                        <input type="file" id="seletorDeArquivo" accept=".txt,.csv" style="display: none">
                        <tags:botao label="Selecionar arquivo pontos" css="botao botao-novo botao-com-icone" type="button" id="linkSelecionarArquivo"/>
                        
                        <c:if test="${not empty propriedade && not empty propriedade.arquivoPontos}">
                            <tags:botao label="Baixar arquivo" css="botao botao-novo botao-com-icone" href="${downloadUrl}" icone="https://img.icons8.com/small/16/download--v1.png"/>
                        </c:if>
                    </div>

                    <div class="formulario-acoes">
                        <tags:botao label="${label}" css="botao botao-novo" type="submit"/>
                    </div>
                </form:form>
            </div>
        </main>
    </div>
    <script src="<c:url value='/js/insercaoCoordenadas.js' />"></script>
    <script>
        VMasker(document.getElementById("cpfProprietario")).maskPattern("999.999.999-99");
    </script>
</tags:corpo>