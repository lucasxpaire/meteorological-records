<%@page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="propriedades" />
        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <h1>Gerenciar Propriedades</h1>
                <a href="cadastroPropriedade.html" class="botao botao-novo">Nova Propriedade</a>
            </div>

            <c:if test="${not empty resultado}">
                <div class="alerta-sucesso">
                        ${resultado}
                </div>
            </c:if>

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
                            <a href="alterarPropriedade.html?idPropriedade=${propriedade.id}" class="botao-tabela botao-tabela--alterar">Alterar</a>
                            <a href="deletarPropriedade.html?idPropriedade=${propriedade.id}&paginaOrigemRequisicao=gerenciarPropriedades" class="botao-tabela botao-tabela--deletar">Deletar</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </main>
</tags:corpo>