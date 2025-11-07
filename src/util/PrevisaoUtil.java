package util;

import modelo.RegistroMeteorologico;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PrevisaoUtil {

    private static final int LIMITE_INTERPOLACAO = 6;

    public static List<RegistroMeteorologico> limparJanelaDePrevisao(List<RegistroMeteorologico> registrosComBuracos, String codigoEstacao, Function<RegistroMeteorologico, Double> getter, BiConsumer<RegistroMeteorologico, Double> setter) {

        List<RegistroMeteorologico> registrosLimpos = new ArrayList<>();

        for (int indiceAtual = 0; indiceAtual < registrosComBuracos.size(); indiceAtual++) {

            RegistroMeteorologico registroAtual = registrosComBuracos.get(indiceAtual);
            if (getter.apply(registroAtual) != null) {
                registrosLimpos.add(registroAtual);
                continue;
            }

            int indiceInicioBuraco = indiceAtual;
            int tamanhoDoBuraco = 0;

            while (indiceAtual < registrosComBuracos.size() && getter.apply(registrosComBuracos.get(indiceAtual)) == null) {
                indiceAtual++;
                tamanhoDoBuraco++;
            }

            List<RegistroMeteorologico> registrosComBuracosPreenchidos;
            if (tamanhoDoBuraco <= LIMITE_INTERPOLACAO) {
                registrosComBuracosPreenchidos = preencherComInterpolacaoLinear(registrosComBuracos, indiceInicioBuraco, tamanhoDoBuraco, getter, setter);
            } else {
                registrosComBuracosPreenchidos = preencherComSarima(registrosComBuracos, indiceInicioBuraco, tamanhoDoBuraco, codigoEstacao, getter, setter);
            }

            registrosLimpos.addAll(registrosComBuracosPreenchidos);

            indiceAtual--;
        }

        return registrosLimpos;
    }

    public static List<RegistroMeteorologico> preencherComInterpolacaoLinear(List<RegistroMeteorologico> registrosComDadosNulos, int indiceAtual, int tamanhoDoBuraco, Function<RegistroMeteorologico, Double> getter, BiConsumer<RegistroMeteorologico, Double> setter) {

    }

    public static List<RegistroMeteorologico> preencherComSarima(List<RegistroMeteorologico> registrosComDadosNulos, int indiceAtual, int tamanhoDoBuraco, String codigoEstacao, Function<RegistroMeteorologico, Double> getter, BiConsumer<RegistroMeteorologico, Double> setter) {

    }

    public static double[] preencherBuracosComInterpolacao(List<Double> dadosComValoresAusentes) {
        double[] dadosPreenchidos = new double[dadosComValoresAusentes.size()];

        for (int indiceAtual = 0; indiceAtual < dadosComValoresAusentes.size(); indiceAtual++) {
            if (Double.isNaN(dadosComValoresAusentes.get(indiceAtual))) {

                if (indiceAtual == 0) {
                    int indicePrimeiroNumeroValido = 0;
                    while (indicePrimeiroNumeroValido < dadosComValoresAusentes.size() && Double.isNaN(dadosComValoresAusentes.get(indicePrimeiroNumeroValido))) {
                        indicePrimeiroNumeroValido++;
                    }

                    double primeiroNumeroValido;
                    if (indicePrimeiroNumeroValido == dadosComValoresAusentes.size()) {
                        primeiroNumeroValido = 0.0;
                    } else {
                        primeiroNumeroValido = dadosComValoresAusentes.get(indicePrimeiroNumeroValido);
                    }

                    for(int j = 0; j < indicePrimeiroNumeroValido; j++) {
                        dadosPreenchidos[j] = primeiroNumeroValido;
                    }
                    indiceAtual = indicePrimeiroNumeroValido - 1;
                    continue;
                }

                double valorAnterior = dadosPreenchidos[indiceAtual - 1];
                int indiceValorAnterior = indiceAtual - 1;

                int indiceProximoValor = indiceAtual;
                while (indiceProximoValor < dadosComValoresAusentes.size() && Double.isNaN(dadosComValoresAusentes.get(indiceProximoValor))) {
                    indiceProximoValor++;
                }

                double valorPosterior;
                if (indiceProximoValor == dadosComValoresAusentes.size()) {
                    valorPosterior = valorAnterior;
                } else {
                    valorPosterior = dadosComValoresAusentes.get(indiceProximoValor);
                }

                int numeroDePassosNoBuraco = indiceProximoValor - indiceValorAnterior;
                double incrementoPorPasso = (valorPosterior - valorAnterior) / numeroDePassosNoBuraco;

                for (int indiceBuraco = indiceAtual; indiceBuraco < indiceProximoValor; indiceBuraco++) {
                    int distanciaDesdeOValorAnterior = indiceBuraco - indiceValorAnterior;
                    dadosPreenchidos[indiceBuraco] = valorAnterior + (incrementoPorPasso * distanciaDesdeOValorAnterior);
                }

                indiceAtual = indiceProximoValor - 1;

            } else {
                dadosPreenchidos[indiceAtual] = dadosComValoresAusentes.get(indiceAtual);
            }
        }
        return dadosPreenchidos;
    }

}
