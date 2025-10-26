<%@ page pageEncoding="UTF-8" %>

<%@ include file="cabecalho.jspf" %>

<%--@elvariable id="sucesso" type="java.lang.String"--%>
<%--@elvariable id="falha" type="java.lang.String"--%>

<c:url var="urlCadastroProprietario" value="cadastroProprietario.html"/>
<c:url var="urlGerenciarProprietarios" value="gerenciarProprietarios.html"/>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:menuLateral paginaAtiva="proprietarios"/>
        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <h1>Gerenciar proprietários</h1>
                <tags:botao label="Novo proprietário" css="botao botao-novo" href="${urlCadastroProprietario}"/>
            </div>

            <tags:alerta css="alerta-sucesso" alerta="${sucesso}" />
            <tags:alerta css="alerta-falha" alerta="${falha}" />

            <div class="busca-container">
                <form action="${urlGerenciarProprietarios}" method="get" class="formulario-busca">
                    <input type="text" name="busca" id="campo-busca" placeholder="Buscar por nome ou CPF" class="campo-busca" value="${param.busca}">
                    <tags:botao label="Buscar" css="botao botao-busca" type="submit"/>
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
                                <c:url var="urlAlterar" value="/alterarProprietario.html">
                                    <c:param name="idProprietario" value="${proprietario.id}"/>
                                </c:url>
                                <tags:botao label="Alterar" css="botao-tabela botao-tabela--alterar" href="${urlAlterar}"/>

                                <c:url var="urlDeletar" value="deletarProprietario.html">
                                    <c:param name="idProprietario" value="${proprietario.id}"/>
                                </c:url>
                                <tags:botao label="Deletar" css="botao-tabela botao-tabela--deletar" href="${urlDeletar}"/>
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