package menu.propriedade;

import dados.Dados;
import menu.Menu;
import modelo.*;
import servico.EstacaoMeteorologicaServico;
import servico.PropriedadeServico;
import servico.ProprietarioServico;
import util.EscritorUtil;
import util.LeitorUtil;
import util.PoligonoUtil;

import java.util.List;

public class MenuGerenciamentoPropriedade extends Menu {

    public static final int LISTAR = 1;
    public static final int CRIAR = 2;
    public static final int ALTERAR = 3;
    public static final int DELETAR = 4;
    public static final int VOLTAR = 5;

    public static final int SELECAO_UNICA = 1;

    private final PropriedadeServico propriedadeServico;
    private final ProprietarioServico proprietarioServico;
    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico;
    private final Dados dados;

    private final SubMenuListagemPropriedade menuListar;

    public MenuGerenciamentoPropriedade(PropriedadeServico propriedadeServico, ProprietarioServico proprietarioServico, EstacaoMeteorologicaServico estacaoMeteorologicaServico, Dados dados) {
        super("Menu Propriedade");
        this.propriedadeServico = propriedadeServico;
        this.proprietarioServico = proprietarioServico;
        this.estacaoMeteorologicaServico = estacaoMeteorologicaServico;
        this.dados = dados;
        this.menuListar = new SubMenuListagemPropriedade("Listar Propriedades", propriedadeServico);
    }

    @Override
    protected void configurarOpcoes() {
        adicionarOpcao(LISTAR, "Listar Propriedades", menuListar::exibir);
        adicionarOpcao(CRIAR, "Criar Propriedade", this::criarPropriedade);
        adicionarOpcao(ALTERAR, "Alterar Propriedade", this::alterarPropriedade);
        adicionarOpcao(DELETAR, "Deletar Propriedade", this::deletarPropriedade);
        adicionarOpcao(VOLTAR, "Voltar", this::voltar);
    }

    private void criarPropriedade() {
        dados.iniciarTransacao();
        try {
            EscritorUtil.escreverEmNovaLinha("------------ Criar Propriedade ------------");
            if (proprietarioServico.existeAlgumProprietario()) {
                String entradaUsuario = LeitorUtil.lerNome("Digite o nome da propriedade ou 'voltar': ");
                if (entradaUsuario.equalsIgnoreCase("voltar")) {
                    dados.desfazerTransacao();
                    return;
                }

                String cpf = LeitorUtil.lerCpf("Digite o CPF do proprietário: ");

                if (proprietarioServico.cpfJaExiste(cpf)) {
                    Proprietario proprietario = proprietarioServico.buscarPorCpf(cpf);
                    Poligono poligono = PoligonoUtil.obterCriacaoDePoligono();

                    Propriedade propriedade = new Propriedade();
                    propriedade.setNome(entradaUsuario);
                    propriedade.setProprietario(proprietario);
                    propriedade.setPoligono(poligono);

                    Ponto novoCentroide = propriedade.getPoligono().calcularCentroide();
                    List<EstacaoMeteorologica> estacoes = estacaoMeteorologicaServico.buscarEstacoesRelevantes(novoCentroide);

                    novoCentroide.setEstacoesMeteorologicas(estacoes);

                    Temperatura novaTemperatura = novoCentroide.interpolarTemperaturaAtual(estacoes);
                    novoCentroide.getHistoricoTemperaturas().add(novaTemperatura);

                    propriedade.setCentroide(novoCentroide);
                    propriedadeServico.salvar(propriedade);

                    EscritorUtil.exibirMensagemSeparada("Propriedade criada com sucesso!");
                    dados.confirmarTransacao();
                } else {
                    EscritorUtil.exibirMensagemSeparada("Falha: Nenhum proprietário encontrado.");
                    dados.desfazerTransacao();
                }
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Nenhum proprietário disponível. Crie um proprietário primeiro.");
                dados.desfazerTransacao();
            }
        } catch (Exception e) {
            dados.desfazerTransacao();
            EscritorUtil.exibirMensagemSeparada("Falha: Não foi possível criar propriedade: " + e.getMessage());
        }
    }

