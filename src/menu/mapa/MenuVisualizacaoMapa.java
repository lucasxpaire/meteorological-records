package menu.mapa;

import menu.Menu;
import modelo.Propriedade;
import modelo.Proprietario;
import modelo.Temperatura;
import servico.EstacaoMeteorologicaServico;
import servico.PropriedadeServico;
import servico.ProprietarioServico;
import servico.TemperaturaServico;
import util.EscritorUtil;
import util.LeitorUtil;
import util.VisualizadorMapaUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MenuVisualizacaoMapa extends Menu {

    public static final int MAPA_TODAS_PROPRIEDADES = 1;
    public static final int MAPA_PROPRIEDADE_MAIS_RECENTE = 2;
    public static final int MAPA_PROPRIEDADES_POR_NOME = 3;
    public static final int MAPA_PROPRIEDADES_POR_CPF = 4;
    public static final int MAPA_PREVISOES_TEMPERATURA = 5;
    public static final int VOLTAR = 6;

    private final PropriedadeServico propriedadeServico;
    private final ProprietarioServico proprietarioServico;
    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico;
    private final TemperaturaServico temperaturaServico;

    public MenuVisualizacaoMapa(PropriedadeServico propriedadeServico, ProprietarioServico proprietarioServico, EstacaoMeteorologicaServico estacaoMeteorologicaServico, TemperaturaServico temperaturaServico) {
        super("Menu Visualização No Mapa");
        this.propriedadeServico = propriedadeServico;
        this.proprietarioServico = proprietarioServico;
        this.estacaoMeteorologicaServico = estacaoMeteorologicaServico;
        this.temperaturaServico = temperaturaServico;
    }

    @Override
    protected void configurarOpcoes() {
        adicionarOpcao(MAPA_TODAS_PROPRIEDADES, "Visualizar todas as propriedades", this::visualizarTodasPropriedades);
        adicionarOpcao(MAPA_PROPRIEDADE_MAIS_RECENTE, "Visualizar propriedade mais recentemente adicionada", this::visualizarPropriedadeMaisRecente);
        adicionarOpcao(MAPA_PROPRIEDADES_POR_NOME, "Selecionar propriedades por nome", this::visualizarPropriedadesPorNome);
        adicionarOpcao(MAPA_PROPRIEDADES_POR_CPF, "Visualizar propriedades por CPF do proprietário", this::visualizarPropriedadesPorCpfDoProprietario);
        adicionarOpcao(MAPA_PREVISOES_TEMPERATURA, "Visualizar previsões no mapa", this::visualizarPrevisoesMapa);
        adicionarOpcao(VOLTAR, "Voltar", this::voltar);
    }

    private void visualizarTodasPropriedades() {
        try {
            if (propriedadeServico.existeAlgumaPropriedade()) {
                List<Propriedade> propriedadesEncontradas = propriedadeServico.listarTodas();
                VisualizadorMapaUtil.visualizarPropriedadesNoMapa(propriedadesEncontradas, estacaoMeteorologicaServico);
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Nenhuma propriedade encontrada.");
            }
        } catch (RuntimeException e) {
            EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
        }
    }

    private void visualizarPropriedadeMaisRecente() {
        try {
            Propriedade propriedadeEncontrada = propriedadeServico.buscarMaisRecenteAdicionada();
            VisualizadorMapaUtil.visualizarPropriedadesNoMapa(List.of(propriedadeEncontrada), estacaoMeteorologicaServico);
        } catch (IllegalArgumentException e) {
            EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
        }
    }

    private void visualizarPropriedadesPorNome() {
        List<Propriedade> propriedadesParaVisualizar = new ArrayList<>();
        while (true) {
            if (!propriedadesParaVisualizar.isEmpty()) {
                EscritorUtil.escreverEmNovaLinha("Propriedades na lista de visualização:");
                for (Propriedade propriedade : propriedadesParaVisualizar) {
                    EscritorUtil.escreverEmNovaLinha("Propriedade: " + propriedade.getNome() + " | Proprietário: " + propriedade.getProprietario().getNome() + " | CPF: " + propriedade.getProprietario().getCpf());
                }
            }

            String entradaUsuario = LeitorUtil.lerNome("Digite o nome da propriedade para adicionar, 'visualizar' para gerar o mapa, ou 'voltar': ");
            if (entradaUsuario.equalsIgnoreCase("voltar")) {
                return;
            }

            if (entradaUsuario.equalsIgnoreCase("visualizar")) {
                if (propriedadesParaVisualizar.isEmpty()) {
                    EscritorUtil.exibirMensagemSeparada("Nenhuma propriedade selecionada para visualização.");
                    continue;
                }
                VisualizadorMapaUtil.visualizarPropriedadesNoMapa(propriedadesParaVisualizar, estacaoMeteorologicaServico);
                return;
            }

            if (!propriedadeServico.existeComEsseNome(entradaUsuario)) {
                EscritorUtil.exibirMensagemSeparada("Falha: Nenhuma propriedade encontrada com esse nome.");
                continue;
            }

            try {
                List<Propriedade> propriedadesEncontradas = propriedadeServico.buscarPorNome(entradaUsuario);
                List<Propriedade> novasPropriedadesParaAdicionar = propriedadesEncontradas.stream()
                        .filter(p -> !propriedadesParaVisualizar.contains(p))
                        .toList();

                if (!novasPropriedadesParaAdicionar.isEmpty()) {
                    propriedadesParaVisualizar.addAll(novasPropriedadesParaAdicionar);
                    EscritorUtil.exibirMensagemSeparada(novasPropriedadesParaAdicionar.size() + " propriedade(s) adicionada(s) à lista.");
                } else {
                    EscritorUtil.exibirMensagemSeparada("As propriedades encontradas já estão na lista.");
                }
            } catch (Exception e) {
                EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
            }
        }
    }

    private void visualizarPropriedadesPorCpfDoProprietario() {
        while (true) {
            String cpf = LeitorUtil.lerCpf("Digite o CPF do proprietário ou 'voltar': ");
            if (cpf.equalsIgnoreCase("voltar")) {
                return;
            }
            if (proprietarioServico.cpfJaExiste(cpf)) {
                try {
                    Proprietario proprietario = proprietarioServico.buscarPorCpf(cpf);
                    Set<Propriedade> propriedades = proprietario.getPropriedades();
                    if (propriedades.isEmpty()) {
                        EscritorUtil.exibirMensagemSeparada("Falha: Nenhuma propriedade encontrada para este proprietário.");
                    } else {
                        VisualizadorMapaUtil.visualizarPropriedadesNoMapa(propriedades, estacaoMeteorologicaServico);
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
                }
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Nenhum proprietário encontrado.");
            }
        }
    }

    private void visualizarPrevisoesMapa() {
        try {
            List<Temperatura> previsoes = temperaturaServico.listarTodasAsPrevisoes();
            if (previsoes.isEmpty()) {
                EscritorUtil.exibirMensagemSeparada("Nenhuma previsão do tempo foi encontrada para visualizar.");
            } else {
                VisualizadorMapaUtil.visualizarPrevisoesNoMapa(previsoes);
            }
        } catch (RuntimeException e) {
            EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
        }
    }
}
