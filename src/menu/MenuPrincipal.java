package menu;

import dados.Dados;
import menu.mapa.MenuVisualizacaoMapa;
import menu.previsaoTemperatura.MenuPrevisaoTemperatura;
import menu.propriedade.MenuGerenciamentoPropriedade;
import menu.proprietario.MenuGerenciamentoProprietario;
import servico.*;

public class MenuPrincipal extends Menu {

    public static final int GERENCIAR_PROPRIETARIOS = 1;
    public static final int GERENCIAR_PROPRIEDADES = 2;
    public static final int VISUALIZAR_MAPAS = 3;
    public static final int PREVISAO_TEMPERATURA = 4;
    public static final int SAIR = 5;

    private final MenuGerenciamentoProprietario menuGerenciamentoProprietario;
    private final MenuGerenciamentoPropriedade menuGerenciamentoPropriedade;
    private final MenuVisualizacaoMapa menuVisualizacaoMapa;
    private final MenuPrevisaoTemperatura menuPrevisaoTemperatura;

    public MenuPrincipal(ProprietarioServico proprietarioServico, PropriedadeServico propriedadeServico, CorServico corServico, EstacaoMeteorologicaServico estacaoMeteorologicaServico, TemperaturaServico temperaturaServico, Dados dados) {
        super("Menu Principal");
        this.menuGerenciamentoProprietario = new MenuGerenciamentoProprietario(proprietarioServico, corServico, dados);
        this.menuGerenciamentoPropriedade = new MenuGerenciamentoPropriedade(propriedadeServico, proprietarioServico, estacaoMeteorologicaServico, dados);
        this.menuVisualizacaoMapa = new MenuVisualizacaoMapa(propriedadeServico, proprietarioServico, estacaoMeteorologicaServico, temperaturaServico);
        this.menuPrevisaoTemperatura = new MenuPrevisaoTemperatura(temperaturaServico, dados);
    }

    @Override
    protected void configurarOpcoes() {
        adicionarOpcao(GERENCIAR_PROPRIETARIOS, "Gerenciar Proprietários", menuGerenciamentoProprietario::exibir);
        adicionarOpcao(GERENCIAR_PROPRIEDADES, "Gerenciar Propriedades", menuGerenciamentoPropriedade::exibir);
        adicionarOpcao(VISUALIZAR_MAPAS, "Visualizar Mapas", menuVisualizacaoMapa::exibir);
        adicionarOpcao(PREVISAO_TEMPERATURA, "Previsão de Temperatura", menuPrevisaoTemperatura::exibir);
        adicionarOpcao(SAIR, "Sair", () -> System.exit(0));
    }

}