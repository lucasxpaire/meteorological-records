<%@page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<%--@elvariable id="sucesso" type="java.lang.String"--%>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="propriedades" />
        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <h1>Gerenciar propriedades</h1>
                <a href="cadastroPropriedade.html" class="botao botao-novo">Nova propriedade</a>
            </div>

            <tags:alerta css="alerta-sucesso" alerta="${sucesso}" />

            <div class="busca-container">
                <form action="gerenciarPropriedades.html" method="get" class="formulario-busca">
                    <input type="text" name="busca" id="campo-busca" placeholder="Buscar por nome de propriedade" class="campo-busca" value="${param.busca}">
                    <button type="submit" class="botao-busca">Buscar</button>
                </form>
            </div>

            <table class="tabela">
                <thead>
                <th>Nome</th>
                <th>Cor</th>
                <th>Proprietário</th>
                <th>Ações</th>
                </thead>
                <tbody>
                <c:forEach var="propriedade" items="${propriedades}">
                    <tr>
                        <td>${propriedade.nome}</td>
                        <td>${propriedade.proprietario.cor.nome}</td>
                        <td>${propriedade.proprietario.nome}</td>
                        <td>
                            <a href="visualizarPropriedade.html?idPropriedade=${propriedade.id}" class="botao-tabela botao-tabela--visualizar">Ver no mapa</a>
                            <a href="alterarPropriedade.html?idPropriedade=${propriedade.id}" class="botao-tabela botao-tabela--alterar">Alterar</a>
                            <a href="deletarPropriedade.html?idPropriedade=${propriedade.id}" class="botao-tabela botao-tabela--deletar">Deletar</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </main>
</tags:corpo>