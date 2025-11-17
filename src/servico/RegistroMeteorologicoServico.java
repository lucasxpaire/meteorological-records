package servico;

import com.fasterxml.jackson.databind.JsonNode;
import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import modelo.RegistroMeteorologico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import smile.timeseries.AR;
import util.FormatadorUtil;
import util.LeitorArquivoUtil;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static util.FormatadorUtil.ESPACO_EM_BRANCO;
import static util.FormatadorUtil.UTC;

@Service
@DependsOn("estacaoMeteorologicaServico")
public class RegistroMeteorologicoServico {

    private static final String URL_REGISTROS_METEOROLOGICOS = "https://megatecnologia.com.br/silas.json?chave=A5AC5-6AA87-69587-2B9B2-A7BE1-C99F9-21&estacao=";
    private static final String URL_REGISTROS_METEOROLOGICOS2 = "http://192.168.1.2:8081/controle/silas.json?chave=A5AC5-6AA87-69587-2B9B2-A7BE1-C99F9-21&estacao=";

    private static final double PESO_NULO = 0.0;

    private static final int ANO_MAIS_ANTIGO_REGISTROS_HISTORICOS = 2020;

    private static final int MINIMO_DE_REGISTROS_PARA_PREVISAO = 2;
    private static final int CONDICAO_DE_REGISTRO_UNICO = 1;

    private static final TimeUnit UNIDADE_DE_TEMPO_DO_INTERVALO = TimeUnit.HOURS;

    public static final long QUANTIDADE_HORAS = 4L;
    private static final long MINUTOS_POR_HORA = 60L;
    private static final long SEGUNDOS_POR_MINUTO = 60L;
    private static final long MILISSEGUNDOS_POR_SEGUNDO = 1000L;

    private static final long INTERVALO_ATUALIZACAO = QUANTIDADE_HORAS * MINUTOS_POR_HORA * SEGUNDOS_POR_MINUTO * MILISSEGUNDOS_POR_SEGUNDO;

    public static final String JSON_CHAVE_TEMPERATURA = "temperatura";
    public static final String JSON_CHAVE_PRECIPITACAO = "precipitacao";
    public static final String JSON_CHAVE_DATA = "data";
    public static final String JSON_CHAVE_HORA = "hora";
    public static final String JSON_CHAVE_RADIACAO_SOLAR = "radiacao_solar";

    @Autowired
    private Dados dados;

    @Autowired
    private EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    private LocalDateTime ultimaAtualizacaoDeRegistrosMeteorologicos;

    public RegistroMeteorologicoServico() {
    }

    public RegistroMeteorologicoServico(Dados dados, EstacaoMeteorologicaServico estacaoMeteorologicaServico) {
        this.dados = dados;
        this.estacaoMeteorologicaServico = estacaoMeteorologicaServico;
    }

    public void salvar(RegistroMeteorologico registroMeteorologico) {
        if (registroMeteorologico == null) {
            throw new IllegalArgumentException("Falha: o registro meteorológico não pode ser nulo.");
        } else if (registroMeteorologico.getPonto() == null) {
            throw new IllegalArgumentException("Falha: o ponto associado ao registro meteorológico não pode ser nulo.");
        } else if (registroMeteorologico.getDataHora() == null) {
            throw new IllegalArgumentException("Falha: a data e hora do registro meteorológico não podem ser nulas.");
        }

        dados.salvar(registroMeteorologico);
    }

    @PostConstruct
    private void inicializarRegistrosMeteorologicos() {
        dados.iniciarTransacao();
        try {
            popularHistoricoDeRegistrosMeteorologicos();
            executarAtualizacaoDeRegistrosMeteorologicos();
            dados.confirmarTransacao();
        } catch (Exception e) {
            dados.desfazerTransacao();
        }
    }

