<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ftags" uri="http://www.springframework.org/tags/form" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div>
        <tags:barraLateral paginaAtiva="propriedades" />

        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Edição de Propriedade" textoCondicaoFalsa="Cadastro de Propriedade" tagHtml="h1" />
                <a href="gerenciarPropriedades.html" class="botao botao-novo">Voltar</a>
            </div>

            <c:if test="${not empty resultado}">
                <div class="alerta alerta-sucesso">
                    ${resultado}
                </div>
            </c:if>

            <div class="formulario-container">
                <tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Altere os dados necessários de propriedade" textoCondicaoFalsa="Insira os dados no formulário abaixo para cadastrar uma propriedade" tagHtml="h3" />

                <form:form modelAttribute="PropriedadeCommand" method="post" action="cadastroPropriedade.html">
                    <form:hidden path="id" />
                    <form:hidden path="proprietarioId" />

                    <tags:inputForm path="nome" label="Nome" placeholder="Digite o nome da propriedade" />

                    <div class="separador-formulario">
                        <h4>Definição do Polígono</h4>
                        <form:errors path="tipoEntradaPoligono" cssClass="mensagem-erro" />
                    </div>

                    <div class="campo-formulario-radio">
                        <form:radiobutton path="tipoEntradaPoligono" id="tipoManual" value="manual" checked="true"/>
                        <label for="entrada-manual">Inserção Manual</label>
                    </div>

                    <div class="campo-formulario-radio">
                        <form:radiobutton path="tipoEntradaPoligono" id="tipoArquivo" value="arquivo" />
                        <label for="entrada-arquivo">Arquivo CSV</label>
                    </div>

                    <div id="campoManual" class="campo-formulario">
                        <label for="coordenadaPorInsercaoManual">Coordenadas:</label>
                        <form:textarea path="coordenadasPorInsercaoManual" id="coordenadaPorInsercaoManual" plaeholder="Insira as coordenadas no formato(latitude,longitude): XX,XXXXX XX,XXXX. Separando latitude e longitude por espaço" cssClass="campo-text" />
                        <form:errors path="coordenadasPorInsercaoManual" cssClass="mensagem-erro" />
                    </div>

                    <div id="campoArquivo" class="campo-formulario">
                        <label for="coordenadaPorArquivo">Arquivo CSV:</label>
                        <form:input path="coordenadasPorArquivo" type="file" id="coordenadaPorArquivo" />
                        <form:errors path="coordenadasPorArquivo" cssClass="mensagem-erro" />
                    </div>

                    <input type="submit" value="<tags:conteudoCondicional condicao="${not empty proprietario}" textoCondicaoVerdadeira="Alterar" textoCondicaoFalsa="Cadastrar" />" class="botao botao-novo"/>
                </form:form>

            </div>
        </main>
    </div>
    <script>
        document.querySelectorAll('input[name="tipoEntradaPoligono"]').forEach(function (radio) {
            radio.addEventListener('change', function () {
                if (this.value === 'manual') {
                    document.getElementById('campoManual').style.display = 'block';
                    document.getElementById('campoArquivo').style.display = 'none';
                } else if (this.value === 'arquivo') {
                    document.getElementById('campoArquivo').style.display = 'block';
                    document.getElementById('campoManual').style.display = 'none';
                }
            })
        })
    </script>
    <script src="<c:url value='/js/mascarasCoordenadas.js'/>"></script>
</tags:corpo>