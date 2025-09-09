package menu.propriedade;

import menu.Menu;
import modelo.Propriedade;
import servico.PropriedadeServico;
import util.EscritorUtil;
import util.LeitorUtil;

import java.util.List;

public class SubMenuListagemPropriedade extends Menu {

    public static final int LISTAR_TODAS = 1;
    public static final int LISTAR_RECENTE = 2;
    public static final int LISTAR_POR_NOME = 3;
    public static final int VOLTAR = 4;

    private final PropriedadeServico propriedadeServico;

    public SubMenuListagemPropriedade(String titulo, PropriedadeServico propriedadeServico) {
        super(titulo);
        this.propriedadeServico = propriedadeServico;
    }

    @Override
    protected void configurarOpcoes() {
        adicionarOpcao(LISTAR_TODAS, "Todas as propriedades", this::listarTodas);
        adicionarOpcao(LISTAR_RECENTE, "Propriedade Mais Recentemente Adicionada", this::listarRecente);
        adicionarOpcao(LISTAR_POR_NOME, "Buscar propriedade por nome", this::listarPorNome);
        adicionarOpcao(VOLTAR, "Voltar", this::voltar);
    }

    private void listarTodas() {
        try {
            if (propriedadeServico.existeAlgumaPropriedade()) {
                List<Propriedade> propriedades = propriedadeServico.listarTodas();
                EscritorUtil.pularLinha();
                for (Propriedade propriedade : propriedades) {
                    EscritorUtil.escreverEmNovaLinha("Propriedade: " + propriedade.getNome() + " | Proprietário: " + propriedade.getProprietario().getNome() + " | CPF: " + propriedade.getProprietario().getCpf());
                }
                EscritorUtil.pularLinha();
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Nenhuma propriedade encontrada.");
                return;
            }
        } catch (RuntimeException e) {
            EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
        }
    }

    private void listarRecente() {
        try {
            Propriedade propriedade = propriedadeServico.buscarMaisRecenteAdicionada();
            EscritorUtil.exibirMensagemSeparada("Propriedade: " + propriedade.getNome() + " | Proprietário: " + propriedade.getProprietario().getNome() + " | CPF: " + propriedade.getProprietario().getCpf());
        } catch (RuntimeException e) {
            EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
        }
    }

    private void listarPorNome() {
        while (true) {
            String entradaUsuario = LeitorUtil.lerNome("Digite o nome da propriedade ou 'voltar': ");
            if (entradaUsuario.equalsIgnoreCase("voltar")) {
                return;
            }
            if (propriedadeServico.existeComEsseNome(entradaUsuario)) {
                try {
                    List<Propriedade> propriedades = propriedadeServico.buscarPorNome(entradaUsuario);
                    if (propriedades.isEmpty()) {
                        EscritorUtil.exibirMensagemSeparada("Falha: Nenhuma propriedade encontrada com esse nome.");
                    } else {
                        EscritorUtil.pularLinha();
                        for (Propriedade propriedade : propriedades) {
                            EscritorUtil.escreverEmNovaLinha("Propriedade: " + propriedade.getNome() + " | Proprietário: " + propriedade.getProprietario().getNome() + " | CPF: " + propriedade.getProprietario().getCpf());
                        }
                        EscritorUtil.pularLinha();
                    }
                } catch (IllegalArgumentException e) {
                    EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
                }
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Não existe alguma propriedade com esse nome.");
            }
        }
    }
}