    private void popularHistoricoDeRegistrosMeteorologicos() {
        if (dados.existeAlgumVazio(EstacaoMeteorologica.class, "localizacao.historicoRegistrosMeteorologicos")) {
            atualizarRegistrosDeEstacoes(dados.buscarOndeCampoForVazio(EstacaoMeteorologica.class, "localizacao.historicoRegistrosMeteorologicos"));
        }
    }

    @Scheduled(fixedRate = INTERVALO_ATUALIZACAO)
    private void executarAtualizacaoDeRegistrosMeteorologicos() {
        dados.iniciarTransacao();
        try {
            ultimaAtualizacaoDeRegistrosMeteorologicos = LocalDateTime.now();
            atualizarRegistrosDeEstacoesAssociadasACentroides();
            atualizarRegistrosDeCentroidesAssociadosAEstacoes();
            atualizarValoresPrevistosComReaisESuasDiferencas();
            dados.confirmarTransacao();
        } catch (Exception e) {
            dados.desfazerTransacao();
        }
    }

    public void atualizarRegistrosDeEstacoesAssociadasACentroides() {
        List<EstacaoMeteorologica> estacoesAssociadas = dados.buscarEstacoesAssociadasAPropriedades();
        if (!estacoesAssociadas.isEmpty()) {
            atualizarRegistrosDeEstacoes(estacoesAssociadas);
        }
    }

    public void atualizarRegistrosDeCentroidesAssociadosAEstacoes() {
        List<Ponto> pontosAssociados = dados.buscarCentroidesAssociadosAEstacoes();
        if (!pontosAssociados.isEmpty()) {
            atualizarRegistrosMeteorologicosDeCentroides(pontosAssociados);
        }
    }

    private void atualizarValoresPrevistosComReaisESuasDiferencas() {
        List<RegistroMeteorologico> registrosPrevistos = dados.buscarRegistrosPrevistosSemValoresReais();
        for (RegistroMeteorologico registroPrevisto : registrosPrevistos) {
            if (registroPrevisto.getDataHora().isAfter(LocalDateTime.now())) {
                continue;
            }
            preencherValoresDeRegistrosPrevistosComReais(registroPrevisto);
            calcularDiferencaDeValoresPrevistosParaReais(registroPrevisto);
            dados.salvar(registroPrevisto);
        }
    }

    private void preencherValoresDeRegistrosPrevistosComReais(RegistroMeteorologico registroPrevisto) {
        double temperaturaCalculada;
        double radiacaoSolarCalculada;
        double precipitacaoCalculada;

        double somaTemperaturas = 0.0;
        double somaRadiacaoSolar = 0.0;
        double somaPrecipitacao = 0.0;
        double somaPesos = 0.0;

        for (EstacaoMeteorologica estacaoMeteorologica : registroPrevisto.getPonto().getEstacoesMeteorologicas()) {
            RegistroMeteorologico registroRealNoHorarioDoRegistroPrevisto = estacaoMeteorologica.getLocalizacao().getHistoricoRegistrosMeteorologicos().stream()
                    .filter(registroReal -> registroReal.getDataHora().equals(registroPrevisto.getDataHora()))
                    .findFirst()
                    .orElse(null);

            if (registroRealNoHorarioDoRegistroPrevisto == null) {
                continue;
            }

            if (registroRealNoHorarioDoRegistroPrevisto.getTemperaturaReal() != null) {
                somaTemperaturas += registroRealNoHorarioDoRegistroPrevisto.getTemperaturaReal();
            }
            if (registroRealNoHorarioDoRegistroPrevisto.getPrecipitacaoReal() != null) {
                somaPrecipitacao += registroRealNoHorarioDoRegistroPrevisto.getPrecipitacaoReal();
            }
            if (registroRealNoHorarioDoRegistroPrevisto.getRadiacaoSolarReal() != null) {
                somaRadiacaoSolar += registroRealNoHorarioDoRegistroPrevisto.getRadiacaoSolarReal();
            }

            somaPesos += registroPrevisto.getPonto().calcularPesoDeProximidadePara(estacaoMeteorologica);
        }

        if (somaPesos != PESO_NULO) {
            temperaturaCalculada = somaTemperaturas / somaPesos;
            precipitacaoCalculada = somaPrecipitacao / somaPesos;
            radiacaoSolarCalculada = somaRadiacaoSolar / somaPesos;

            registroPrevisto.setTemperaturaCalculada(temperaturaCalculada);
            registroPrevisto.setPrecipitacaoCalculada(precipitacaoCalculada);
            registroPrevisto.setRadiacaoSolarCalculada(radiacaoSolarCalculada);
        }
    }

