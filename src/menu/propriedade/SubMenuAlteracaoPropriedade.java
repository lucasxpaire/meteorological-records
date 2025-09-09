package menu.propriedade;

import menu.Menu;
import modelo.*;
import servico.EstacaoMeteorologicaServico;
import servico.PropriedadeServico;
import util.EscritorUtil;
import util.LeitorUtil;
import util.PoligonoUtil;

import java.util.List;

public class SubMenuAlteracaoPropriedade extends Menu {

    public static final int ALTERAR_NOME = 1;
    public static final int ALTERAR_POLIGONO = 2;
    public static final int VOLTAR = 3;

    private final Propriedade propriedade;
    private final PropriedadeServico propriedadeServico;
    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    public SubMenuAlteracaoPropriedade(String titulo, Propriedade propriedade, PropriedadeServico propriedadeServico, EstacaoMeteorologicaServico estacaoMeteorologicaServico) {
        super(titulo);
        this.propriedade = propriedade;
        this.propriedadeServico = propriedadeServico;
        this.estacaoMeteorologicaServico = estacaoMeteorologicaServico;
    }

    @Override
    protected void configurarOpcoes() {
        adicionarOpcao(ALTERAR_NOME, "Alterar Nome", this::alterarNome);
        adicionarOpcao(ALTERAR_POLIGONO, "Alterar Polígono", this::alterarPoligono);
        adicionarOpcao(VOLTAR, "Voltar", this::voltar);
    }

    private void alterarNome() {
        String novoNome = LeitorUtil.lerNome("Digite o novo nome: ");
        propriedade.setNome(novoNome);
        propriedadeServico.salvar(propriedade);
        EscritorUtil.exibirMensagemSeparada("Nome alterado com sucesso!");
    }

    private void alterarPoligono() {
        Poligono novoPoligono = PoligonoUtil.obterCriacaoDePoligono();
        propriedade.setPoligono(novoPoligono);

        Ponto novoCentroide = propriedade.getPoligono().calcularCentroide();
        List<EstacaoMeteorologica> estacoes = estacaoMeteorologicaServico.buscarEstacoesRelevantes(novoCentroide);

        novoCentroide.setEstacoesMeteorologicas(estacoes);

        Temperatura novaTemperatura = novoCentroide.interpolarTemperaturaAtual(estacoes);
        novoCentroide.getHistoricoTemperaturas().add(novaTemperatura);

        propriedade.setCentroide(novoCentroide);
        propriedadeServico.salvar(propriedade);

        EscritorUtil.exibirMensagemSeparada("Polígono alterado com sucesso!");
    }
}
