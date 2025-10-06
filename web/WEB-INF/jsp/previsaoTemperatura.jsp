<%@page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="previsao"/>

        <div class="estrutura-pagina-conteudo">
            <div id="map"></div>
        </div>

        <div class="menu-flutuante">
            <h3>Previsão de Temperatura</h3>

            <tags:alerta alerta="${alertaFalha}" css="alerta-erro-formulario"/>

            <form:form modelAttribute="previsaoCommand" method="post" action="previsaoTemperatura.html" id="form-previsao">
                <div class="menu-flutuante-grupo">
                    <tags:inputFormulario path="latitude" label="Latitude" placeholder="Ex: -29.6841"/>
                </div>

                <div class="menu-flutuante-grupo">
                    <tags:inputFormulario path="longitude" label="Longitude" placeholder="Ex: -53.8011"/>
                </div>

                <div class="menu-flutuante-grupo">
                    <label for="data">Data</label>
                    <input type="date" id="data" name="data" value="${previsaoCommand.data}" class="form-control"/>
                    <form:errors path="data" cssClass="alerta-erro-formulario" />
                </div>

                <div class="menu-flutuante-grupo">
                    <label for="hora">Hora</label>
                    <select id="hora" name="hora" class="form-control" size="6">
                        <option value="" disabled ${empty previsaoCommand.hora ? 'selected' : ''}>Selecione a hora</option>
                        <c:forEach var="h" begin="0" end="23">
                            <option value="${h}" ${previsaoCommand.hora == h ? 'selected' : ''}>
                                <fmt:formatNumber value="${h}" minIntegerDigits="2" />:00
                            </option>
                        </c:forEach>
                    </select>
                    <form:errors path="hora" cssClass="alerta-erro-formulario" />
                </div>

                <div class="menu-flutuante-grupo">
                    <button type="submit" class="botao">Prever Temperatura</button>
                </div>
            </form:form>
        </div>
    </div>

    <script id="dados-estacoes" type="application/json">${estacoesJson}</script>
    <script id="dados-previsao" type="application/json">${previsaoJson}</script>

    <script src="<c:url value='/js/previsaoTemperaturaMapa.js'/>"></script>
    <script src="https://maps.googleapis.com/maps/api/js?"></script>
</tags:corpo>