    private void calcularDiferencaDeValoresPrevistosParaReais(RegistroMeteorologico registroPrevisto) {
        if (registroPrevisto.getTemperaturaCalculada() != null && registroPrevisto.getTemperaturaPrevista() != null) {
            registroPrevisto.setDiferencaTemperatura(registroPrevisto.getTemperaturaCalculada() - registroPrevisto.getTemperaturaPrevista());
        }
        if (registroPrevisto.getPrecipitacaoCalculada() != null && registroPrevisto.getPrecipitacaoPrevista() != null) {
            registroPrevisto.setDiferencaPrecipitacao(registroPrevisto.getPrecipitacaoCalculada() - registroPrevisto.getPrecipitacaoPrevista());
        }
        if (registroPrevisto.getRadiacaoSolarCalculada() != null && registroPrevisto.getRadiacaoSolarPrevista() != null) {
            registroPrevisto.setDiferencaRadiacaoSolar(registroPrevisto.getRadiacaoSolarCalculada() - registroPrevisto.getRadiacaoSolarPrevista());
        }
    }

    private void atualizarRegistrosDeEstacoes(List<EstacaoMeteorologica> estacoes) {
        if (estacoes.isEmpty()) {
            return;
        }

        for (EstacaoMeteorologica estacao : estacoes) {
            try {
                JsonNode dadosJson = estacaoMeteorologicaServico.obterDadosDaEstacao(URL_REGISTROS_METEOROLOGICOS + estacao.getCodigoEstacao());

                if (dadosJson.isNull()) {
                    continue;
                }

                Set<LocalDateTime> datasHorasExistentes = estacao.obterDatasHorasExistentesNoHistorico();

                for (JsonNode objeto : dadosJson) {
                    if (objeto.get(JSON_CHAVE_DATA) == null || objeto.get(JSON_CHAVE_HORA) == null || objeto.get(JSON_CHAVE_TEMPERATURA) == null || objeto.get(JSON_CHAVE_PRECIPITACAO) == null || objeto.get(JSON_CHAVE_RADIACAO_SOLAR) == null) {
                        continue;
                    }

                    String data = objeto.get(JSON_CHAVE_DATA).asText();
                    String hora = String.format("%02d", objeto.get(JSON_CHAVE_HORA).asInt());
                    LocalDateTime dataHoraUTC = LocalDateTime.parse(data + ESPACO_EM_BRANCO + hora, FormatadorUtil.FORMATADOR_DATA_HORA_PARA_COMPARACAO_JSON);
                    ZonedDateTime dataHoraGMT = dataHoraUTC.atZone(ZoneId.of(UTC)).withZoneSameInstant(ZoneId.of(estacao.getLocalizacao().getFusoHorario()));

                    boolean temRegistroPrevisto = estacao.getLocalizacao().getHistoricoRegistrosMeteorologicos().stream()
                            .anyMatch(r -> r.getDataHora().equals(dataHoraGMT.toLocalDateTime()) && r.getTemperaturaPrevista() != null && r.getPrecipitacaoPrevista() != null && r.getRadiacaoSolarPrevista() != null);
                    if (temRegistroPrevisto) {
                        RegistroMeteorologico registroPrevisto = estacao.getLocalizacao().getHistoricoRegistrosMeteorologicos().stream()
                                .filter(r -> r.getDataHora().equals(dataHoraGMT.toLocalDateTime()) && r.getTemperaturaPrevista() != null && r.getPrecipitacaoPrevista() != null && r.getRadiacaoSolarPrevista() != null)
                                .findFirst()
                                .orElse(null);

                        if (registroPrevisto == null) {
                            continue;
                        }

                        registroPrevisto.setTemperaturaReal(objeto.get(JSON_CHAVE_TEMPERATURA).asDouble());
                        registroPrevisto.setPrecipitacaoReal(objeto.get(JSON_CHAVE_PRECIPITACAO).asDouble());
                        registroPrevisto.setRadiacaoSolarReal(objeto.get(JSON_CHAVE_RADIACAO_SOLAR).asDouble());
                        registroPrevisto.setDiferencaTemperatura(registroPrevisto.getTemperaturaReal() - registroPrevisto.getTemperaturaPrevista());
                        registroPrevisto.setDiferencaPrecipitacao(registroPrevisto.getPrecipitacaoReal() - registroPrevisto.getPrecipitacaoPrevista());
                        registroPrevisto.setDiferencaRadiacaoSolar(registroPrevisto.getRadiacaoSolarReal() - registroPrevisto.getRadiacaoSolarPrevista());
                    }

                    if (!datasHorasExistentes.contains(dataHoraGMT.toLocalDateTime())) {
                        RegistroMeteorologico registroMeteorologico = new RegistroMeteorologico();
                        registroMeteorologico.setTemperaturaReal(objeto.get(JSON_CHAVE_TEMPERATURA).asDouble());
                        registroMeteorologico.setPrecipitacaoReal(objeto.get(JSON_CHAVE_PRECIPITACAO).asDouble());
                        registroMeteorologico.setRadiacaoSolarReal(objeto.get(JSON_CHAVE_RADIACAO_SOLAR).asDouble());

                        registroMeteorologico.setDataHora(dataHoraGMT.toLocalDateTime());
                        registroMeteorologico.setPonto(estacao.getLocalizacao());
                        estacao.getLocalizacao().getHistoricoRegistrosMeteorologicos().add(registroMeteorologico);

                        datasHorasExistentes.add(dataHoraGMT.toLocalDateTime());
                    }
                }

                dados.salvar(estacao);
            } catch (IOException e) {
                throw new RuntimeException("Falha: Não foi possível obter histórico de registros para a estação: " + estacao.getCodigoEstacao());
            }
        }
    }

