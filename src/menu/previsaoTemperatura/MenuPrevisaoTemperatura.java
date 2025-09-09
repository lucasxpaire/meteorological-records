package menu.previsaoTemperatura;

import dados.Dados;
import menu.Menu;
import modelo.Ponto;
import modelo.Temperatura;
import servico.PontoServico;
import servico.TemperaturaServico;
import util.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MenuPrevisaoTemperatura extends Menu {

    private static final int PREVER_POR_DATA_E_LOCAL = 1;
    private static final int VOLTAR = 2;
    public static final int MINUTO_ZERO = 0;

    private final TemperaturaServico temperaturaServico;
    private final PontoServico pontoServico;
    private final Dados dados;

    public MenuPrevisaoTemperatura(TemperaturaServico temperaturaServico, Dados dados) {
        super("Menu Previsão De Temperatura");
        this.temperaturaServico = temperaturaServico;
        this.pontoServico = new PontoServico(dados);
        this.dados = dados;
    }

    @Override
    protected void configurarOpcoes() {
        adicionarOpcao(PREVER_POR_DATA_E_LOCAL, "Prever temperatura por data e local", this::solicitarPrevisaoDeTemperatura);
        adicionarOpcao(VOLTAR, "Voltar", this::voltar);
    }

    private void solicitarPrevisaoDeTemperatura() {
        dados.iniciarTransacao();
        try {
            EscritorUtil.escreverEmNovaLinha("------ Previsão de Temperatura para Data e Hora Específica ------");
            Ponto ponto = LeitorUtil.lerPonto();
            ponto.setFusoHorario(ponto.obterFusoHorario());

            LocalDate dataSelecionada = LeitorUtil.lerData("Digite a data de previsão a partir do dia atual no formato (dd/MM/yyyy): ");
            int horaSelecionada = LeitorUtil.lerInteiroComIntervalo("Digite a hora para a previsão sem fuso horário (0-23): ", 0, 23);
            LocalDateTime dataHoraPrevisao = dataSelecionada.atTime(horaSelecionada, MINUTO_ZERO);
            Temperatura temperatura = temperaturaServico.preverTemperaturaParaPonto(ponto, dataHoraPrevisao);

            if (temperatura != null) {
                EscritorUtil.pularLinha();
                EscritorUtil.escreverEmNovaLinha("------ Temperatura prevista ------");
                EscritorUtil.escreverEmNovaLinha(" DataHora: " + temperatura.getDataHora().format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO) + " | Temperatura: " + FormatadorUtil.formatarPontoDecimalParaVirgula(temperatura.getTemperaturaPrevista()) + " °C");
                EscritorUtil.pularLinha();

                pontoServico.salvar(ponto);
                dados.confirmarTransacao();
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Não há dados de temperatura disponíveis para a previsão nessa coordenada.");
                dados.desfazerTransacao();
            }
        } catch (RuntimeException e) {
            dados.desfazerTransacao();
            EscritorUtil.exibirMensagemSeparada("Falha: " + e.getMessage());
        }
    }
}
