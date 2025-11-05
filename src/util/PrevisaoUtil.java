package util;

import java.util.List;

public class PrevisaoUtil {

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
