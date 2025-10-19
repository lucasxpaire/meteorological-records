<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="ftags" uri="http://www.springframework.org/tags/form" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div>
        <tags:barraLateral paginaAtiva="propriedades" />

        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Edição de propriedade" textoCondicaoFalsa="Cadastro de propriedade" tagHtml="h1" />
                <a href="gerenciarProprietarios.html" class="botao botao-novo">Voltar</a>
            </div>

            <div class="formulario-container">
                <tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Altere os dados necessários de propriedade" textoCondicaoFalsa="Insira os dados no formulário abaixo para cadastrar uma propriedade" tagHtml="h3" />

                <form:form modelAttribute="PropriedadeCommand" method="post" action="cadastroPropriedade.html">
                    <form:hidden path="id" />

                    <tags:inputFormulario path="cpfProprietario" label="CPF do proprietário" placeholder="Digite o CPF do proprietário" />

                    <tags:inputFormulario path="nome" label="Nome" placeholder="Digite o nome da propriedade" />

                    <h4>Definição do polígono</h4>
                    <form:hidden path="nomeArquivoPontos" />

                    <div id="campoManual" >
                        <label for="pontos">Coordenadas no formato: XX,XXXX;XX,XXXX</label>
                        <form:textarea path="pontos" id="pontos" placeholder="Digite as coordenadas (XX,XXXX;XX,XXXX) uma por linha, OU ARRASTE SEU ARQUIVO .txt/.csv aqui." cssStyle="width: 700px; height: 200px;"/>
                        <form:errors path="pontos" cssClass="alerta-erro-formulario" />

                        <div class="formulario-acoes-arquivo">
                            <input type="file" id="seletorDeArquivo" accept=".txt,.csv" style="display: none">
                            <a href="#" id="linkSelecionarArquivo" class="botao botao-novo botao-com-icone">Selecionar Arquivo</a>

                            <c:if test="${not empty propriedade && not empty propriedade.arquivoPontos}">
                                <a href="<c:url value='/arquivo/baixar/${propriedade.arquivoPontos.id}' />" title="Baixar arquivo" class="botao botao-novo botao-com-icone">
                                    <img src="https://img.icons8.com/small/16/download--v1.png" alt="Baixar arquivo"/>Baixar arquivo
                                </a>
                            </c:if>
                        </div>
                    </div>
                    <div class="formulario-acoes">
                        <input type="submit" value="<tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Alterar" textoCondicaoFalsa="Cadastrar" />" class="botao botao-novo"/>
                    </div>
                </form:form>
            </div>
        </main>
    </div>
    <script src="<c:url value='/js/insercaoCoordenadas.js' />"></script>
    <script src="<c:url value='/js/mascaraCoordenadas.js'/>"></script>
    <script>
        VMasker(document.getElementById("cpfProprietario")).maskPattern("999.999.999-99");
    </script>
</tags:corpo>