<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<%--@elvariable id="sucesso" type="java.lang.String"--%>

<tags:corpo>
    <div class="estrutura-pagina">
        <main class="estrutura-pagina-conteudo">
            <tags:barraLateral paginaAtiva="propriedades" />

            <div class="estrutura-pagina-cabecalho">
                <h1>Propriedades do ${proprietario.nome}</h1>
                <div>
                    <a href="cadastroPropriedade.html?idProprietario=${proprietario.id}" class="botao botao-novo">Nova Propriedade</a>
                    <a href="gerenciarProprietarios.html" class="botao botao-novo">Voltar</a>
                </div>
            </div>

            <tags:alerta css="alerta-sucesso" alerta="${sucesso}" />

            <div class="busca-container">
                <form action="gerenciarPropriedadesDoProprietario.html" method="get" class="formulario-busca">
                    <input type="hidden" name="idProprietario" value="${proprietario.id}" />
                    <input type="text" name="busca" id="campo-busca" placeholder="Buscar por nome..." class="campo-busca" value="${param.busca}">
                    <button type="submit" class="botao-busca">Buscar</button>
                </form>
            </div>

            <table class="tabela">
                <thead>
                    <th>Nome</th>
                    <th>Cor da Propriedade</th>
                    <th>Ações</th>
                </thead>
                <tbody>
                    <c:forEach var="propriedade" items="${propriedades}">
                        <tr>
                            <td>${propriedade.nome}</td>
                            <td>${proprietario.cor.nome}</td>
                            <td>
                                <a href="alterarPropriedade.html?idPropriedade=${propriedade.id}" class="botao-tabela botao-tabela--alterar">Alterar</a>
                                <a href="deletarPropriedade.html?idPropriedade=${propriedade.id}&paginaOrigemRequisicao=gerenciarPropriedadesDoProprietario" class="botao-tabela botao-tabela--deletar">Deletar</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </main>
    </div>
</tags:corpo>