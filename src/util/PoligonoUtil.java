package util;

import modelo.Poligono;
import modelo.Ponto;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PoligonoUtil {

    public static final String TIPO_MANUAL = "manual";
    public static final String TIPO_ARQUIVO = "arquivo";
    public static final int DUAS_PARTES = 2;
    public static final String CABECALHO_VALIDO = "lat;long";
    public static final int LATITUDE = 0;
    public static final int LONGITUDE = 1;

    public static Poligono criarPoligonoPorArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Falha: Nenhuma coordenada ou arquivo foi fornecido.");
        }

        List<Ponto> pontos = new ArrayList<>();
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(arquivo.getInputStream()))) {
            String linha;
            int numeroLinha = 0;

            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;

                if (linha.equalsIgnoreCase(CABECALHO_VALIDO) || linha.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linha.split(";");
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
                    Double latitude = FormatadorUtil.StringParaDouble(partes[LATITUDE]);
                    Double longitude = FormatadorUtil.StringParaDouble(partes[LONGITUDE]);

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
