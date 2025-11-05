package modelo;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import dados.Dados;
import org.junit.Test;

import servico.EstacaoMeteorologicaServico;
import servico.RegistroMeteorologicoServico;
import util.LeitorArquivoUtil;
import util.PrevisaoUtil;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static util.LeitorArquivoUtil.COLUNA_TEMPERATURA;

public class SarimaAprendizadoTest {

    public static final int p = 1;
    public static final int d = 0;
    public static final int q = 0;
    public static final int P = 1;
    public static final int D = 0;
    public static final int Q = 0;

    public static final int M_PERIODO_SAZONAL = 24;

    public static final int m = M_PERIODO_SAZONAL;

    public static final int forecastSize = 12;

    public static Ponto ponto = new Ponto(-29.72047902, -53.705404735);

    private final Dados dados = new Dados();
    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico = new EstacaoMeteorologicaServico(dados);
    private final RegistroMeteorologicoServico registroMeteorologicoServico = new RegistroMeteorologicoServico();

    @Test
    public void obterDados() throws URISyntaxException, FileNotFoundException {
        dados.iniciarTransacao();
        List<EstacaoMeteorologica> estacoes = estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto);
        dados.confirmarTransacao();

        for (EstacaoMeteorologica estacaoMeteorologica : estacoes) {
            if (estacaoMeteorologica.getCodigoEstacao().equals("A803")) {
                System.out.println("--- Testando SARIMA na Estação: " + estacaoMeteorologica.getCodigoEstacao() + " ---");
                String caminhoArquivo = LeitorArquivoUtil.obterCaminhoDoArquivoDaEstacao("2019", estacaoMeteorologica.getCodigoEstacao());
                if (!caminhoArquivo.isEmpty()) {
                    List<Double> temperaturasComGaps = LeitorArquivoUtil.obterDadosEmColunaEspecifica(caminhoArquivo, COLUNA_TEMPERATURA.toString());
                    System.out.println("Total de dados: " + temperaturasComGaps.size());

                    double[] dadosLimpos = PrevisaoUtil.preencherBuracosComInterpolacao(temperaturasComGaps);
                    System.out.println("Total dos dados após interpolação: " + dadosLimpos.length);

                    if (dadosLimpos.length == 0) {
                        return;
                    }

                    int pontosParaTreino = m * 7;
                    if (dadosLimpos.length < pontosParaTreino) {
                        System.out.println("Dados insuficientes para treinar (precisa de " + pontosParaTreino + ", tem " + dadosLimpos.length + ")");
                        return;
                    }

                    double[] janelaDeTreino = Arrays.copyOfRange(dadosLimpos, dadosLimpos.length - pontosParaTreino, dadosLimpos.length);

                    System.out.println("Usando " + janelaDeTreino.length + " pontos (7 dias) para treino.");

                    ArimaParams params = new ArimaParams(p, d, q, P, D, Q, m);
                    ForecastResult forecastResult = Arima.forecast_arima(janelaDeTreino, forecastSize, params);
                    double[] valoresPrevistos = forecastResult.getForecast();

                    System.out.println("\n--- Previsão (próximas " + forecastSize + " horas) ---");
                    System.out.println("Última temperatura real (para referência): " + String.format("%.4f", janelaDeTreino[janelaDeTreino.length - 1]));

                    for (int i = 0; i < valoresPrevistos.length; i++) {
                        System.out.printf("Previsão [t+%d]: %.4f\n", (i + 1), valoresPrevistos[i]);
                    }
                }
            }
        }
    }

    @Test
    public void listarRegistrosMeteorologicos() throws URISyntaxException {
        dados.iniciarTransacao();
        List<EstacaoMeteorologica> estacoesAssociadas = estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto);
        dados.confirmarTransacao();

        for (EstacaoMeteorologica estacaoMeteorologica : estacoesAssociadas) {
            if (estacaoMeteorologica.getCodigoEstacao().equals("A803")) {
                System.out.println("--- Testando SARIMA na Estação: " + estacaoMeteorologica.getCodigoEstacao() + " ---");
                String caminhoArquivo = LeitorArquivoUtil.obterCaminhoDoArquivoDaEstacao("2024", estacaoMeteorologica.getCodigoEstacao());

                if (!caminhoArquivo.isEmpty()) {
                    List<RegistroMeteorologico> registrosMeteorologicos = LeitorArquivoUtil.lerRegistrosMeteorologicos(caminhoArquivo);
                    System.out.println("Total de dados: " + registrosMeteorologicos.size());

                    for (RegistroMeteorologico registroMeteorologico : registrosMeteorologicos) {
                        System.out.format("DataHora: %s | Precipitação: %f | Radiação Solar: %f | Temperatura: %f \n", registroMeteorologico.getDataHora(), registroMeteorologico.getPrecipitacaoReal(), registroMeteorologico.getRadiacaoSolarReal(), registroMeteorologico.getTemperaturaReal());
                    }
                }
            }
        }
    }
}
