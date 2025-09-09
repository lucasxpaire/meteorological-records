package menu.proprietario;

import menu.Menu;
import modelo.Cor;
import modelo.Proprietario;
import servico.CorServico;
import servico.ProprietarioServico;
import util.CorUtil;
import util.EscritorUtil;
import util.LeitorUtil;

import java.util.List;

public class SubMenuAlteracaoProprietario extends Menu {

    public static final int ALTERAR_NOME = 1;
    public static final int ALTERAR_CPF = 2;
    public static final int ALTERAR_TELEFONE = 3;
    public static final int ALTERAR_COR = 4;
    public static final int VOLTAR = 5;

    private final Proprietario proprietario;
    private final ProprietarioServico proprietarioServico;
    private final CorServico corServico;

    public SubMenuAlteracaoProprietario(String titulo, Proprietario proprietario, ProprietarioServico proprietarioServico, CorServico corServico) {
        super(titulo);
        this.proprietario = proprietario;
        this.proprietarioServico = proprietarioServico;
        this.corServico = corServico;
    }

    @Override
    protected void configurarOpcoes() {
        adicionarOpcao(ALTERAR_NOME, "Alterar Nome", this::alterarNome);
        adicionarOpcao(ALTERAR_CPF, "Alterar CPF", this::alterarCpf);
        adicionarOpcao(ALTERAR_TELEFONE, "Alterar Telefone", this::alterarTelefone);
        adicionarOpcao(ALTERAR_COR, "Alterar Cor Padrão", this::alterarCor);
        adicionarOpcao(VOLTAR, "Voltar", this::voltar);
    }

    private void alterarNome() {
        String novoNome = LeitorUtil.lerNome("Digite o novo nome: ");
        proprietario.setNome(novoNome);
        proprietarioServico.salvar(proprietario);
        EscritorUtil.exibirMensagemSeparada("Nome alterado com sucesso!");
    }

    private void alterarCpf() {
        String novoCpf = LeitorUtil.lerCpf("Digite o novo CPF: ");
        if (proprietarioServico.cpfJaExiste(novoCpf)) {
            EscritorUtil.exibirMensagemSeparada("Falha: CPF já existe.");
            return;
        }
        proprietario.setCpf(novoCpf);
        proprietarioServico.salvar(proprietario);
        EscritorUtil.exibirMensagemSeparada("CPF alterado com sucesso!");
    }

    private void alterarTelefone() {
        String novoTelefone = LeitorUtil.lerTelefone("Digite o novo telefone: ");
        proprietario.setTelefone(novoTelefone);
        proprietarioServico.salvar(proprietario);
        EscritorUtil.exibirMensagemSeparada("Telefone alterado com sucesso!");
    }

    private void alterarCor() {
        List<Cor> coresDisponiveis = corServico.listarTodos();
        CorUtil.exibirCoresDisponiveis(coresDisponiveis);
        int escolhaCor = LeitorUtil.lerInteiroComIntervalo("Digite a nova cor: ", 1, coresDisponiveis.size());
        Cor novaCor = CorUtil.obterCorPorEscolha(coresDisponiveis, escolhaCor);
        proprietario.setCor(novaCor);
        proprietarioServico.salvar(proprietario);
        EscritorUtil.exibirMensagemSeparada("Cor padrão alterada com sucesso!");
    }
}
