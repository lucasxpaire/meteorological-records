<%@page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="proprietarios"/>
        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <h1>Gerenciar Proprietários</h1>
                <a href="cadastroProprietario.html" class="botao botao-novo">Novo Proprietário</a>
            </div>

            <c:if test="${not empty resultado}">
                <div class="alerta-sucesso">
                    ${resultado}
                </div>
            </c:if>

            <div class="busca-container">
                <form action="gerenciarProprietarios.html" method="get" class="formulario-busca">
                    <input type="text" name="busca" id="campo-busca" placeholder="Buscar por nome ou CPF..." class="campo-busca" value="${param.busca}">
                    <button type="submit" class="botao-busca">Buscar</button>
                </form>
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
                            <td>${proprietario.cpfFormatado} </td>
                            <td>${proprietario.telefoneFormatado}</td>
                            <td>${proprietario.cor.nome}</td>
                            <td>
                                <a href="gerenciarPropriedadesDoProprietario.html?idProprietario=${proprietario.id}" class="botao-tabela botao-tabela--adicionar">Gerenciar Propriedades</a>
                                <a href="alterarProprietario.html?idProprietario=${proprietario.id}" class="botao-tabela botao-tabela--alterar">Alterar</a>
                                <a href="deletarProprietario.html?idProprietario=${proprietario.id}" class="botao-tabela botao-tabela--deletar">Deletar</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </main>
    </div>
</tags:corpo>