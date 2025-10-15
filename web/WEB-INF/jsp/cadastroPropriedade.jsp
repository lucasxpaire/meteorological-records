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

                <form:form modelAttribute="PropriedadeCommand" method="post" action="cadastroPropriedade.html" enctype="multipart/form-data">
                    <form:hidden path="id" />

                    <tags:inputFormulario path="cpfProprietario" label="CPF do proprietário" placeholder="Digite o CPF do proprietário" />

                    <tags:inputFormulario path="nome" label="Nome" placeholder="Digite o nome da propriedade" />

                    <h4>Definição do polígono</h4>

                    <div class="radio-group">
                        <label for="tipoManual">
                            <form:radiobutton path="tipoEntradaPoligono" id="tipoManual" value="manual" checked="true" /> Inserção manual
                        </label>
                        <label for="tipoArquivo">
                            <form:radiobutton path="tipoEntradaPoligono" id="tipoArquivo" value="arquivo" /> Arquivo CSV
                        </label>
                    </div>

                    <div id="campoManual" >
                        <label for="coordenadaPorInsercaoManual">Coordenadas:</label>
                        <form:textarea path="coordenadasPorInsercaoManual" id="coordenadaPorInsercaoManual" placeholder="Inserir no formato (lat;long): XX,XXXX;XX,XXXX. Aperte Enter para uma nova coordenada." cssStyle="width: 700px; height: 200px; overflow: hidden"/>
                        <form:errors path="coordenadasPorInsercaoManual" cssClass="alerta-erro-formulario" />
                    </div>

                    <div id="campoArquivo" class="formulario">
                        <label for="coordenadaPorArquivo">Arquivo CSV:</label>

                        <div class="input-com-icones">
                            <form:input path="coordenadasPorArquivo" type="file" id="coordenadaPorArquivo" />

                            <a id="visualizarArquivo" class="icone-arquivo" title="Visualizar conteúdo do arquivo">
                                <img src="https://img.icons8.com/ios/50/view-file.png" alt="Visualizar" width="20" height="20"/>
                            </a>
                            <a id="downloadArquivo" class="icone-arquivo" title="Baixar arquivo selecionado">
                                <img src="https://img.icons8.com/small/16/download--v1.png" alt="Download" width="20" height="20"/>
                            </a>
                        </div>

                        <form:errors path="coordenadasPorArquivo" cssClass="alerta-erro-formulario" />
                    </div>

                    <form:errors path="tipoEntradaPoligono" cssClass="alerta-erro-formulario" />

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
    <script src="<c:url value='/js/mascaraCoordenadas.js'/>"></script>
    <script>
        VMasker(document.getElementById("cpfProprietario")).maskPattern("999.999.999-99");
    </script>
</tags:corpo>