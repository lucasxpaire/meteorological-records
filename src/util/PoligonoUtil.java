package util;

import modelo.Arquivo;
import modelo.Poligono;
import modelo.Ponto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PoligonoUtil {

    private static final int DUAS_PARTES = 2;
    private static final String CABECALHO_VALIDO = "lat;long";
    private static final int LATITUDE = 0;
    private static final int LONGITUDE = 1;
    private static final String PONTO = ".";
    private static final String PONTO_E_VIRGULA = ";";

    public static Poligono criarPoligonoPorArquivo(Arquivo arquivo) {
        if (arquivo == null) {
            throw new IllegalArgumentException("Falha: Nenhuma coordenada ou arquivo foi fornecido.");
        }

        List<Ponto> pontos = new ArrayList<>();
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(arquivo.getConteudo())))) {
            String linha;
            int numeroLinha = 0;

            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;

                if (linha.equalsIgnoreCase(CABECALHO_VALIDO) || linha.trim().isEmpty()) {
                    continue;
                }

                if (linha.matches(".*\\p{L}.*")) {
                    throw new IllegalArgumentException("Falha: Na linha " + numeroLinha + ". Digite apenas números.");
                }

                if (linha.contains(PONTO)) {
                    throw new IllegalArgumentException("Falha: Na linha " + numeroLinha + ". Use vírgula ao invés de ponto como separador decimal.");
                }

                String[] partes = linha.split(PONTO_E_VIRGULA);
                if (partes.length < DUAS_PARTES) {
                    throw new IllegalArgumentException("Falha: Na linha " + numeroLinha + ". Formato inválido. Use o formato 'latitude;longitude' (ex: -29,7181;-53,8225).");
                }

                if (partes[LATITUDE].isEmpty()) {
                    throw new IllegalArgumentException("Falha: Na linha " + numeroLinha + ". Latitude não pode ser vazia.");
                }

                if (partes[LONGITUDE].isEmpty()) {
                    throw new IllegalArgumentException("Falha: Na linha " + numeroLinha + ". Longitude não pode ser vazia.");
                }

                try {
                    Double latitude = FormatadorUtil.converterStringParaDouble(partes[LATITUDE]);
                    Double longitude = FormatadorUtil.converterStringParaDouble(partes[LONGITUDE]);

                    if (!Ponto.validarLatitude(latitude)) {
                        throw new IllegalArgumentException("Falha: Na linha " + numeroLinha + ". A Latitude deve estar entre -90 e 90.");
                    }

                    if (!Ponto.validarLongitude(longitude)) {
                        throw new IllegalArgumentException("Falha: Na linha " + numeroLinha + ". A Longitude deve estar entre -180 e 180.");
                    }

                    pontos.add(new Ponto(latitude, longitude));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Falha: Na linha " + numeroLinha + ". A coordenada não possui um número válido.");
                }
            }
            return new Poligono(pontos);
        } catch (IOException e) {
            throw new IllegalArgumentException("Falha: Ocorreu um erro ao ler os dados das coordenadas. Verifique o arquivo.");
        }
    }

}
