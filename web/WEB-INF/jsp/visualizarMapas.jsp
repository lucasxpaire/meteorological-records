<%@page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="mapas"/>

        <div class="estrutura-pagina-conteudo">
            <div id="map"></div>
        </div>

        <div class="menu-flutuante">
            <h3>Controles do Mapa</h3>

            <div class="menu-flutuante-grupo">
                <label for="select-propriedade">Visualizar Propriedade</label>
                <select id="select-propriedade">
                    <option value="todas">Todas as Propriedades</option>
                    <option value="recente">Propriedade Mais Recente</option>
                    <option value="nome">Buscar por nome</option>
                    <option value="cpf">Buscar por cpf do proprietário</option>
                </select>
            </div>

            <div class="menu-flutuante-grupo" id="grupo-busca">
                <tags:inputGenerico id="input-busca" label="Termo de Busca" type="text" />
                <tags:botao id="botao-buscar" css="botao" label="Buscar" />
            </div>

            <div class="menu-flutuante-grupo">
                <label>Exibir Camadas:</label>
                <tags:checkbox id="checkbox-estacoes" label="Estações Meteorológicas" css="checkbox-label" type="checkbox" checked="true" />
                <tags:checkbox id="checkbox-centroides" label="Centroides das Propriedades" css="checkbox-label" type="checkbox" checked="true" />
                <tags:checkbox id="checkbox-raio-relevancia" label="Raio de Relevância" css="checkbox-label" type="checkbox" checked="true" />
            </div>

            <div class="menu-flutuante-grupo">
                <tags:botao id="botao-resetar-zoom" label="Centralizar Visualização" css="botao" />
            </div>

        </div>
    </div>

    <script id="dados-estacoes" type="application/json">${estacoesJson}</script>
    <script id="dados-propriedades" type="application/json">${propriedadesJson}</script>
    <script id="dados-propriedade-recente" type="application/json">${propriedadeMaisRecenteJson}</script>

    <script src="<c:url value='/js/mapaInterativo.js' />"></script>

    <script src="https://maps.googleapis.com/maps/api/js"></script>

</tags:corpo>