    private void alterarPropriedade() {
        EscritorUtil.escreverEmNovaLinha("------------ Alterar Propriedade ------------");
        Propriedade propriedade = selecionarPropriedade();

        if (propriedade == null) {
            return;
        }

        dados.iniciarTransacao();
        try {
            Menu menuAlterar = new SubMenuAlteracaoPropriedade("Alterar Propriedade", propriedade, propriedadeServico, estacaoMeteorologicaServico);
            menuAlterar.exibir();

            dados.confirmarTransacao();
            EscritorUtil.exibirMensagemSeparada("Alterações salvas com sucesso!");
        } catch (Exception e) {
            dados.desfazerTransacao();
            EscritorUtil.exibirMensagemSeparada("Falha: ocorreu um erro e todas as alterações foram desfeitas: " + e.getMessage());
        }
    }

    private void deletarPropriedade() {
        EscritorUtil.escreverEmNovaLinha("------------ Deletar Propriedade ------------");
        Propriedade propriedade = selecionarPropriedade();

        if (propriedade == null) {
            return;
        }

        dados.iniciarTransacao();
        try {
            String confirmacao = LeitorUtil.lerConfirmacao("Tem certeza que deseja deletar a propriedade '" + propriedade.getNome() + "'? (S/N): ");
            if (confirmacao.equalsIgnoreCase("S")) {

                if (propriedade.getCentroide() != null) {
                    propriedade.getCentroide().getHistoricoTemperaturas().clear();
                }
                if (propriedade.getPoligono() != null) {
                    propriedade.getPoligono().getPontos().clear();
                }

                propriedadeServico.deletar(propriedade);
                dados.confirmarTransacao();
                EscritorUtil.exibirMensagemSeparada("Propriedade deletada com sucesso!");
            } else {
                EscritorUtil.exibirMensagemSeparada("Operação cancelada.");
                dados.desfazerTransacao();
            }
        } catch (Exception e) {
            dados.desfazerTransacao();
            EscritorUtil.exibirMensagemSeparada("Falha: Não foi possível deletar propriedade: " + e.getMessage());
        }
    }

    private Propriedade selecionarPropriedade() {
        while (true) {
            String entradaUsuario = LeitorUtil.lerNome("Digite o nome da propriedade ou 'voltar': ");
            if (entradaUsuario.equalsIgnoreCase("voltar")) {
                return null;
            }

            if (propriedadeServico.existeComEsseNome(entradaUsuario)) {
                List<Propriedade> propriedades = propriedadeServico.buscarPorNome(entradaUsuario);
                if (propriedades.isEmpty()) {
                    EscritorUtil.exibirMensagemSeparada("Falha: Nenhuma propriedade encontrada com esse nome.");
                    continue;
                }

                if (propriedades.size() == SELECAO_UNICA) {
                    return propriedades.getFirst();
                } else {
                    EscritorUtil.escreverEmNovaLinha("Mais de uma propriedade encontrada:");
                    for (int i = 0; i < propriedades.size(); i++) {
                        Propriedade p = propriedades.get(i);
                        EscritorUtil.escreverEmNovaLinha((i + 1) + ". " + p.getNome() + " | Proprietário: " + p.getProprietario().getNome());
                    }
                    int opcaoVoltar = propriedades.size() + 1;
                    EscritorUtil.escreverEmNovaLinha(opcaoVoltar + ". Voltar");
                    int escolha = LeitorUtil.lerInteiroComIntervalo("Escolha uma opção (1-" + opcaoVoltar + "): ", 1, opcaoVoltar);
                    if (escolha == opcaoVoltar) {
                        continue;
                    }
                    return propriedades.get(escolha - 1);
                }
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Não existe propriedade com esse nome.");
            }
        }
    }
}