    private void atualizarRegistrosMeteorologicosDeCentroides(List<Ponto> pontos) {
        for (Ponto ponto : pontos) {
            if (!ponto.getEstacoesMeteorologicas().isEmpty()) {
                RegistroMeteorologico registroCalculado = calcularRegistroMeteorologico(ponto);
                if (registroCalculado != null) {
                    boolean ehNovoRegistro = ponto.getHistoricoRegistrosMeteorologicos().stream()
                            .noneMatch(t -> t.getDataHora().equals(registroCalculado.getDataHora()));

                    if (ehNovoRegistro) {
                        ponto.getHistoricoRegistrosMeteorologicos().add(registroCalculado);
                        dados.salvar(registroCalculado);
                    }
                }
            }
        }
    }

    public RegistroMeteorologico calcularRegistroMeteorologico(Ponto ponto) {
        Optional<LocalDateTime> dataHoraDoRegistroMaisRecenteEntreEstacoes = ponto.getEstacoesMeteorologicas().stream()
                .map(e -> e.getLocalizacao().getHistoricoRegistrosMeteorologicos().stream()
                        .filter(t -> t.getTemperaturaReal() != null && t.getRadiacaoSolarReal() != null && t.getPrecipitacaoReal() != null)
                        .map(RegistroMeteorologico::getDataHora)
                        .max(Comparator.naturalOrder())
                        .orElse(null))
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());

        if (dataHoraDoRegistroMaisRecenteEntreEstacoes.isEmpty()) {
            return null;
        }

        double somaTemperaturas = 0.0;
        double somaPrecipitacao = 0.0;
        double somaRadiacaoSolar = 0.0;
        double somaPesos = 0.0;

