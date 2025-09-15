<%@page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="proprietarios"/>
        <main class="estrutura-pagina-conteudo">

            <div class="estrutura-pagina-cabecalho">
                <h1>Gerenciar Proprietários</h1>
                <a href="cadastroProprietario.html" class="botao botao-novo">Novo Proprietário</a>
            </div>

            <table class="tabela">
                <thead>
                    <th>Nome</th>
                    <th>CPF</th>
                    <th>Telefone</th>
                    <th>Cor</th>
                    <th>Ações</th>
                </thead>
                <tbody>
                    <c:forEach var="proprietario" items="${proprietarios}">
                        <tr>
                            <td>${proprietario.nome}</td>
                            <td>${proprietario.cpf}</td>
                            <td>${proprietario.telefone}</td>
                            <td>${proprietario.cor.nome}</td>
                            <td>
                                <a href="alterarProprietario.html?id=${proprietario.id}" class="botao-acao botao-alterar">Alterar</a>
                                <a href="deletarProprietario.html?id=${proprietario.id}" class="botao-acao botao-deletar">Deletar</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

        </main>
    </div>
</tags:corpo>