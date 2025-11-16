package util;

import modelo.EstacaoMeteorologica;
import modelo.RegistroMeteorologico;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import static util.FormatadorUtil.*;

public class LeitorArquivoUtil {

    private static final Set<String> CABECALHO_VALIDO_REGISTROS_METEOROLOGICOS = Set.of("Data;Hora UTC;PRECIPITAÇÃO TOTAL, HORÁRIO (mm);PRESSAO ATMOSFERICA AO NIVEL DA ESTACAO, HORARIA (mB);PRESSÃO ATMOSFERICA MAX.NA HORA ANT. (AUT) (mB);PRESSÃO ATMOSFERICA MIN. NA HORA ANT. (AUT) (mB);RADIACAO GLOBAL (Kj/m²);TEMPERATURA DO AR - BULBO SECO, HORARIA (°C);TEMPERATURA DO PONTO DE ORVALHO (°C);TEMPERATURA MÁXIMA NA HORA ANT. (AUT) (°C);TEMPERATURA MÍNIMA NA HORA ANT. (AUT) (°C);TEMPERATURA ORVALHO MAX. NA HORA ANT. (AUT) (°C);TEMPERATURA ORVALHO MIN. NA HORA ANT. (AUT) (°C);UMIDADE REL. MAX. NA HORA ANT. (AUT) (%);UMIDADE REL. MIN. NA HORA ANT. (AUT) (%);UMIDADE RELATIVA DO AR, HORARIA (%);VENTO, DIREÇÃO HORARIA (gr) (° (gr));VENTO, RAJADA MAXIMA (m/s);VENTO, VELOCIDADE HORARIA (m/s);", "DATA (YYYY-MM-DD);HORA (UTC);PRECIPITAÇÃO TOTAL, HORÁRIO (mm);PRESSAO ATMOSFERICA AO NIVEL DA ESTACAO, HORARIA (mB);PRESSÃO ATMOSFERICA MAX.NA HORA ANT. (AUT) (mB);PRESSÃO ATMOSFERICA MIN. NA HORA ANT. (AUT) (mB);RADIACAO GLOBAL (KJ/m²);TEMPERATURA DO AR - BULBO SECO, HORARIA (°C);TEMPERATURA DO PONTO DE ORVALHO (°C);TEMPERATURA MÁXIMA NA HORA ANT. (AUT) (°C);TEMPERATURA MÍNIMA NA HORA ANT. (AUT) (°C);TEMPERATURA ORVALHO MAX. NA HORA ANT. (AUT) (°C);TEMPERATURA ORVALHO MIN. NA HORA ANT. (AUT) (°C);UMIDADE REL. MAX. NA HORA ANT. (AUT) (%);UMIDADE REL. MIN. NA HORA ANT. (AUT) (%);UMIDADE RELATIVA DO AR, HORARIA (%);VENTO, DIREÇÃO HORARIA (gr) (° (gr));VENTO, RAJADA MAXIMA (m/s);VENTO, VELOCIDADE HORARIA (m/s);");

    private static final String[] ANOS_VALIDOS = new String[]{"2020", "2021", "2022", "2023", "2024", "2025"};

    private static final int INDICE_INICIO_DIA_MES = 0;
    private static final int INDICE_FIM_DIA_MES = 6;

    private static final int NENHUM_ARQUIVO = 0;

    private static final String CODIFICADOR_DE_CARACTERES = "Windows-1252";

    public static final Set<String> COLUNA_DATA = Set.of("Data", "DATA (YYYY-MM-DD)");
    public static final Set<String> COLUNA_HORA = Set.of("Hora UTC", "HORA (UTC)");
    public static final Set<String> COLUNA_PRECIPITACAO = Set.of("PRECIPITAÇÃO TOTAL, HORÁRIO (mm)");
    public static final Set<String> COLUNA_RADIACAO_SOLAR = Set.of("RADIACAO GLOBAL (Kj/m²)", "RADIACAO GLOBAL (KJ/m²)", "RADIACAO GLOBAL");
    public static final Set<String> COLUNA_TEMPERATURA = Set.of("TEMPERATURA DO AR - BULBO SECO, HORARIA (°C)") ;

