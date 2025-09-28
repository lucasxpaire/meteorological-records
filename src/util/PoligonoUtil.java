package util;

import modelo.Poligono;
import modelo.Ponto;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PoligonoUtil {

    public static final int INSERCAO_MANUAL = 1;
    public static final int ARQUIVO_CSV = 2;

    public static final String TIPO_MANUAL = "manual";
    public static final String TIPO_ARQUIVO = "arquivo";

    public static Poligono criarPoligonoPorArquivo(MultipartFile arquivo) {
        List<Ponto> pontos = new ArrayList<>();

        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(arquivo.getInputStream()))) {
            String linha;

            while ((linha = leitor.readLine()) != null) {
                if (linha.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linha.split(";");
                Double latitude = FormatadorUtil.StringParaDouble(partes[0]);
                Double longitude = FormatadorUtil.StringParaDouble(partes[1]);
                pontos.add(new Ponto(latitude, longitude));
            }
            return new Poligono(pontos);
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    public static Poligono obterCriacaoDePoligono() {
        while (true) {
            EscritorUtil.escreverEmNovaLinha("------ Opções de Criação ------");
            EscritorUtil.escreverEmNovaLinha("1. Inserção manual");
            EscritorUtil.escreverEmNovaLinha("2. Arquivo CSV");
            int escolhaUsuario = LeitorUtil.lerInteiroComIntervalo("Escolha entre (1-2): ", INSERCAO_MANUAL, ARQUIVO_CSV);

            Poligono poligono = switch (escolhaUsuario) {
                case INSERCAO_MANUAL -> criarPoligonoManual();
                case ARQUIVO_CSV -> criarPoligonoPorCSV();
                default -> throw new IllegalStateException("Escolha inválida.");
            };

            if (poligono == null) {
                return null;
            }

            if (poligono.possuiAutoIntersecao()) {
                EscritorUtil.escreverEmNovaLinha("Falha: O polígono possui auto-interseção. Tente novamente.");
            } else {
                return poligono;
            }
        }
    }

    private static Poligono criarPoligonoManual() {
        List<Ponto> pontos = LeitorUtil.lerListaManual();

        if (pontos == null) {
            return null;
        }

        Poligono poligono = new Poligono();
        poligono.setPontos(pontos);

        if (poligono.possuiAutoIntersecao()) {
            EscritorUtil.escreverEmNovaLinha("Falha: O polígono possui auto-interseção.");
        }

        EscritorUtil.escreverEmNovaLinha("Polígono criado com " + poligono.getPontos().size() + " arestas.");
        return poligono;
    }

    private static Poligono criarPoligonoPorCSV() {
        String caminhoArquivo = LeitorArquivoUtil.obterCaminhoArquivoDePontos();
        if (caminhoArquivo == null) {
            return null;
        }

        try {
            List<Ponto> pontos = LeitorArquivoUtil.lerPontosDoCSV(caminhoArquivo);
            if (pontos.size() < Poligono.QUANTIDADE_MINIMA_DE_PONTOS) {
                throw new RuntimeException("Um polígono precisa de pelo menos 3 pontos.");
            }
            Poligono poligono = new Poligono();
            poligono.setPontos(pontos);

            if (poligono.possuiAutoIntersecao()) {
                EscritorUtil.escreverEmNovaLinha("Falha: O polígono possui auto-interseção.");
            }

            EscritorUtil.escreverEmNovaLinha("Polígono criado com " + poligono.getPontos().size() + " arestas.");
            return poligono;

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