        for (EstacaoMeteorologica estacao : ponto.getEstacoesMeteorologicas()) {
            Optional<RegistroMeteorologico> registroMaisRecenteDaEstacao = estacao.getLocalizacao().getHistoricoRegistrosMeteorologicos().stream()
                    .filter(t -> t.getDataHora().equals(dataHoraDoRegistroMaisRecenteEntreEstacoes.get()) && t.getTemperaturaReal() != null)
                    .findFirst();

            Double temperatura = 0.0;
            Double precipitacao = 0.0;
            Double radiacaoSolar = 0.0;
            Double peso = 0.0;
            if (registroMaisRecenteDaEstacao.isEmpty()) {
                Map<EstacaoMeteorologica, RegistroMeteorologico> registrosPrevistosDasEstacoes = preverRegistroMeteorologicoParaEstacoes(List.of(estacao), dataHoraDoRegistroMaisRecenteEntreEstacoes.get());
                temperatura = registrosPrevistosDasEstacoes.get(estacao).getTemperaturaPrevista();
                precipitacao = registrosPrevistosDasEstacoes.get(estacao).getPrecipitacaoPrevista();
                radiacaoSolar = registrosPrevistosDasEstacoes.get(estacao).getRadiacaoSolarPrevista();
                peso = ponto.calcularPesoDeProximidadePara(estacao);

                estacao.getLocalizacao().getHistoricoRegistrosMeteorologicos().add(registrosPrevistosDasEstacoes.get(estacao));
                dados.salvar(estacao);
            }

            if (registroMaisRecenteDaEstacao.isPresent()) {
                temperatura = registroMaisRecenteDaEstacao.get().getTemperaturaReal();
                precipitacao = registroMaisRecenteDaEstacao.get().getPrecipitacaoReal();
                radiacaoSolar = registroMaisRecenteDaEstacao.get().getRadiacaoSolarReal();
                peso = ponto.calcularPesoDeProximidadePara(estacao);
            }

            if (temperatura != null) {
                somaTemperaturas += temperatura * peso;
            }
            if (precipitacao != null) {
                somaPrecipitacao += precipitacao * peso;
            }
            if (radiacaoSolar != null) {
                somaRadiacaoSolar += radiacaoSolar * peso;
            }

            somaPesos += peso;
        }

        if (somaPesos == PESO_NULO) {
            return null;
        }

        double temperatura = somaTemperaturas / somaPesos;
        double precipitacao = somaPrecipitacao / somaPesos;
        double radiacaoSolar = somaRadiacaoSolar / somaPesos;

