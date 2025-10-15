<%@tag pageEncoding="UTF-8" %>
<%@ attribute name="paginaAtiva" required="true" type="java.lang.String" %>

<nav class="menu-lateral">
    <div class="menu-lateral-cabecalho">
        <h3>Gerenciamento de propriedades</h3>
    </div>

    <ul class="menu-lateral-lista">
        <li>
            <a href="index.html" class="${paginaAtiva == 'inicio' ? 'active' : ''}">Início</a>
        </li>
        <li>
            <a href="gerenciarProprietarios.html" class="${paginaAtiva == 'proprietarios' ? 'active' : ''}">Gerenciar proprietários</a>
        </li>
        <li>
            <a href="gerenciarPropriedades.html" class="${paginaAtiva == 'propriedades' ? 'active' : ''}">Gerenciar propriedades</a>
        </li>
        <li>
            <a href="visualizarMapa.html" class="${paginaAtiva == 'mapa' ? 'active' : ''}">Visualizar mapa</a>
        </li>
    </ul>

    <div class="menu-lateral-rodape">
        <h3>Gerenciamento de propriedades</h3>
        <span>© Todos os direitos reservados</span>
    </div>
</nav>
