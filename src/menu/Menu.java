package menu;

import util.EscritorUtil;
import util.LeitorUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Menu {

    protected final Map<Integer, Opcao> opcoes = new LinkedHashMap<>();
    protected final String titulo;
    protected boolean continuarNoMenu;

    public Menu(String titulo) {
        this.titulo = titulo;
    }

    protected abstract void configurarOpcoes();

    public void exibir() {
        if (opcoes.isEmpty()) {
            configurarOpcoes();
        }

        continuarNoMenu = true;
        while (continuarNoMenu) {
            mostrarOpcoes();
            int escolha = LeitorUtil.lerInteiroComIntervalo("Escolha uma opcão: ", 1, opcoes.size());
            tratarOpcoes(escolha);
        }
    }

    protected void mostrarOpcoes() {
        EscritorUtil.escreverEmNovaLinha("------------ " + titulo + " ------------");
        for (Map.Entry<Integer, Opcao> entrada : opcoes.entrySet()) {
            EscritorUtil.escreverEmNovaLinha(entrada.getKey() + ". " + entrada.getValue().descricao());
        }
    }

    protected void tratarOpcoes(int escolha) {
        Opcao opcaoSelecionada = opcoes.get(escolha);
        if (opcaoSelecionada != null) {
            opcaoSelecionada.acao().run();
        } else {
            EscritorUtil.exibirMensagemSeparada("Opção inválida.");
        }
    }

    protected void adicionarOpcao(int numero, String descricao, Runnable acao) {
        opcoes.put(numero, new Opcao(descricao, acao));
    }

    protected void voltar() {
        this.continuarNoMenu = false;
    }
}
