package util;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import modelo.RegistroMeteorologico;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PrevisaoUtil {

    private static final int LIMITE_INTERPOLACAO = 6;

    private static final int JANELA_SARIMA_TREINO = 168;

    private static final ArimaParams PARAMS_SARIMA_IMPUTACAO = new ArimaParams(1, 0, 0, 1, 0, 0, 24);

    public static void limparJanelaDePrevisao(List<RegistroMeteorologico> janelaParaLimpar, String codigoEstacao, Function<RegistroMeteorologico, Double> getter, BiConsumer<RegistroMeteorologico, Double> setter) throws URISyntaxException {
        for (int indiceAtual = 0; indiceAtual < janelaParaLimpar.size(); indiceAtual++) {

            if (getter.apply(janelaParaLimpar.get(indiceAtual)) != null) {
                continue;
            }

            int indiceInicioBuraco = indiceAtual;
            int tamanhoDoBuraco = 0;

            while (indiceAtual < janelaParaLimpar.size() && getter.apply(janelaParaLimpar.get(indiceAtual)) == null) {
                tamanhoDoBuraco++;
                indiceAtual++;
            }

            boolean temValorAnterior = (indiceInicioBuraco > 0);
            boolean temValorPosterior = (indiceAtual < janelaParaLimpar.size());

            if (temValorAnterior && temValorPosterior && tamanhoDoBuraco <= LIMITE_INTERPOLACAO) {
                preencherComInterpolacaoLinear(janelaParaLimpar, indiceInicioBuraco, tamanhoDoBuraco, getter, setter);
            } else {
                preencherComSarima(janelaParaLimpar, indiceInicioBuraco, tamanhoDoBuraco, codigoEstacao, getter, setter);
            }

        }
    }

    public static void preencherComInterpolacaoLinear(List<RegistroMeteorologico> listaRegistros, int indiceInicioBuraco, int tamanhoDoBuraco, Function<RegistroMeteorologico, Double> getter, BiConsumer<RegistroMeteorologico, Double> setter) {
        double valorAnterior = getter.apply(listaRegistros.get(indiceInicioBuraco - 1));
        double valorPosterior = getter.apply(listaRegistros.get(indiceInicioBuraco + tamanhoDoBuraco));

        int distanciaEntreValores = tamanhoDoBuraco + 1;
        double incrementoPorPasso = (valorPosterior - valorAnterior) / distanciaEntreValores;

        for (int i = 0; i < tamanhoDoBuraco; i++) {
            double valorPreenchido = valorAnterior + incrementoPorPasso * (i + 1);
            setter.accept(listaRegistros.get(indiceInicioBuraco + i), valorPreenchido);
        }
    }

    public static void preencherComSarima(List<RegistroMeteorologico> listaRegistros, int indiceInicioBuraco, int tamanhoDoBuraco, String codigoEstacao, Function<RegistroMeteorologico, Double> getter, BiConsumer<RegistroMeteorologico, Double> setter) throws URISyntaxException {
        LocalDateTime dataHoraFimTreino = listaRegistros.get(indiceInicioBuraco).getDataHora().minusHours(1);
        LocalDateTime dataHoraInicioTreino = dataHoraFimTreino.minusHours(JANELA_SARIMA_TREINO - 1);

        List<RegistroMeteorologico> novaJanelaTreino = LeitorArquivoUtil.lerJanelaDeRegistros(codigoEstacao, dataHoraInicioTreino, dataHoraFimTreino);

        limparJanelaDePrevisao(novaJanelaTreino, codigoEstacao, getter, setter);

        double[] dadosDeTreino = novaJanelaTreino.stream()
                .mapToDouble(getter::apply)
                .toArray();

        ForecastResult previsao = Arima.forecast_arima(dadosDeTreino, tamanhoDoBuraco, PARAMS_SARIMA_IMPUTACAO);
        double[] valoresPrevistos = previsao.getForecast();

        for (int i = 0; i < tamanhoDoBuraco; i++) {
            setter.accept(listaRegistros.get(indiceInicioBuraco + i), valoresPrevistos[i]);
        }
    }

}
