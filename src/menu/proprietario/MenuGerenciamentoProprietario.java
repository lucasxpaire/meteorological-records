package menu.proprietario;

import dados.Dados;
import menu.Menu;
import modelo.Cor;
import modelo.Proprietario;
import servico.CorServico;
import servico.ProprietarioServico;
import util.CorUtil;
import util.EscritorUtil;
import util.LeitorUtil;

import java.util.List;

public class MenuGerenciamentoProprietario extends Menu {

    public static final int LISTAR = 1;
    public static final int CRIAR = 2;
    public static final int ALTERAR = 3;
    public static final int DELETAR = 4;
    public static final int VOLTAR = 5;

    private final ProprietarioServico proprietarioServico;
    private final CorServico corServico;
    private final Dados dados;

    private final SubMenuListagemProprietario menuListar;

    public MenuGerenciamentoProprietario(ProprietarioServico proprietarioServico, CorServico corServico, Dados dados) {
        super("Menu Proprietário");
        this.proprietarioServico = proprietarioServico;
        this.corServico = corServico;
        this.dados = dados;
        this.menuListar = new SubMenuListagemProprietario("Listar Proprietário", proprietarioServico);
    }

    @Override
    protected void configurarOpcoes() {
        adicionarOpcao(LISTAR, "Listar Proprietário", menuListar::exibir);
        adicionarOpcao(CRIAR, "Criar Proprietário", this::criarProprietario);
        adicionarOpcao(ALTERAR, "Alterar Proprietário", this::alterarProprietario);
        adicionarOpcao(DELETAR, "Deletar Proprietário", this::deletarProprietario);
        adicionarOpcao(VOLTAR, "Voltar", this::voltar);
    }

    private void criarProprietario() {
        EscritorUtil.escreverEmNovaLinha("------------ Criar Proprietário ------------");
        String nome = LeitorUtil.lerNome("Digite o nome ou 'voltar': ");
        if (nome.equalsIgnoreCase("voltar")) {
            return;
        }

        dados.iniciarTransacao();
        try {
            String cpf;
            while (true) {
                cpf = LeitorUtil.lerCpf("Digite o CPF: ");
                if (proprietarioServico.cpfJaExiste(cpf)) {
                    EscritorUtil.exibirMensagemSeparada("Falha: Esse CPF já foi salvo.");
                    continue;
                }
                break;
            }

            String telefone;
            while (true) {
                telefone = LeitorUtil.lerTelefone("Digite o telefone: ");
                if (proprietarioServico.telefoneJaExiste(telefone)) {
                    EscritorUtil.exibirMensagemSeparada("Falha: Esse telefone já foi salvo.");
                    continue;
                }
                break;
            }

            List<Cor> coresDisponiveis = corServico.listarTodos();
            CorUtil.exibirCoresDisponiveis(coresDisponiveis);
            int escolhaCor = LeitorUtil.lerInteiroComIntervalo("Digite o número da cor desejada: ", 1, coresDisponiveis.size());
            Cor cor = CorUtil.obterCorPorEscolha(coresDisponiveis, escolhaCor);

            Proprietario proprietario = new Proprietario();
            proprietario.setNome(nome);
            proprietario.setCpf(cpf);
            proprietario.setCor(cor);
            proprietario.setTelefone(telefone);
            proprietarioServico.salvar(proprietario);

            dados.confirmarTransacao();
            EscritorUtil.exibirMensagemSeparada("Proprietário criado com sucesso!");
        } catch (Exception e) {
            dados.desfazerTransacao();
            EscritorUtil.exibirMensagemSeparada("Falha: Não foi possível criar proprietário: " + e.getMessage());
        }
    }

    private void alterarProprietario() {
        EscritorUtil.escreverEmNovaLinha("------------ Alterar Proprietário ------------");
        String cpf = LeitorUtil.lerCpf("Digite o CPF do proprietário: ");

        dados.iniciarTransacao();
        try {
            if (proprietarioServico.cpfJaExiste(cpf)) {
                Proprietario proprietario = proprietarioServico.buscarPorCpf(cpf);

                Menu menuAlterar = new SubMenuAlteracaoProprietario("Alterar Proprietário", proprietario, proprietarioServico, corServico);
                menuAlterar.exibir();

                dados.confirmarTransacao();
                EscritorUtil.exibirMensagemSeparada("Alterações salvas com sucesso!");
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Nenhum proprietário encontrado.");
                dados.desfazerTransacao();
            }
        } catch (Exception  e) {
            dados.desfazerTransacao();
            EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
        }
    }

    private void deletarProprietario() {
        EscritorUtil.escreverEmNovaLinha("------------ Deletar Proprietário ------------");
        String cpf = LeitorUtil.lerCpf("Digite o CPF do proprietário: ");

        dados.iniciarTransacao();
        try {
            if (proprietarioServico.cpfJaExiste(cpf)) {
                Proprietario proprietario = proprietarioServico.buscarPorCpf(cpf);
                if (proprietario.getPropriedades().isEmpty()) {
                    EscritorUtil.exibirMensagemSeparada("Falha: Este proprietário possui " + proprietario.getPropriedades().size() + " propriedade(s) e não pode ser deletado.");
                    dados.desfazerTransacao();
                    return;
                }
                String confirmacao = LeitorUtil.lerConfirmacao("Tem certeza que deseja deletar '" + proprietario.getNome() + "'? (S/N): ");
                if (confirmacao.equalsIgnoreCase("S")) {
                    proprietarioServico.deletar(proprietario);
                    EscritorUtil.exibirMensagemSeparada("Proprietário deletado com sucesso!");
                    dados.confirmarTransacao();
                } else {
                    dados.desfazerTransacao();
                }
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Nenhum proprietário encontrado.");
                dados.desfazerTransacao();
            }
        } catch (Exception e) {
            dados.desfazerTransacao();
            EscritorUtil.exibirMensagemSeparada("Falha ao deletar proprietário: " + e.getMessage());
        }
    }

}
