<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="inicio"/>
        <main class="estrutura-pagina-conteudo">
            <h1>Início</h1>
            <p>Visualize as estatísticas, atualizações de temperaturas e ações rápidas.</p>

            <div class="dashboard-container">
                <h2>Estatísticas</h2>
                <div class="dashboard-stats">
                    <div class="stat-card">
                        <h2>${totalProprietarios}</h2>
                        <span>Proprietários cadastrados</span>
                    </div>
                    <div class="stat-card">
                        <h2>${totalPropriedades}</h2>
                        <span>Propriedades cadastradas</span>
                    </div>
                    <div class="stat-card">
                        <h2>${totalEstacoes}</h2>
                        <span>Estações meteorológicas monitoradas</span>
                    </div>
                </div>

                <h2>Atualização de temperaturas</h2>
                <div class="dashboard-stats">
                    <div class="stat-card routine-card">
                        <h2>Última atualização de temperaturas</h2>
                        <span>${ultimaAtualizacaoEstacoesECentroides}</span>
                    </div>
                </div>

                <div class="dashboard-stats">
                    <div class="stat-card routine-card">
                        <h2>Próxima atualização de temperaturas</h2>
                        <span>${proximaAtualizacao}</span>
                    </div>
                </div>

                <h2>Ações rápidas</h2>
                <div class="dashboard-actions">
                    <a href="cadastroProprietario.html" class="action-card">
                        <h3>Cadastro proprietário</h3>
                        <p>Adicione um novo proprietário no sitema.</p>
                    </a>
                    <a href="cadastroPropriedade.html" class="action-card">
                        <h3>Cadastro propriedade</h3>
                        <p>Adicione uma nova propriedade no sistema.</p>
                    </a>
                </div>

            </div>
        </main>
    </div>
</tags:corpo>