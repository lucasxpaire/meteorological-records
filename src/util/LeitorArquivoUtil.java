package util;

import modelo.EstacaoMeteorologica;
import modelo.Temperatura;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static util.FormatadorUtil.FORMATADOR_DATA_HORA_PARA_COMPARACAO_CSV;

public class LeitorArquivoUtil {

    public static final Set<String> CABECALHO_VALIDO_DADOS_HISTORICOS = Set.of("Data;Hora UTC;PRECIPITAÇÃO TOTAL, HORÁRIO (mm);PRESSAO ATMOSFERICA AO NIVEL DA ESTACAO, HORARIA (mB);PRESSÃO ATMOSFERICA MAX.NA HORA ANT. (AUT) (mB);PRESSÃO ATMOSFERICA MIN. NA HORA ANT. (AUT) (mB);RADIACAO GLOBAL (Kj/m²);TEMPERATURA DO AR - BULBO SECO, HORARIA (°C);TEMPERATURA DO PONTO DE ORVALHO (°C);TEMPERATURA MÁXIMA NA HORA ANT. (AUT) (°C);TEMPERATURA MÍNIMA NA HORA ANT. (AUT) (°C);TEMPERATURA ORVALHO MAX. NA HORA ANT. (AUT) (°C);TEMPERATURA ORVALHO MIN. NA HORA ANT. (AUT) (°C);UMIDADE REL. MAX. NA HORA ANT. (AUT) (%);UMIDADE REL. MIN. NA HORA ANT. (AUT) (%);UMIDADE RELATIVA DO AR, HORARIA (%);VENTO, DIREÇÃO HORARIA (gr) (° (gr));VENTO, RAJADA MAXIMA (m/s);VENTO, VELOCIDADE HORARIA (m/s);");

    public static final int INTERVALO_DE_COMECO_DIA = 0;
    public static final String[] ANOS_VALIDOS = new String[]{"2020", "2021", "2022", "2023", "2024", "2025"};
    public static final int TAMANHO_DATA_SEM_ANO = 6;
    public static final int NENHUM_ARQUIVO = 0;

    public static List<String> listarCaminhosArquivosTemperaturasHistoricas(String codigoEstacao) {
        try {
            ClassLoader classLoader = LeitorArquivoUtil.class.getClassLoader();
            URL resourceUrl = classLoader.getResource("dadosHistoricos");

            if (resourceUrl == null) {
                throw new IllegalArgumentException("Pasta de dados históricos não encontrada no classpath: dadosHistoricos/");
            }

            File pastaRaiz = new File(resourceUrl.toURI());

            if (!pastaRaiz.exists()) {
                throw new IllegalArgumentException("Pasta de dados históricos não encontrada: " + pastaRaiz.getAbsolutePath());
            }

            List<String> caminhos = new ArrayList<>();
            File[] subpastas = pastaRaiz.listFiles(File::isDirectory);

            if (subpastas == null) {
                return caminhos;
            }

            for (File subpasta : subpastas) {
                File[] arquivos = subpasta.listFiles((dir, name) -> name.contains(codigoEstacao));
                if (arquivos != null && arquivos.length > NENHUM_ARQUIVO) {
                    caminhos.add(arquivos[0].getPath());
                }
            }

            if (caminhos.isEmpty()) {
                throw new RuntimeException("Não foi encontrado nenhum arquivo de temperaturas históricas com o código de estação: " + codigoEstacao);
            }

            return caminhos;

        } catch (Exception e) {
            throw new RuntimeException("Falha: Não foi possível acessar pasta de dados históricos.");
        }
    }

