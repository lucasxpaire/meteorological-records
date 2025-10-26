<%@ tag pageEncoding="UTF-8" %>

<%@ include file="../jsp/cabecalho.jspf"%>

<%@ attribute name="paginaAtiva" required="true" type="java.lang.String" %>

<c:url var="urlInicio" value="inicio.html"/>
<c:url var="urlGerenciarProprietarios" value="gerenciarProprietarios.html"/>
<c:url var="urlGerenciarPropriedades" value="gerenciarPropriedades.html"/>
<c:url var="urlVisualizarMapa" value="visualizarMapa.html"/>

<nav class="menu-lateral">
    <div class="menu-lateral-cabecalho">
        <h3>Gerenciamento de propriedades</h3>
    </div>

    <ul class="menu-lateral-lista">
        <li>
            <tags:botao label="Início" href="${urlInicio}" css="${paginaAtiva == 'inicio' ? 'active' : ''}"/>
        </li>
        <li>
            <tags:botao label="Gerenciar proprietários" href="${urlGerenciarProprietarios}" css="${paginaAtiva == 'proprietarios' ? 'active' : ''}"/>
        </li>
        <li>
            <tags:botao label="Gerenciar propriedades" href="${urlGerenciarPropriedades}" css="${paginaAtiva == 'propriedades' ? 'active' : ''}"/>
        </li>
        <li>
            <tags:botao label="Visualizar mapa" href="${urlVisualizarMapa}" css="${paginaAtiva == 'mapa' ? 'active' : ''}"/>
        </li>
    </ul>

    <div class="menu-lateral-rodape">
        <h3>Gerenciamento de propriedades</h3>
        <span>© Todos os direitos reservados</span>
    </div>
</nav>