        RegistroMeteorologico registroMeteorologicoCalculado = new RegistroMeteorologico();
        registroMeteorologicoCalculado.setPonto(ponto);
        registroMeteorologicoCalculado.setTemperaturaCalculada(temperatura);
        registroMeteorologicoCalculado.setPrecipitacaoCalculada(precipitacao);
        registroMeteorologicoCalculado.setRadiacaoSolarCalculada(radiacaoSolar);
        registroMeteorologicoCalculado.setDataHora(dataHoraDoRegistroMaisRecenteEntreEstacoes.get());
        return registroMeteorologicoCalculado;
    }

    public RegistroMeteorologico preverRegistroMeteorologico(Ponto ponto, LocalDateTime dataHoraPrevisao) {
        Map<EstacaoMeteorologica, RegistroMeteorologico> registrosPrevistosDasEstacoes = preverRegistroMeteorologicoParaEstacoes(ponto.getEstacoesMeteorologicas(), dataHoraPrevisao);

        double somaTemperaturas = 0.0;
        double somaPrecipitacao = 0.0;
        double somaRadiacaoSolar = 0.0;
        double somaPesos = 0.0;

        for (Map.Entry<EstacaoMeteorologica, RegistroMeteorologico> entry : registrosPrevistosDasEstacoes.entrySet()) {
            EstacaoMeteorologica estacaoMeteorologica = entry.getKey();
            RegistroMeteorologico registroMeteorologico = entry.getValue();

            if (registroMeteorologico == null) {
                continue;
            }

            double peso = ponto.calcularPesoDeProximidadePara(estacaoMeteorologica);
            if (registroMeteorologico.getTemperaturaPrevista() != null) {
                somaTemperaturas += registroMeteorologico.getTemperaturaPrevista() * peso;
            }
            if (registroMeteorologico.getPrecipitacaoPrevista() != null) {
                somaPrecipitacao += registroMeteorologico.getPrecipitacaoPrevista() * peso;
            }
            if (registroMeteorologico.getRadiacaoSolarPrevista() != null) {
                somaRadiacaoSolar += registroMeteorologico.getRadiacaoSolarPrevista() * peso;
            }
            somaPesos += peso;
        }

        if (somaPesos == PESO_NULO) {
            return null;
        }

        double temperatura = somaTemperaturas / somaPesos;
        double precipitacao = somaPrecipitacao / somaPesos;
        double radiacaoSolar = somaRadiacaoSolar / somaPesos;

        RegistroMeteorologico registroMeteorologicoPrevisto = new RegistroMeteorologico();
        registroMeteorologicoPrevisto.setDataHora(dataHoraPrevisao);
        registroMeteorologicoPrevisto.setTemperaturaPrevista(temperatura);
        registroMeteorologicoPrevisto.setPrecipitacaoPrevista(precipitacao);
        registroMeteorologicoPrevisto.setRadiacaoSolarPrevista(radiacaoSolar);
        registroMeteorologicoPrevisto.setPonto(ponto);

        return registroMeteorologicoPrevisto;
    }

    private Map<EstacaoMeteorologica, RegistroMeteorologico> preverRegistroMeteorologicoParaEstacoes(List<EstacaoMeteorologica> estacaoMeteorologicas, LocalDateTime dataHoraPrevisao) {
        Map<EstacaoMeteorologica, List<RegistroMeteorologico>> registrosMeteorologicosPorEstacao = new HashMap<>();

        for (EstacaoMeteorologica estacao : estacaoMeteorologicas) {
            List<LocalDateTime> datasHorasDecrescentes = obterDatasHorasDescrescentes(dataHoraPrevisao);
            List<RegistroMeteorologico> registrosCombinados = combinarRegistrosDoBancoDeDadosComDadosHistoricos(estacao, datasHorasDecrescentes);

            if (!registrosCombinados.isEmpty()) {
                registrosMeteorologicosPorEstacao.put(estacao, registrosCombinados);
            }
        }

        Map<EstacaoMeteorologica, RegistroMeteorologico> previsoesDeCadaEstacao = new HashMap<>();

        for (Map.Entry<EstacaoMeteorologica, List<RegistroMeteorologico>> registrosDaEstacao : registrosMeteorologicosPorEstacao.entrySet()) {
            List<RegistroMeteorologico> registros = registrosDaEstacao.getValue();
            if (registros == null || registros.isEmpty()) {
                continue;
            }

            try {
                Double temperaturaPrevista = preverValor(registros, RegistroMeteorologico::getTemperaturaReal);
                Double precipitacaoPrevista = preverValor(registros, RegistroMeteorologico::getPrecipitacaoReal);
                Double radiacaoSolarPrevista = preverValor(registros, RegistroMeteorologico::getRadiacaoSolarReal);

                RegistroMeteorologico registroPrevisto = new RegistroMeteorologico();
                registroPrevisto.setTemperaturaPrevista(temperaturaPrevista);
                registroPrevisto.setPrecipitacaoPrevista(precipitacaoPrevista);
                registroPrevisto.setRadiacaoSolarPrevista(radiacaoSolarPrevista);
                registroPrevisto.setDataHora(dataHoraPrevisao);
                registroPrevisto.setPonto(registrosDaEstacao.getKey().getLocalizacao());
                previsoesDeCadaEstacao.put(registrosDaEstacao.getKey(), registroPrevisto);

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Falha ao prever valores para a estação: " + registrosDaEstacao.getKey().getCodigoEstacao() + ". " + e.getMessage());
            }
        }

        return previsoesDeCadaEstacao;
    }

    private Double preverValor(List<RegistroMeteorologico> registros, Function<RegistroMeteorologico, Double> getter) {
        double[] serieTemporal = registros.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .toArray();

        if (serieTemporal.length == CONDICAO_DE_REGISTRO_UNICO) {
            return serieTemporal[0];
        } else if (serieTemporal.length >= MINIMO_DE_REGISTROS_PARA_PREVISAO) {
            long valoresDistintos = Arrays.stream(serieTemporal).distinct().count();
            if (valoresDistintos <= 1) {
                return serieTemporal[0];
            }
            AR modelo = AR.fit(serieTemporal, serieTemporal.length - 1);
            return modelo.forecast();
        } else {
            return null;
        }
    }

    private List<LocalDateTime> obterDatasHorasDescrescentes(LocalDateTime dataHoraPrevisao) {
        List<LocalDateTime> datasHorasDecrescentes = new ArrayList<>();
        int anoDaDataHora = dataHoraPrevisao.getYear();
        for (int ano = anoDaDataHora - 1; ano >= ANO_MAIS_ANTIGO_REGISTROS_HISTORICOS; ano--) {
            datasHorasDecrescentes.add(dataHoraPrevisao.withYear(ano));
        }
        return datasHorasDecrescentes;
    }

    private List<RegistroMeteorologico> combinarRegistrosDoBancoDeDadosComDadosHistoricos(EstacaoMeteorologica estacaoMeteorologica, List<LocalDateTime> datasHorasDecrescentes) {
        if (datasHorasDecrescentes == null || datasHorasDecrescentes.isEmpty()) {
            return new ArrayList<>();
        }

        List<RegistroMeteorologico> registrosExistentesNoBancoDeDados = dados.buscarRegistrosDaEstacaoMeteorologica(estacaoMeteorologica.getLocalizacao(), datasHorasDecrescentes);
        Set<LocalDateTime> datasHorasObtidasDoBanco = registrosExistentesNoBancoDeDados.stream()
                .map(RegistroMeteorologico::getDataHora)
                .collect(Collectors.toSet());

        LocalDateTime dataHoraDoAnoAnteriorDaPrevisao = datasHorasDecrescentes.getFirst();
        String dataFormatada = FormatadorUtil.formatarDataParaComparacao(dataHoraDoAnoAnteriorDaPrevisao);
        String horaFormatada = FormatadorUtil.formatarHoraParaComparacao(dataHoraDoAnoAnteriorDaPrevisao);

        List<RegistroMeteorologico> registrosDosDadosHistoricos = LeitorArquivoUtil.lerRegistrosAnuaisDosCsv(dataFormatada, horaFormatada, estacaoMeteorologica);
        List<RegistroMeteorologico> registrosAusentesDoBancoDeDados = registrosDosDadosHistoricos.stream()
                .filter(t -> !datasHorasObtidasDoBanco.contains(t.getDataHora()))
                .toList();

        return Stream.concat(registrosExistentesNoBancoDeDados.stream(), registrosAusentesDoBancoDeDados.stream())
                .sorted(Comparator.comparing(RegistroMeteorologico::getDataHora).reversed())
                .collect(Collectors.toList());
    }

    public LocalDateTime obterUltimaAtualizacaoDeRegistrosMeteorologicos() {
        return ultimaAtualizacaoDeRegistrosMeteorologicos;
    }

    public LocalDateTime obterProximaAtualizacaoDeRegistrosMeteorologicos() {
        if (ultimaAtualizacaoDeRegistrosMeteorologicos != null) {
            return ultimaAtualizacaoDeRegistrosMeteorologicos.plus(QUANTIDADE_HORAS, UNIDADE_DE_TEMPO_DO_INTERVALO.toChronoUnit());
        }
        return null;
    }

}

