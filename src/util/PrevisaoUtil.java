package util;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import modelo.RegistroMeteorologico;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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

            Double valorAnterior;
            if (indiceInicioBuraco == 0) {
                valorAnterior = null;
            } else {
                valorAnterior = getter.apply(janelaParaLimpar.get(indiceInicioBuraco - 1));
            }

            Double valorPosterior;
            if (indiceAtual == janelaParaLimpar.size()) {
                valorPosterior = null;
            } else {
                valorPosterior = getter.apply(janelaParaLimpar.get(indiceAtual));
            }

            if (tamanhoDoBuraco <= LIMITE_INTERPOLACAO && valorAnterior != null && valorPosterior != null) {
                preencherBuracosComInterpolacaoLinear(janelaParaLimpar, indiceInicioBuraco, tamanhoDoBuraco, setter, valorAnterior, valorPosterior);
            } else {
                preencherBuracosComSarima(janelaParaLimpar, indiceInicioBuraco, tamanhoDoBuraco, codigoEstacao, getter, setter, valorAnterior, valorPosterior);
            }

            if (indiceAtual < janelaParaLimpar.size()) {
                indiceAtual--;
            }
        }

    }

    public static void preencherBuracosComInterpolacaoLinear(List<RegistroMeteorologico> janela, int indiceInicioBuraco, int tamanhoDoBuraco, BiConsumer<RegistroMeteorologico, Double> setter, Double valorAnterior, Double valorPosterior) {

        int distanciaEntreValores = tamanhoDoBuraco + 1;
        double incrementoPorPasso = (valorPosterior - valorAnterior) / distanciaEntreValores;

        for (int i = 0; i < tamanhoDoBuraco; i++) {
            double valorPreenchido = valorAnterior + incrementoPorPasso * (i + 1);
            setter.accept(janela.get(indiceInicioBuraco + i), valorPreenchido);
        }
    }

    public static void preencherBuracosComSarima(List<RegistroMeteorologico> janela, int indiceInicioBuraco, int tamanhoDoBuraco, String codigoEstacao, Function<RegistroMeteorologico, Double> getter, BiConsumer<RegistroMeteorologico, Double> setter, Double valorAnterior, Double valorPosterior) throws URISyntaxException {

        double[] dadosDeTreino;
        List<RegistroMeteorologico> janelaDeTreino;
        if (indiceInicioBuraco >= JANELA_SARIMA_TREINO) {
            janelaDeTreino = janela.subList(indiceInicioBuraco - JANELA_SARIMA_TREINO, indiceInicioBuraco);
            dadosDeTreino = janelaDeTreino.stream()
                    .mapToDouble(getter::apply)
                    .toArray();
        } else {
            LocalDateTime dataHoraFimTreino = janela.get(indiceInicioBuraco).getDataHora().minusHours(1);
            LocalDateTime dataHoraInicioTreino = dataHoraFimTreino.minusHours(JANELA_SARIMA_TREINO - 1);
            janelaDeTreino = LeitorArquivoUtil.lerJanelaDeRegistros(codigoEstacao, dataHoraInicioTreino, dataHoraFimTreino);

            if (janelaDeTreino.size() < JANELA_SARIMA_TREINO) {
                preencherComFallback(janela, indiceInicioBuraco, tamanhoDoBuraco, setter, valorAnterior, valorPosterior);
                return;
            }

            limparJanelaDePrevisao(janelaDeTreino, codigoEstacao, getter, setter);

            dadosDeTreino = janelaDeTreino.stream()
                    .map(getter)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .toArray();

            if (dadosDeTreino.length < JANELA_SARIMA_TREINO) {
                preencherComFallback(janela, indiceInicioBuraco, tamanhoDoBuraco, setter, valorAnterior, valorPosterior);
                return;
            }
        }

        // Tenta treinar e prever com SARIMA
        try {
            ForecastResult previsao = Arima.forecast_arima(dadosDeTreino, tamanhoDoBuraco, PARAMS_SARIMA_IMPUTACAO);
            double[] valoresPrevistos = previsao.getForecast();

            for (int i = 0; i < tamanhoDoBuraco; i++) {
                setter.accept(janela.get(indiceInicioBuraco + i), valoresPrevistos[i]);
            }
        } catch (Exception e) {
            // Se SARIMA falhar (ex: dados insuficientes, etc.), usa fallback.
            preencherComFallback(janela, indiceInicioBuraco, tamanhoDoBuraco, setter, valorAnterior, valorPosterior);
        }
    }

    private static void preencherComFallback(List<RegistroMeteorologico> janela, int indiceInicio, int tamanho, BiConsumer<RegistroMeteorologico, Double> setter, Double valorAnterior, Double valorPosterior) {
        Double valorFallback = 0.0;
        if (valorAnterior != null) {
            valorFallback = valorAnterior;
        } else if (valorPosterior != null) {
            valorFallback = valorPosterior;
        }

        for (int i = 0; i < tamanho; i++) {
            setter.accept(janela.get(indiceInicio + i), valorFallback);
        }
    }

    public static double preverValorDoRegistroMeteorologico(List<RegistroMeteorologico> janelaDePrevisao, Function<RegistroMeteorologico, Double> getter) {
        double[] dadosDePrevisao = janelaDePrevisao.stream()
                .mapToDouble(getter::apply)
                .toArray();

        if (!temVarianciaSuficiente(dadosDePrevisao)) {
            return dadosDePrevisao[dadosDePrevisao.length - 1];
        }

        try {
            ForecastResult previsao = Arima.forecast_arima(dadosDePrevisao, 1, PARAMS_SARIMA_IMPUTACAO);
            double[] valorPrevisto = previsao.getForecast();
            return valorPrevisto[0];
        } catch (Exception e) {
            return dadosDePrevisao[dadosDePrevisao.length - 1];
        }
    }

    public static List<RegistroMeteorologico> criarCopiaProfunda(List<RegistroMeteorologico> originais) {
        return originais.stream()
                .map(PrevisaoUtil::copiarRegistro)
                .collect(Collectors.toList());
    }

    private static boolean temVarianciaSuficiente(double[] dados) {
        if (dados.length < 2) {
            return false;
        }
        double primeiroValor = dados[0];
        for (int i = 1; i < dados.length; i++) {
            if (dados[i] != primeiroValor) {
                return true;
            }
        }
        return false;
    }

    private static RegistroMeteorologico copiarRegistro(RegistroMeteorologico original) {
        RegistroMeteorologico copia = new RegistroMeteorologico();
        copia.setDataHora(original.getDataHora());

        copia.setTemperaturaReal(original.getTemperaturaReal());
        copia.setPrecipitacaoReal(original.getPrecipitacaoReal());
        copia.setRadiacaoSolarReal(original.getRadiacaoSolarReal());

        return copia;
    }

}
