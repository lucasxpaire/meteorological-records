package menu.proprietario;

import menu.Menu;
import modelo.Proprietario;
import servico.ProprietarioServico;
import util.EscritorUtil;
import util.LeitorUtil;

import java.util.List;

public class SubMenuListagemProprietario extends Menu {

    public static final int LISTAR_TODOS = 1;
    public static final int LISTAR_MAIS_RECENTE = 2;
    public static final int LISTAR_POR_CPF = 3;
    public static final int VOLTAR = 4;

    private final ProprietarioServico proprietarioServico;

    public SubMenuListagemProprietario(String titulo, ProprietarioServico proprietarioServico) {
        super(titulo);
        this.proprietarioServico = proprietarioServico;
    }

    @Override
    protected void configurarOpcoes() {
        adicionarOpcao(LISTAR_TODOS, "Todos os proprietários", this::listarTodos);
        adicionarOpcao(LISTAR_MAIS_RECENTE, "Proprietário Mais Recentemente Adicionado", this::listarMaisRecente);
        adicionarOpcao(LISTAR_POR_CPF, "Buscar proprietário por CPF", this::listarPorCpf);
        adicionarOpcao(VOLTAR, "Voltar", this::voltar);
    }

    private void listarTodos() {
        try {
            if (proprietarioServico.existeAlgumProprietario()) {
                List<Proprietario> proprietarios = proprietarioServico.listarTodos();
                EscritorUtil.pularLinha();
                for (Proprietario proprietario : proprietarios) {
                    EscritorUtil.escreverEmNovaLinha("Proprietário: " + proprietario.getNome() + " | CPF: " + proprietario.getCpf() + " | Cor: " + proprietario.getCor().getNome() + " | Telefone: " + proprietario.getTelefone() + " | Total Propriedades: " + proprietario.obterNumeroTotalPropriedades());
                }
                EscritorUtil.pularLinha();
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Nenhum proprietário encontrado.");
                return;
            }
        } catch (RuntimeException e) {
            EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
        }
    }

    private void listarMaisRecente() {
        try {
            Proprietario proprietario = proprietarioServico.buscarUltimoAdicionado();
            EscritorUtil.exibirMensagemSeparada("Proprietário: " + proprietario.getNome() + " | CPF: " + proprietario.getCpf() + " | Cor: " + proprietario.getCor().getNome() + " | Telefone: " + proprietario.getTelefone() + " | Total Propriedades: " + proprietario.obterNumeroTotalPropriedades());
        } catch (RuntimeException e) {
            EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
        }
    }

    private void listarPorCpf() {
        String cpf = LeitorUtil.lerCpf("Digite o CPF: ");
        if (proprietarioServico.cpfJaExiste(cpf)) {
            try {
                Proprietario proprietario = proprietarioServico.buscarPorCpf(cpf);
                EscritorUtil.exibirMensagemSeparada("Proprietário: " + proprietario.getNome() + " | CPF: " + proprietario.getCpf() + " | Cor: " + proprietario.getCor().getNome() + " | Telefone: " + proprietario.getTelefone() + " | Total Propriedades: " + proprietario.obterNumeroTotalPropriedades());
            } catch (IllegalArgumentException e) {
                EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
            }
        } else {
            EscritorUtil.exibirMensagemSeparada("Falha: Nenhum proprietário encontrado.");
            return;
        }
    }
}
