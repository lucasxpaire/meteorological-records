<%@ page pageEncoding="UTF-8" %>

<%@ include file="cabecalho.jspf" %>

<%--@elvariable id="sucesso" type="java.lang.String"--%>
<%--@elvariable id="falha" type="java.lang.String"--%>

<c:url var="urlCadastroPropriedade" value="cadastroPropriedade.html"/>
<c:url var="urlGerenciarPropriedades" value="gerenciarPropriedades.html"/>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="propriedades" />
        <main class="estrutura-pagina-conteudo">
            <div class="estrutura-pagina-cabecalho">
                <h1>Gerenciar propriedades</h1>
                <tags:botao label="Nova propriedade" css="botao botao-novo" href="${urlCadastroPropriedade}"/>
            </div>

            <tags:alerta css="alerta-sucesso" alerta="${sucesso}" />
            <tags:alerta css="alerta-falha" alerta="${falha}" />

            <div class="busca-container">
                <form action="${urlGerenciarPropriedades}" method="get" class="formulario-busca">
                    <input type="text" name="busca" id="campo-busca" placeholder="Buscar por nome de propriedade" class="campo-busca" value="${param.busca}">
                    <tags:botao label="Buscar" css="botao botao-busca" type="submit"/>
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
                            <c:url var="urlVisualizarPropriedade" value="visualizarPropriedade.html">
                                <c:param name="idPropriedade" value="${propriedade.id}"/>
                            </c:url>
                            <tags:botao label="Ver no mapa" css="botao-tabela botao-tabela--visualizar" href="${urlVisualizarPropriedade}"/>

                            <c:url var="urlAlterarPropriedade" value="alterarPropriedade.html">
                                <c:param name="idPropriedade" value="${propriedade.id}"/>
                            </c:url>
                            <tags:botao label="Alterar" css="botao-tabela botao-tabela--alterar" href="${urlAlterarPropriedade}"/>

                            <c:url var="urlDeletarPropriedade" value="deletarPropriedade.html">
                                <c:param name="idPropriedade" value="${propriedade.id}"/>
                            </c:url>
                            <tags:botao label="Deletar" css="botao-tabela botao-tabela--deletar" href="${urlDeletarPropriedade}"/>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </main>
</tags:corpo>