    public static List<Temperatura> lerTemperaturasHistoricasCsv(String dataDeBusca, String horaDeBusca, EstacaoMeteorologica estacaoMeteorologica) {
        List<String> caminhosArquivos = listarCaminhosArquivosTemperaturasHistoricas(estacaoMeteorologica.getCodigoEstacao());
        List<Temperatura> temperaturasHistoricas = new ArrayList<>();

        for (String caminhoArquivo : caminhosArquivos) {
            String dataEsperadaDoArquivoAtual = selecionarDataEsperadaDoArquivoAtual(dataDeBusca, caminhoArquivo);

            try (Scanner leitorArquivo = new Scanner(new File(caminhoArquivo), "Windows-1252")) {
                Map<String, Integer> mapaColunasRelevantes = new HashMap<>();

                boolean encontrouCabecalho = false;

                while (leitorArquivo.hasNextLine()) {
                    String linhaAtual = leitorArquivo.nextLine().trim();

                    if (!encontrouCabecalho) {
                        if (CABECALHO_VALIDO_DADOS_HISTORICOS.contains(linhaAtual)) {
                            String[] cabecalho = linhaAtual.split(";");
                            for (int i = 0; i < cabecalho.length; i++) {
                                mapaColunasRelevantes.put(cabecalho[i].trim(), i);
                            }
                            encontrouCabecalho = true;
                        }
                        continue;
                    }

                    if (linhaAtual.isEmpty()) {
                        continue;
                    }

                    String[] colunas = linhaAtual.split(";");

                    Integer idxData = mapaColunasRelevantes.get("Data");
                    Integer idxHora = mapaColunasRelevantes.get("Hora UTC");
                    Integer idxTemp = mapaColunasRelevantes.get("TEMPERATURA DO AR - BULBO SECO, HORARIA (°C)");

                    if (idxData == null || idxHora == null || idxTemp == null) {
                        continue;
                    }

                    String colunaData;
                    if (idxData < colunas.length && !colunas[idxData].isBlank()) {
                        colunaData = FormatadorUtil.reordenarStringDataParaFormatacaoBrasileira(colunas[idxData]);
                    } else {
                        colunaData = dataEsperadaDoArquivoAtual;
                    }

                    String colunaHora;
                    if (idxHora < colunas.length && !colunas[idxHora].isBlank()) {
                        colunaHora = FormatadorUtil.removerSubPalavraUTC(colunas[idxHora]);
                    } else {
                        colunaHora = horaDeBusca;
                    }

                    String colunaTemperatura;
                    if (idxTemp < colunas.length && !colunas[idxTemp].isBlank()) {
                        colunaTemperatura = colunas[idxTemp];
                    } else {
                        colunaTemperatura = null;
                    }

                    if (dataEsperadaDoArquivoAtual == null) {
                        continue;
                    }

                    if (dataEsperadaDoArquivoAtual.contains(colunaData) && horaDeBusca.contains(colunaHora)) {
                        Temperatura temperatura = new Temperatura();
                        if (colunaTemperatura == null) {
                            temperatura.setTemperaturaReal(null);
                        } else {
                            temperatura.setTemperaturaReal(FormatadorUtil.StringParaDouble(colunaTemperatura));
                        }

                        LocalDateTime dataHora = LocalDateTime.parse(dataEsperadaDoArquivoAtual + " " +  horaDeBusca.substring(0, 2) + ":" + horaDeBusca.substring(2, 4), FORMATADOR_DATA_HORA_PARA_COMPARACAO_CSV);
                        temperatura.setDataHora(dataHora);

                        temperaturasHistoricas.add(temperatura);
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        temperaturasHistoricas.sort(Comparator.comparing(Temperatura::getDataHora));

        return temperaturasHistoricas.stream()
                .filter(t -> t.getTemperaturaReal() != null)
                .collect(Collectors.toList());
    }

    private static String selecionarDataEsperadaDoArquivoAtual(String dataDeBusca, String caminhoArquivo) {
        for (String ano : ANOS_VALIDOS) {
            if (caminhoArquivo.contains(ano)) {
                return dataDeBusca.substring(INTERVALO_DE_COMECO_DIA, TAMANHO_DATA_SEM_ANO) + ano;
            }
        }
        return null;
    }
}
