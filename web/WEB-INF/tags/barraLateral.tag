<%@tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="paginaAtiva" required="true" type="java.lang.String" %>

<nav class="menu-lateral">
    <div class="menu-lateral-cabecalho">
        <h3>GeoPropriedades</h3>
        <span>Sistema de Gerenciamento</span>
    </div>

    <ul class="menu-lateral-lista">
        <li>
            <a href="index.html" class="${paginaAtiva == 'inicio' ? 'active' : ''}">Início</a>
        </li>
        <li>
            <a href="gerenciarProprietarios.html" class="${paginaAtiva == 'proprietarios' ? 'active' : ''}">Gerenciar Proprietários</a>
        </li>
        <li>
            <a href="gerenciarPropriedades.html" class="${paginaAtiva == 'propriedades' ? 'active' : ''}">Gerenciar Propriedades</a>
        </li>
        <li>
            <a href="visualizarMapas.html" class="${paginaAtiva == 'mapas' ? 'active' : ''}">Visualizar Mapas</a>
        </li>
        <li>
            <a href="previsaoTemperatura.html" class="${paginaAtiva == 'previsao' ? 'active' : ''}">Previsão de Temperatura</a>
        </li>
    </ul>
</nav>
