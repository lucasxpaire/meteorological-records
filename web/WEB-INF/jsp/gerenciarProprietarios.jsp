<%@page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<%--@elvariable id="sucesso" type="java.lang.String"--%>
<%--@elvariable id="falha" type="java.lang.String"--%>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="proprietarios"/>
        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <h1>Gerenciar proprietários</h1>
                <a href="cadastroProprietario.html" class="botao botao-novo">Novo proprietário</a>
            </div>

            <tags:alerta css="alerta-sucesso" alerta="${sucesso}" />
            <tags:alerta css="alerta-falha" alerta="${falha}" />

            <div class="busca-container">
                <form action="gerenciarProprietarios.html" method="get" class="formulario-busca">
                    <input type="text" name="busca" id="campo-busca" placeholder="Buscar por nome ou CPF" class="campo-busca" value="${param.busca}">
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
                                <a href="alterarProprietario.html?idProprietario=${proprietario.id}" class="botao-tabela botao-tabela--alterar">Alterar</a>
                                <a href="deletarProprietario.html?idProprietario=${proprietario.id}" class="botao-tabela botao-tabela--deletar">Deletar</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </main>
    </div>
    <script>
        const campoBusca = document.getElementById("campo-busca");

        campoBusca.addEventListener('input', function(event) {
            const input = event.target;
            let valor = input.value;

            const contemLetra = /[a-zA-Z]/.test(valor);

            if (contemLetra) {
                return;
            }

            const apenasNumeros = valor.replace(/\D/g, '');
            input.value = VMasker.toPattern(apenasNumeros, "999.999.999-99");
        });

    </script>
</tags:corpo>