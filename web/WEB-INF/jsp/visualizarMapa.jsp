<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>

    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="mapa" />

        <div class="estrutura-pagina-conteudo">
            <div id="mapa"></div>
        </div>

        <div class="menu-flutuante">
            <form:form modelAttribute="ControleMapaCommand" action="visualizarMapa.html" method="get">
                <h3>Controles do mapa</h3>

                <div class="menu-flutuante-grupo">
                    <div class="menu-flutuante-grupo">
                        <tags:selectFormulario path="opcaoSelecionada" label="Visualizar propriedades" items="${opcoesControleMapa}" descricao="Selecione uma opção" />
                    </div>

                    <div class="menu-flutuante-grupo-escondido">
                        <tags:inputFormulario path="cpfBusca" label="CPF do proprietário" />
                    </div>

                    <div class="menu-flutuante-grupo-escondido">
                        <tags:inputFormulario path="nomeBusca" label="Nome de propriedade" />
                    </div>
                    
                    <div class="menu-flutuante-grupo">
                        <tags:botao id="botaoBuscarPropriedades" label="Buscar" css="botao" />
                    </div>
                </div>

            </form:form>
        </div>
    </div>
    <script>
        const propriedades = ${propriedadesJson};
        const estacoes = ${estacoesJson};
    </script>
    <script src="<c:url value='/js/mapa.js' />"></script>
    <script src="https://maps.googleapis.com/maps/api/js"></script>
</tags:corpo>


<%--<tags:corpo>--%>
<%--    <div class="estrutura-pagina">--%>
<%--        <tags:barraLateral paginaAtiva="mapas"/>--%>

<%--        <div class="estrutura-pagina-conteudo">--%>
<%--            <div id="map"></div>--%>
<%--        </div>--%>

<%--        <div class="menu-flutuante">--%>
<%--            <form:form modelAttribute="mapaCommand" action="visualizarMapas.html" method="get" id="form-mapa-controles">--%>
<%--                <h3>Controles do Mapa</h3>--%>

<%--                <div class="menu-flutuante-grupo">--%>
<%--                    <label for="select-propriedade">Visualizar Propriedade</label>--%>
<%--                    <form:select path="criterioBusca" id="select-propriedade">--%>
<%--                        <form:option value="todas">Todas as Propriedades</form:option>--%>
<%--                        <form:option value="recente">Propriedade Mais Recente</form:option>--%>
<%--                        <form:option value="nome">Buscar por nome</form:option>--%>
<%--                        <form:option value="cpf">Buscar por cpf do proprietário</form:option>--%>
<%--                    </form:select>--%>
<%--                </div>--%>

<%--                <div class="menu-flutuante-grupo" id="grupo-busca">--%>
<%--                    <label for="input-busca">Termo de Busca</label>--%>
<%--                    <form:input path="termoBusca" id="input-busca" cssClass="campo-busca" />--%>
<%--                    <button type="submit" class="botao">Buscar</button>--%>
<%--                    <div id="busca-alerta-falha" class="alerta-erro-formulario" style="display: none;">Falha: Nenhuma propriedade encontrada.</div>--%>
<%--                </div>--%>

<%--                <div class="menu-flutuante-grupo">--%>
<%--                    <label>Exibir Camadas:</label>--%>
<%--                    <div class="checkbox-label">--%>
<%--                        <form:checkbox path="exibirEstacoes" id="checkbox-estacoes" />--%>
<%--                        <label for="checkbox-estacoes">Estações Meteorológicas</label>--%>
<%--                    </div>--%>
<%--                    <div class="checkbox-label">--%>
<%--                        <form:checkbox path="exibirCentroides" id="checkbox-centroides" />--%>
<%--                        <label for="checkbox-centroides">Centroides das Propriedades</label>--%>
<%--                    </div>--%>
<%--                </div>--%>

<%--                <div class="menu-flutuante-grupo">--%>
<%--                    <button type="submit" class="botao">Centralizar Visualização</button>--%>
<%--                </div>--%>
<%--            </form:form>--%>
<%--        </div>--%>
<%--    </div>--%>

<%--    <script id="dados-estacoes" type="application/json">${estacoesJson}</script>--%>
<%--    <script id="dados-propriedades" type="application/json">${propriedadesJson}</script>--%>
<%--    <script id="dados-propriedade-recente" type="application/json">${propriedadeMaisRecenteJson}</script>--%>

<%--    <script src="<c:url value='/js/mapaInterativo.js' />"></script>--%>
<%--    <script src="https://maps.googleapis.com/maps/api/js"></script>--%>
<%--</tags:corpo>--%>