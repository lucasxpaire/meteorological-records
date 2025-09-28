<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="ftags" uri="http://www.springframework.org/tags/form" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div>
        <tags:barraLateral paginaAtiva="propriedades" />

        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Edição de Propriedade" textoCondicaoFalsa="Cadastro de Propriedade" tagHtml="h1" />
                <a href="gerenciarProprietarios.html" class="botao botao-novo">Voltar</a>
            </div>

            <div class="formulario-container">
                <tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Altere os dados necessários de propriedade" textoCondicaoFalsa="Insira os dados no formulário abaixo para cadastrar uma propriedade" tagHtml="h3" />

                <form:form modelAttribute="PropriedadeCommand" method="post" action="cadastroPropriedade.html" enctype="multipart/form-data">
                    <form:hidden path="id" />
                    <form:hidden path="paginaOrigemRequisicao" />

                    <c:choose>
                        <c:when test="${not empty param.idProprietario}">
                            <form:hidden path="idProprietario" />
                        </c:when>
                        <c:otherwise>
                            <tags:inputForm path="cpfProprietario" label="Cpf do Proprietário" placeholder="Digite o CPF do proprietário" />
                        </c:otherwise>
                    </c:choose>

                    <tags:inputForm path="nome" label="Nome" placeholder="Digite o nome da propriedade" />

                    <h4>Definição do Polígono</h4>
                    <form:errors path="tipoEntradaPoligono" cssClass="alerta-erro" />

                    <div class="radio-group">
                        <label for="tipoManual">
                            <form:radiobutton path="tipoEntradaPoligono" id="tipoManual" value="manual" checked="true" />
                            Inserção Manual
                        </label>
                        <label for="tipoArquivo">
                            <form:radiobutton path="tipoEntradaPoligono" id="tipoArquivo" value="arquivo" />
                            Arquivo CSV
                        </label>
                    </div>

                    <div id="campoManual" >
                        <label for="coordenadaPorInsercaoManual">Coordenadas:</label>
                        <form:textarea path="coordenadasPorInsercaoManual" id="coordenadaPorInsercaoManual" placeholder="Insira as coordenadas no formato (lat;long): XX,XXXX;XX,XXXX. Pressione Enter para uma nova coordenada." cssStyle="width: 700px; height: 200px; overflow: hidden"/>
                        <form:errors path="coordenadasPorInsercaoManual" cssClass="alerta-erro" />
                    </div>

                    <div id="campoArquivo" class="formulario">
                        <label for="coordenadaPorArquivo">Arquivo CSV:</label>
                        <form:input path="coordenadasPorArquivo" type="file" id="coordenadaPorArquivo" />
                        <form:errors path="coordenadasPorArquivo" cssClass="alerta-erro" />
                    </div>

                    <div class="formulario-acoes">
                        <input type="submit" value="<tags:conteudoCondicional condicao="${not empty propriedade}" textoCondicaoVerdadeira="Alterar" textoCondicaoFalsa="Cadastrar" />" class="botao botao-novo"/>
                    </div>
                </form:form>

            </div>
        </main>
    </div>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const tipoManualRadio = document.getElementById('tipoManual');
            const tipoArquivoRadio = document.getElementById('tipoArquivo');
            const campoManual = document.getElementById('campoManual')
            const campoArquivo = document.getElementById('campoArquivo')

            function toggleCampos() {
                if (tipoManualRadio.checked) {
                    campoManual.style.display = 'block';
                    campoArquivo.style.display = 'none';
                } else if (tipoArquivoRadio.checked) {
                    campoManual.style.display = 'none';
                    campoArquivo.style.display = 'block';
                }
            }

            toggleCampos();

            tipoManualRadio.addEventListener('change', toggleCampos);
            tipoArquivoRadio.addEventListener('change', toggleCampos);
        })

    </script>
    <script src="<c:url value='/js/mascarasCoordenadas.js'/>"></script>
    <script>
        VMasker(document.getElementById("cpfProprietario")).maskPattern("999.999.999-99");
    </script>
</tags:corpo>