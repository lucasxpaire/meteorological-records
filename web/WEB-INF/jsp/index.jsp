<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>

<tags:corpo>
    <div class="estrutura-pagina">
        <tags:barraLateral paginaAtiva="inicio"/>
        <main class="estrutura-pagina-conteudo">
            <h1>Painel de Controle</h1>
            <p>Visão geral e atalhos para as principais funcionalidades do sistema.</p>

            <div class="dashboard-container">
                <div class="dashboard-stats">
                    <div class="stat-card">
                        <h2>${totalProprietarios}</h2>
                        <span>Proprietários Cadastrados</span>
                    </div>
                    <div class="stat-card">
                        <h2>${totalPropriedades}</h2>
                        <span>Propriedades Registradas</span>
                    </div>
                    <div class="stat-card">
                        <h2>${totalEstacoes}</h2>
                        <span>Estações Monitoradas</span>
                    </div>
                </div>

                <h2>Ações Rápidas</h2>
                <div class="dashboard-actions">
                    <a href="gerenciarProprietarios.html" class="action-card">
                        <h3>Gerenciar Proprietários</h3>
                        <p>Adicionar, editar e visualizar todos os proprietários.</p>
                    </a>
                    <a href="gerenciarPropriedades.html" class="action-card">
                        <h3>Gerenciar Propriedades</h3>
                        <p>Cadastrar novas propriedades e gerenciar as existentes.</p>
                    </a>
                    <a href="visualizarMapas.html" class="action-card">
                        <h3>Visualizar Mapas</h3>
                        <p>Ver todas as propriedades e estações no mapa interativo.</p>
                    </a>
                    <a href="previsaoTemperatura.html" class="action-card">
                        <h3>Nova Previsão</h3>
                        <p>Fazer uma nova previsão de temperatura para um ponto específico.</p>
                    </a>
                </div>

                <c:if test="${not empty propriedadeMaisRecente}">
                    <h2>Atividade Recente</h2>
                    <div class="dashboard-recent">
                        <h4>Última Propriedade Adicionada</h4>
                        <p><b>Nome:</b> ${propriedadeMaisRecente.nome}</p>
                        <p><b>Proprietário:</b> ${propriedadeMaisRecente.proprietario.nome}</p>
                        <a href="visualizarMapas.html">Ver no mapa</a>
                    </div>
                </c:if>
            </div>
        </main>
    </div>
</tags:corpo>