    public static List<String> obterCaminhosDeArquivosCsvDaEstacao(String codigoEstacao) {
        try {
            URL resourceUrl = LeitorArquivoUtil.class.getClassLoader().getResource("dadosEstacoesMeteorologicas");

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
                throw new RuntimeException("Não foi encontrado nenhum arquivo de registros históricos com o código de estação: " + codigoEstacao);
            }

            return caminhos;

        } catch (Exception e) {
            throw new RuntimeException("Falha: Não foi possível acessar pasta de dados históricos.");
        }
    }

    public static List<RegistroMeteorologico> lerRegistrosAnuaisDosCsv(String dataDeBusca, String horaDeBusca, EstacaoMeteorologica estacaoMeteorologica) {
        List<String> caminhosArquivos = obterCaminhosDeArquivosCsvDaEstacao(estacaoMeteorologica.getCodigoEstacao());
        List<RegistroMeteorologico> registrosHistoricos = new ArrayList<>();

        for (String caminhoArquivo : caminhosArquivos) {
            try (Scanner leitorArquivo = new Scanner(new File(caminhoArquivo), CODIFICADOR_DE_CARACTERES)) {
                boolean encontrouCabecalho = false;
                int indiceColunaData = -1, indiceColunaHora = -1, indiceColunaTemperatura = -1, indiceColunaPrecipitacao = -1, indiceColunaRadiacaoSolar = -1;

                while (leitorArquivo.hasNextLine()) {
                    String linhaAtual = leitorArquivo.nextLine().trim();

                    if (!encontrouCabecalho) {
                        if (CABECALHO_VALIDO_REGISTROS_METEOROLOGICOS.contains(linhaAtual)) {
                            String[] cabecalho = linhaAtual.split(PONTO_E_VIRGULA);
                            for (int i = 0; i < cabecalho.length; i++) {
                                if (COLUNA_DATA.contains(cabecalho[i])) {
                                    indiceColunaData = i;
                                } else if (COLUNA_HORA.contains(cabecalho[i])) {
                                    indiceColunaHora = i;
                                } else if (COLUNA_TEMPERATURA.contains(cabecalho[i])) {
                                    indiceColunaTemperatura = i;
                                } else if (COLUNA_PRECIPITACAO.contains(cabecalho[i])) {
                                    indiceColunaPrecipitacao = i;
                                } else if (COLUNA_RADIACAO_SOLAR.contains(cabecalho[i])) {
                                    indiceColunaRadiacaoSolar = i;
                                }
                            }
                            encontrouCabecalho = true;
                        }
                        continue;
                    }

                    if (linhaAtual.isEmpty() || indiceColunaData == -1 || indiceColunaHora == -1 || indiceColunaTemperatura == -1 || indiceColunaPrecipitacao == -1 || indiceColunaRadiacaoSolar == -1) {
                        continue;
                    }

                    String[] colunas = linhaAtual.split(PONTO_E_VIRGULA);

                    String colunaData = definirValorString(indiceColunaData, colunas);
                    String colunaHora = definirValorString(indiceColunaHora, colunas);

                    if (colunaData == null || colunaHora == null) {
                        continue;
                    }

                    String dataNormalizada = FormatadorUtil.formatarParaDataBrasileira(colunaData);
                    String horaNormalizada = FormatadorUtil.formatarHoraHHmm(FormatadorUtil.removerSubPalavraUTC(colunaHora));

                    String dataDeBuscaNoArquivo = obterDataDeBuscaComAnoDoArquivo(dataDeBusca, caminhoArquivo);
                    if (dataDeBuscaNoArquivo == null) {
                        continue;
                    }

                    if (dataDeBuscaNoArquivo.equals(dataNormalizada) && horaDeBusca.equals(horaNormalizada)) {
                        RegistroMeteorologico registroMeteorologico = new RegistroMeteorologico();
                        registroMeteorologico.setTemperaturaReal(definirValorDouble(indiceColunaTemperatura, colunas));
                        registroMeteorologico.setPrecipitacaoReal(definirValorDouble(indiceColunaPrecipitacao, colunas));
                        registroMeteorologico.setRadiacaoSolarReal(definirValorDouble(indiceColunaRadiacaoSolar, colunas));

                        String horaFormatada = horaDeBusca.substring(0, 2) + DOIS_PONTOS + horaDeBusca.substring(2, 4);
                        LocalDateTime dataHora = LocalDateTime.parse(dataDeBuscaNoArquivo + ESPACO_EM_BRANCO + horaFormatada, FORMATADOR_DATA_HORA_PARA_COMPARACAO_CSV);
                        registroMeteorologico.setDataHora(dataHora);

                        registrosHistoricos.add(registroMeteorologico);
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        registrosHistoricos.sort(Comparator.comparing(RegistroMeteorologico::getDataHora));

        return registrosHistoricos;
    }

    private static String definirValorString(int indiceColuna, String[] colunas) {
        if (indiceColuna == -1 || indiceColuna >= colunas.length || colunas[indiceColuna].isBlank() || colunas[indiceColuna].isEmpty()) {
            return null;
        }
        return colunas[indiceColuna];
    }

    private static Double definirValorDouble(int indiceColuna, String[] colunas) {
        if (indiceColuna == -1 || indiceColuna >= colunas.length || colunas[indiceColuna].isBlank() || colunas[indiceColuna].isEmpty()) {
            return null;
        }
        return FormatadorUtil.converterStringParaDouble(colunas[indiceColuna]);
    }

    private static String obterDataDeBuscaComAnoDoArquivo(String dataDeBusca, String caminhoArquivo) {
        for (String ano : ANOS_VALIDOS) {
            if (caminhoArquivo.contains(ano)) {
                return dataDeBusca.substring(INDICE_INICIO_DIA_MES, INDICE_FIM_DIA_MES) + ano;
            }
        }
        return null;
    }

}
