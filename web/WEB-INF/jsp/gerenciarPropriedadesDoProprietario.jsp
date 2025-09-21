<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tags:corpo>
    <div class="estrutura-pagina">
        <main class="estrutura-pagina-conteudo">

            <div class="estrutura-pagina-cabecalho">
                <h1>Propriedades do Proprietário: ${proprietario.nome}</h1>
                <a href="cadastroPropriedade.html?proprietarioId=${proprietario.id}" class="botao botao-novo">Nova Propriedade</a>
                <a href="gerenciarProprietarios.html" class="botao botao-novo">Voltar</a>
            </div>

            <div class="busca-container">
                <form action="gerenciarPropriedadesDoProprietario.html" method="get" class="formulario-busca">
                    <input type="hidden" name="proprietarioId" value="${proprietario.id}" />
                    <input type="text" name="busca" id="campo-busca" placeholder="Buscar por nome..." class="campo-busca" value="${param.busca}">
                    <button type="submit" class="botao-busca">Buscar</button>
                </form>
            </div>

            <table class="tabela">
                <thead>
                    <th>Nome</th>
                    <th>Ações</th>
                </thead>
                <tbody>
                    <c:forEach var="propriedade" items="${propriedades}">
                        <tr>
                            <td>${propriedade.nome}</td>
                            <td>
                                <a href="alterarPropriedade.html?id=${propriedade.id}" class="botao-acao botao-alterar">Alterar</a>
                                <a href="deletarPropriedade.html?id=${propriedade.id}" class="botao-acao botao-deletar">Deletar</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </main>
    </div>
</tags:corpo>