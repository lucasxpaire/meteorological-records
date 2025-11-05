package util;

import modelo.EstacaoMeteorologica;
import modelo.RegistroMeteorologico;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public static final Set<String> COLUNA_RADIACAO = Set.of("RADIACAO GLOBAL (Kj/m²)");
    public static final Set<String> COLUNA_TEMPERATURA = Set.of("TEMPERATURA DO AR - BULBO SECO, HORARIA (°C)") ;

    public static List<String> obterCaminhosDeArquivosCsvDaEstacao(String codigoEstacao) {
        try {
            URL resourceUrl = LeitorArquivoUtil.class.getClassLoader().getResource("dadosHistoricos");

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

    public static List<RegistroMeteorologico> lerTemperaturasHistoricasCsv(String dataDeBusca, String horaDeBusca, EstacaoMeteorologica estacaoMeteorologica) {
        List<String> caminhosArquivos = obterCaminhosDeArquivosCsvDaEstacao(estacaoMeteorologica.getCodigoEstacao());
        List<RegistroMeteorologico> temperaturasHistoricas = new ArrayList<>();

        for (String caminhoArquivo : caminhosArquivos) {
            String dataEsperadaDoArquivoAtual = selecionarDataEsperadaDoArquivoAtual(dataDeBusca, caminhoArquivo);

            try (Scanner leitorArquivo = new Scanner(new File(caminhoArquivo), CODIFICADOR_DE_CARACTERES)) {
                Map<String, Integer> mapaColunasRelevantes = new HashMap<>();

                boolean encontrouCabecalho = false;

                while (leitorArquivo.hasNextLine()) {
                    String linhaAtual = leitorArquivo.nextLine().trim();

                    if (!encontrouCabecalho) {
                        if (CABECALHO_VALIDO_REGISTROS_METEOROLOGICOS.contains(linhaAtual)) {
                            String[] cabecalho = linhaAtual.split(PONTO_E_VIRGULA);
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

                    String[] colunas = linhaAtual.split(PONTO_E_VIRGULA);

                    Integer indiceColunaData = mapaColunasRelevantes.get("Data");
                    Integer indiceColunaHora = mapaColunasRelevantes.get("Hora UTC");
                    Integer indiceColunaTemperatura = mapaColunasRelevantes.get("TEMPERATURA DO AR - BULBO SECO, HORARIA (°C)");

                    if (indiceColunaData == null || indiceColunaHora == null || indiceColunaTemperatura == null) {
                        continue;
                    }

                    String colunaData;
                    if (indiceColunaData < colunas.length && !colunas[indiceColunaData].isBlank()) {
                        colunaData = formatarParaDataBrasileira(colunas[indiceColunaData]);
                    } else {
                        colunaData = dataEsperadaDoArquivoAtual;
                    }

                    String colunaHora;
                    if (indiceColunaHora < colunas.length && !colunas[indiceColunaHora].isBlank()) {
                        colunaHora = removerSubPalavraUTC(colunas[indiceColunaHora]);
                    } else {
                        colunaHora = horaDeBusca;
                    }

                    String colunaTemperatura;
                    if (indiceColunaTemperatura < colunas.length && !colunas[indiceColunaTemperatura].isBlank()) {
                        colunaTemperatura = colunas[indiceColunaTemperatura];
                    } else {
                        colunaTemperatura = null;
                    }

                    if (dataEsperadaDoArquivoAtual == null) {
                        continue;
                    }

                    if (dataEsperadaDoArquivoAtual.contains(colunaData) && horaDeBusca.contains(colunaHora)) {
                        RegistroMeteorologico registroMeteorologico = new RegistroMeteorologico();
                        if (colunaTemperatura == null) {
                            registroMeteorologico.setTemperaturaReal(null);
                        } else {
                            registroMeteorologico.setTemperaturaReal(converterStringParaDouble(colunaTemperatura));
                        }

                        LocalDateTime dataHora = LocalDateTime.parse(dataEsperadaDoArquivoAtual + ESPACO_EM_BRANCO +  horaDeBusca.substring(0, 2) + DOIS_PONTOS + horaDeBusca.substring(2, 4), FORMATADOR_DATA_HORA_PARA_COMPARACAO_CSV);
                        registroMeteorologico.setDataHora(dataHora);

                        temperaturasHistoricas.add(registroMeteorologico);
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        temperaturasHistoricas.sort(Comparator.comparing(RegistroMeteorologico::getDataHora));

        return temperaturasHistoricas.stream()
                .filter(t -> t.getTemperaturaReal() != null)
                .collect(Collectors.toList());
    }

    private static String selecionarDataEsperadaDoArquivoAtual(String dataDeBusca, String caminhoArquivo) {
        for (String ano : ANOS_VALIDOS) {
            if (caminhoArquivo.contains(ano)) {
                return dataDeBusca.substring(INDICE_INICIO_DIA_MES, INDICE_FIM_DIA_MES) + ano;
            }
        }
        return null;
    }

    public static String obterCaminhoDoArquivoDaEstacao(String nomeSubPasta, String codigoEstacao) throws URISyntaxException {
        URL resourceUrl = LeitorArquivoUtil.class.getClassLoader().getResource("dadosEstacoesMeteorologicas");

        if (resourceUrl == null) {
            throw new IllegalArgumentException("Pasta de dados históricos não encontrada no classpath: dadosEstacoesMeteorologicas/");
        }

        File pastaRaiz = new File(resourceUrl.toURI());

        if (!pastaRaiz.exists()) {
            throw new IllegalArgumentException("Pasta de dados Estacoes Meteorologicas não encontrada: " + pastaRaiz.getAbsolutePath());
        }

        File[] subpastas = pastaRaiz.listFiles(File::isDirectory);

        if (subpastas == null) {
            return STRING_VAZIA;
        }

        for (File subspasta : subpastas) {
            if (subspasta.getName().equals(nomeSubPasta)) {
                File[] arquivos = subspasta.listFiles((dir, name) -> name.contains(codigoEstacao));
                if (arquivos != null && arquivos.length > NENHUM_ARQUIVO) {
                    return arquivos[0].getPath();
                } else {
                    return STRING_VAZIA;
                }
            }
        }

        return STRING_VAZIA;
    }

    public static List<Double> obterDadosEmColunaEspecifica(String caminhoArquivo, String colunaAlvo) throws FileNotFoundException {
        List<Double> dados = new ArrayList<>();
        File arquivo = new File(caminhoArquivo);

        try (Scanner leitorArquivo = new Scanner(arquivo, CODIFICADOR_DE_CARACTERES)) {
            boolean cabecalhoEncontrado = false;
            int indiceColuna = -1;

            while (leitorArquivo.hasNextLine()) {
                String linha = leitorArquivo.nextLine().trim();

                if (!cabecalhoEncontrado) {
                    if (linha.contains(colunaAlvo)) {
                        String[] cabecalho = linha.split(";");
                        for (int i = 0; i < cabecalho.length; i++) {
                            if (cabecalho[i].trim().equals(colunaAlvo)) {
                                indiceColuna = i;
                                cabecalhoEncontrado = true;
                                break;
                            }
                        }
                    }
                    continue;
                }

                if (linha.isEmpty()) {
                    continue;
                }

                String[] colunas = linha.split(";");
                if (indiceColuna >= colunas.length) {
                    dados.add(Double.NaN);
                } else {
                    try {
                        double dado = converterStringParaDouble(colunas[indiceColuna]);
                        dados.add(dado);
                    } catch (NumberFormatException e) {
                        dados.add(Double.NaN);
                    }
                }
            }
            return dados;
        }
    }

    public static List<RegistroMeteorologico> lerRegistrosMeteorologicos(String codigoEstacao, LocalDateTime dataHoraInicioDaLeitura, LocalDateTime dataHoraFimDaLeitura) {
        List<RegistroMeteorologico> registrosMeteorologicos = new ArrayList<>();
        try {
            File arquivo = new File(obterCaminhoDoArquivoDaEstacao(String.valueOf(dataHoraFimDaLeitura.getYear()), codigoEstacao));

            try (Scanner leitorArquivo = new Scanner(arquivo)) {
                boolean cabecalhoEncontrado = false;

                int indiceColunaData = -1;
                int indiceColunaHora = -1;
                int indiceColunaTemperatura = -1;
                int indiceColunaPrecipitacao = -1;
                int indiceColunaRadiacaoSolar = -1;

                while (leitorArquivo.hasNextLine()) {
                    String linha = leitorArquivo.nextLine();

                    if (!cabecalhoEncontrado) {
                        if (CABECALHO_VALIDO_REGISTROS_METEOROLOGICOS.contains(linha)) {
                            String[] cabecalho = linha.split(";");

                            for (int i = 0; i < cabecalho.length; i++) {
                                if (COLUNA_DATA.contains(cabecalho[i])) {
                                    indiceColunaData = i;
                                } else if (COLUNA_HORA.contains(cabecalho[i])) {
                                    indiceColunaHora = i;
                                } else if (COLUNA_PRECIPITACAO.contains(cabecalho[i])) {
                                    indiceColunaPrecipitacao = i;
                                } else if (COLUNA_RADIACAO.contains(cabecalho[i])) {
                                    indiceColunaRadiacaoSolar = i;
                                } else if (COLUNA_TEMPERATURA.contains(cabecalho[i])) {
                                    indiceColunaTemperatura = i;
                                }
                            }
                            cabecalhoEncontrado = true;
                            continue;
                        }
                    }

                    if (linha.isEmpty()) {
                        continue;
                    }

                    String[] registroMeteorologico = linha.split(";");
                    RegistroMeteorologico temperatura = new RegistroMeteorologico();

                    temperatura.setDataHora(formatarECombinarDataHora(registroMeteorologico[indiceColunaData], registroMeteorologico[indiceColunaHora]));
                    temperatura.setPrecipitacaoReal(definirValor(indiceColunaPrecipitacao, registroMeteorologico));
                    temperatura.setRadiacaoSolarReal(definirValor(indiceColunaRadiacaoSolar, registroMeteorologico));
                    temperatura.setTemperaturaReal(definirValor(indiceColunaTemperatura, registroMeteorologico));

                    registrosMeteorologicos.add(temperatura);
                }

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return registrosMeteorologicos;
    }

    public static List<RegistroMeteorologico> lerRegistrosMeteorologicos(String caminhoArquivo) {
        List<RegistroMeteorologico> registrosMeteorologicos = new ArrayList<>();
        File arquivo = new File(caminhoArquivo);

        try (Scanner leitorArquivo = new Scanner(arquivo, CODIFICADOR_DE_CARACTERES)) {
            boolean cabecalhoEncontrado = false;

            int indiceColunaData = -1;
            int indiceColunaHora = -1;
            int indiceColunaTemperatura = -1;
            int indiceColunaPrecipitacao = -1;
            int indiceColunaRadiacaoSolar = -1;

            while (leitorArquivo.hasNextLine()) {
                String linha = leitorArquivo.nextLine().trim();

                if (!cabecalhoEncontrado) {
                    if (CABECALHO_VALIDO_REGISTROS_METEOROLOGICOS.contains(linha)) {
                        String[] cabecalho = linha.split(";");

                        for (int i = 0; i < cabecalho.length; i++) {
                            if (COLUNA_DATA.contains(cabecalho[i])) {
                                indiceColunaData = i;
                            } else if (COLUNA_HORA.contains(cabecalho[i])) {
                                indiceColunaHora = i;
                            } else if (COLUNA_PRECIPITACAO.contains(cabecalho[i])) {
                                indiceColunaPrecipitacao = i;
                            } else if (COLUNA_RADIACAO.contains(cabecalho[i])) {
                                indiceColunaRadiacaoSolar = i;
                            } else if (COLUNA_TEMPERATURA.contains(cabecalho[i])) {
                                indiceColunaTemperatura = i;
                            }
                        }

                        cabecalhoEncontrado = true;
                    }
                    continue;
                }

                if (linha.isEmpty()) {
                    continue;
                }

                String[] registroMeteorologico = linha.split(";");
                RegistroMeteorologico temperatura = new RegistroMeteorologico();

                temperatura.setDataHora(formatarECombinarDataHora(registroMeteorologico[indiceColunaData], registroMeteorologico[indiceColunaHora]));
                temperatura.setPrecipitacaoReal(definirValor(indiceColunaPrecipitacao, registroMeteorologico));
                temperatura.setRadiacaoSolarReal(definirValor(indiceColunaRadiacaoSolar, registroMeteorologico));
                temperatura.setTemperaturaReal(definirValor(indiceColunaTemperatura, registroMeteorologico));

                registrosMeteorologicos.add(temperatura);
            }

            return registrosMeteorologicos;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Double definirValor(int indiceColuna, String[] registro) {
        if (indiceColuna == -1 || indiceColuna >= registro.length || registro[indiceColuna].isBlank()) {
            return null;
        } else {
            return converterStringParaDouble(registro[indiceColuna]);
        }
    }

}
