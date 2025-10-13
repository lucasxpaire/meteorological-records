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
                <h3>Controle do mapa</h3>

                <div class="menu-flutuante-grupo">
                    <div class="menu-flutuante-grupo">
                        <tags:selectFormulario path="opcaoSelecionada" label="Opções de busca" items="${opcoesControleMapa}" descricao="Selecione uma opção" />
                    </div>

                    <div class="menu-flutuante-grupo-escondido">
                        <tags:inputFormulario path="cpfBusca" label="CPF do proprietário:" />
                    </div>

                    <div class="menu-flutuante-grupo-escondido">
                        <tags:inputFormulario path="nomeBusca" label="Nome de propriedade:" />
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
        const urlPrevisao = '<c:url value="/preverTemperatura" />';
    </script>
    <script src="<c:url value='/js/mapa.js' />"></script>
    <script src="https://maps.googleapis.com/maps/api/js"></script>
</tags:corpo>
