package servico;

import com.fasterxml.jackson.databind.JsonNode;
import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import modelo.RegistroMeteorologico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import smile.timeseries.AR;
import util.FormatadorUtil;
import util.LeitorArquivoUtil;
import util.PrevisaoUtil;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static util.FormatadorUtil.ESPACO_EM_BRANCO;

@Service
public class RegistroMeteorologicoServico {

    private static final String URL_TEMPERATURAS = "https://megatecnologia.com.br/silas.json?chave=A5AC5-6AA87-69587-2B9B2-A7BE1-C99F9-21&estacao=";
    private static final String URL_TEMPERATURAS2 = "http://192.168.1.2:8081/controle/silas.json?chave=A5AC5-6AA87-69587-2B9B2-A7BE1-C99F9-21&estacao=";

    private static final double PESO_NULO = 0.0;

    private static final int ANO_MAIS_ANTIGO_TEMPERATURAS_HISTORICAS = 2020;

    private static final int MINIMO_DE_TEMPERATURAS_PARA_PREVISAO = 2;
    private static final int CONDICAO_DE_TEMPERATURA_UNICA = 1;

    private static final TimeUnit UNIDADE_DE_TEMPO_DO_INTERVALO = TimeUnit.HOURS;

    public static final long QUANTIDADE_HORAS = 4L;
    private static final long MINUTOS_POR_HORA = 60L;
    private static final long SEGUNDOS_POR_MINUTO = 60L;
    private static final long MILISSEGUNDOS_POR_SEGUNDO = 1000L;

    private static final long INTERVALO_ATUALIZACAO = QUANTIDADE_HORAS * MINUTOS_POR_HORA * SEGUNDOS_POR_MINUTO * MILISSEGUNDOS_POR_SEGUNDO;

    public static final int JANELA_PRINCIPAL_HORAS = 168;

    @Autowired
    private Dados dados;

    @Autowired
    private EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    private LocalDateTime ultimaAtualizacaoDeTemperaturas;

    public RegistroMeteorologicoServico() {
    }

    public RegistroMeteorologicoServico(Dados dados, EstacaoMeteorologicaServico estacaoMeteorologicaServico) {
        this.dados = dados;
        this.estacaoMeteorologicaServico = estacaoMeteorologicaServico;
    }

    @PostConstruct
    private void inicializarTemperaturas() {
        dados.iniciarTransacao();
        try {
            popularTemperaturasDeEstacoesSemHistorico();
            executarAtualizacaoDeTemperaturas();
            dados.confirmarTransacao();
        } catch (Exception e) {
            dados.desfazerTransacao();
            throw e;
        }
    }

    private void popularTemperaturasDeEstacoesSemHistorico() {
        if (dados.existeAlgumVazio(EstacaoMeteorologica.class, "localizacao.historicoTemperaturas")) {
            atualizarTemperaturasEstacoes(dados.buscarOndeCampoForVazio(EstacaoMeteorologica.class, "localizacao.historicoTemperaturas"));
        }
    }

    @Scheduled(fixedRate = INTERVALO_ATUALIZACAO)
    private void executarAtualizacaoDeTemperaturas() {
        dados.iniciarTransacao();
        try {
            ultimaAtualizacaoDeTemperaturas = LocalDateTime.now();
            atualizarTemperaturasDeEstacoesAssociadasACentroides();
            atualizarTemperaturasDeCentroidesAssociadosAEstacoes();
            dados.confirmarTransacao();
        } catch (Exception e) {
            dados.desfazerTransacao();
        }
    }

    public void atualizarTemperaturasDeEstacoesAssociadasACentroides() {
        List<EstacaoMeteorologica> estacoesAssociadas = dados.buscarEstacoesAssociadasAPropriedades();
        if (!estacoesAssociadas.isEmpty()) {
            atualizarTemperaturasEstacoes(estacoesAssociadas);
        }
    }

    public void atualizarTemperaturasDeCentroidesAssociadosAEstacoes() {
        List<Ponto> pontosAssociados = dados.buscarCentroidesAssociadosAEstacoes();
        if (!pontosAssociados.isEmpty()) {
            atualizarTemperaturasCentroides(pontosAssociados);
        }
    }

    private void atualizarTemperaturasEstacoes(List<EstacaoMeteorologica> estacoes) {
        if (estacoes.isEmpty()) {
            return;
        }

        for (EstacaoMeteorologica estacao : estacoes) {
            try {
                JsonNode dadosJson = estacaoMeteorologicaServico.obterDadosDaEstacao(URL_TEMPERATURAS + estacao.getCodigoEstacao());

                if (dadosJson.isNull()) {
                    return;
                }

                Set<LocalDateTime> datasHorasExistentes = estacao.obterDatasHorasExistentesNoHistoricoTemperaturas();

                for (JsonNode registro : dadosJson) {
                    if (registro.get("data") == null || registro.get("hora") == null || registro.get("temperatura") == null || registro.get("temperatura").isNull()) {
                        continue;
                    }

                    String data = registro.get("data").asText();
                    String hora = String.format("%02d", registro.get("hora").asInt());
                    LocalDateTime dataHoraUTC = LocalDateTime.parse(data + ESPACO_EM_BRANCO + hora, FormatadorUtil.FORMATADOR_DATA_HORA_PARA_COMPARACAO_JSON);
                    ZonedDateTime dataHoraGMT = dataHoraUTC.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of(estacao.getLocalizacao().getFusoHorario()));

                    if (!datasHorasExistentes.contains(dataHoraGMT.toLocalDateTime())) {
                        RegistroMeteorologico temperatura = new RegistroMeteorologico();
                        temperatura.setTemperaturaReal(registro.get("temperatura").asDouble());
                        temperatura.setDataHora(dataHoraGMT.toLocalDateTime());
                        temperatura.setPonto(estacao.getLocalizacao());

                        estacao.getLocalizacao().getHistoricoTemperaturas().add(temperatura);
                        datasHorasExistentes.add(dataHoraGMT.toLocalDateTime());
                    }
                }

                dados.salvar(estacao);
            } catch (IOException e) {
                throw new RuntimeException("Falha: Não foi possível obter histórico de temperaturas para a estação: " + estacao.getCodigoEstacao());
            }
        }
    }

    private void atualizarTemperaturasCentroides(List<Ponto> pontos) {
        for (Ponto ponto : pontos) {
            List<EstacaoMeteorologica> estacoesAssociadas = ponto.getEstacoesMeteorologicas();
            if (!estacoesAssociadas.isEmpty()) {
                RegistroMeteorologico temperaturaCalculada = calcularTemperatura(ponto);
                if (temperaturaCalculada != null) {
                    boolean ehNovaTemperatura = ponto.getHistoricoTemperaturas().stream()
                            .noneMatch(t -> t.getDataHora().equals(temperaturaCalculada.getDataHora()));

                    if (ehNovaTemperatura) {
                        ponto.getHistoricoTemperaturas().add(temperaturaCalculada);
                        dados.salvar(temperaturaCalculada);
                    }
                }
            }
        }
    }

    public RegistroMeteorologico calcularTemperatura(Ponto ponto) {
        List<EstacaoMeteorologica> estacoesRelevantes = estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto);
        if (estacoesRelevantes == null) {
            return null;
        }
        ponto.setEstacoesMeteorologicas(estacoesRelevantes);

        Optional<LocalDateTime> dataHoraMaisRecenteDeTemperaturaEntreEstacoes = estacoesRelevantes.stream()
                .map(e -> e.getLocalizacao().getHistoricoTemperaturas().stream()
                        .filter(t -> t.getTemperaturaReal() != null)
                        .map(RegistroMeteorologico::getDataHora)
                        .max(Comparator.naturalOrder())
                        .orElse(null))
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());

        if (dataHoraMaisRecenteDeTemperaturaEntreEstacoes.isEmpty()) {
            return null;
        }

        double somaTemperaturas = 0.0;
        double somaPesos = 0.0;

        for (EstacaoMeteorologica estacao : estacoesRelevantes) {
            Optional<RegistroMeteorologico> temperaturaMaisRecenteDaEstacao = estacao.getLocalizacao().getHistoricoTemperaturas().stream()
                    .filter(t -> t.getDataHora().equals(dataHoraMaisRecenteDeTemperaturaEntreEstacoes.get()) && t.getTemperaturaReal() != null)
                    .findFirst();

            if (temperaturaMaisRecenteDaEstacao.isPresent()) {
                double temperatura = temperaturaMaisRecenteDaEstacao.get().getTemperaturaReal();
                double peso = ponto.calcularPesoDeProximidadePara(estacao);
                somaTemperaturas += temperatura * peso;
                somaPesos += peso;
            }
        }

        if (somaPesos == PESO_NULO) {
            return null;
        }

        double temperatura = somaTemperaturas / somaPesos;

        RegistroMeteorologico temperaturaCalculada = new RegistroMeteorologico();
        temperaturaCalculada.setPonto(ponto);
        temperaturaCalculada.setTemperaturaCalculada(temperatura);
        temperaturaCalculada.setDataHora(dataHoraMaisRecenteDeTemperaturaEntreEstacoes.get());
        return temperaturaCalculada;

    }

    public RegistroMeteorologico preverRegistroMeteorologico(Ponto ponto, LocalDateTime dataHoraPrevisao) {
        Map<EstacaoMeteorologica, RegistroMeteorologico> temperaturasPrevistasDasEstacoes = preverTemperaturasParaEstacoes(ponto.getEstacoesMeteorologicas(), dataHoraPrevisao);

        double somaTemperaturas = 0.0;
        double somaPesos = 0.0;

        for (Map.Entry<EstacaoMeteorologica, RegistroMeteorologico> entry : temperaturasPrevistasDasEstacoes.entrySet()) {
            EstacaoMeteorologica estacaoMeteorologica = entry.getKey();
            RegistroMeteorologico temperaturaPrevista = entry.getValue();

            if (temperaturaPrevista == null) {
                continue;
            }

            double peso = ponto.calcularPesoDeProximidadePara(estacaoMeteorologica);
            somaTemperaturas += temperaturaPrevista.getTemperaturaPrevista() * peso;
            somaPesos += peso;
        }

        if (somaPesos == PESO_NULO) {
            return null;
        }

        double temperatura = somaTemperaturas / somaPesos;

        RegistroMeteorologico temperaturaPrevista = new RegistroMeteorologico();
        temperaturaPrevista.setDataHora(dataHoraPrevisao);
        temperaturaPrevista.setTemperaturaPrevista(temperatura);
        temperaturaPrevista.setPonto(ponto);

        return temperaturaPrevista;
    }

    private Map<EstacaoMeteorologica, RegistroMeteorologico> preverTemperaturasParaEstacoes (List<EstacaoMeteorologica> estacaoMeteorologicas, LocalDateTime dataHoraPrevisao) {
        Map<EstacaoMeteorologica, List<RegistroMeteorologico>> temperaturasPorEstacao = new HashMap<>();

        for (EstacaoMeteorologica estacao : estacaoMeteorologicas) {
            List<LocalDateTime> datasHorasDecrescentes = obterDatasHorasDescrescentes(dataHoraPrevisao);
            List<RegistroMeteorologico> temperaturasCombinadas = combinarTemperaturasDoBancoDeDadosComDadosHistoricos(estacao, datasHorasDecrescentes);

            if (!temperaturasCombinadas.isEmpty()) {
                temperaturasPorEstacao.put(estacao, temperaturasCombinadas);
            }
        }

        Map<EstacaoMeteorologica, RegistroMeteorologico> previsoesDeCadaEstacao = new HashMap<>();

        for (Map.Entry<EstacaoMeteorologica, List<RegistroMeteorologico>> temperaturasDaEstacao : temperaturasPorEstacao.entrySet()) {
            List<RegistroMeteorologico> temperaturas = temperaturasDaEstacao.getValue();
            if (temperaturas == null || temperaturas.isEmpty()) {
                continue;
            }

            double[] serieTemporal = temperaturas.stream()
                    .mapToDouble(RegistroMeteorologico::getTemperaturaReal)
                    .toArray();

            RegistroMeteorologico temperaturaPrevistaDaEstacao = new RegistroMeteorologico();
            if (serieTemporal.length == CONDICAO_DE_TEMPERATURA_UNICA) {
                temperaturaPrevistaDaEstacao.setTemperaturaPrevista(serieTemporal[0]);
            } else if (serieTemporal.length >= MINIMO_DE_TEMPERATURAS_PARA_PREVISAO) {
                AR modeloAutoRegressivo = AR.fit(serieTemporal, serieTemporal.length - 1);

                double temperaturaPrevista = modeloAutoRegressivo.forecast();
                temperaturaPrevistaDaEstacao.setTemperaturaPrevista(temperaturaPrevista);
            } else {
                throw new IllegalArgumentException("Falha: É necessário pelo menos " + MINIMO_DE_TEMPERATURAS_PARA_PREVISAO + " temperaturas de anos passados para prever a temperatura para a estação: " + temperaturasDaEstacao.getKey().getCodigoEstacao());
            }

            temperaturaPrevistaDaEstacao.setDataHora(dataHoraPrevisao);
            previsoesDeCadaEstacao.put(temperaturasDaEstacao.getKey(), temperaturaPrevistaDaEstacao);
        }

        return previsoesDeCadaEstacao;
    }

    private List<LocalDateTime> obterDatasHorasDescrescentes(LocalDateTime dataHora) {
        List<LocalDateTime> datasHorasDecrescentes = new ArrayList<>();
        int anoDaDataHora = dataHora.getYear();
        for (int ano = anoDaDataHora - 1; ano >= ANO_MAIS_ANTIGO_TEMPERATURAS_HISTORICAS; ano--) {
            datasHorasDecrescentes.add(dataHora.withYear(ano));
        }
        return datasHorasDecrescentes;
    }

    private List<RegistroMeteorologico> combinarTemperaturasDoBancoDeDadosComDadosHistoricos(EstacaoMeteorologica estacaoMeteorologica, List<LocalDateTime> datasHorasDecrescentes) {
        if (datasHorasDecrescentes == null || datasHorasDecrescentes.isEmpty()) {
            return new ArrayList<>();
        }

        List<RegistroMeteorologico> temperaturasExistentesNoBancoDeDados = dados.buscarTemperaturasHistoricas(estacaoMeteorologica.getLocalizacao(), datasHorasDecrescentes);
        Set<LocalDateTime> datasHorasObtidasDoBanco = temperaturasExistentesNoBancoDeDados.stream()
                .map(RegistroMeteorologico::getDataHora)
                .collect(Collectors.toSet());

        LocalDateTime dataHoraDoAnoAnteriorDaPrevisao = datasHorasDecrescentes.getFirst();
        String dataFormatada = FormatadorUtil.formatarDataParaComparacao(dataHoraDoAnoAnteriorDaPrevisao);
        String horaFormatada = FormatadorUtil.formatarHoraParaComparacao(dataHoraDoAnoAnteriorDaPrevisao);

        List<RegistroMeteorologico> temperaturasDosDadosHistoricos = LeitorArquivoUtil.lerTemperaturasHistoricasCsv(dataFormatada, horaFormatada, estacaoMeteorologica);
        List<RegistroMeteorologico> temperaturasDoCsvAusentesNoBancoDeDados = temperaturasDosDadosHistoricos.stream()
                .filter(t -> !datasHorasObtidasDoBanco.contains(t.getDataHora()))
                .toList();

        return Stream.concat(temperaturasExistentesNoBancoDeDados.stream(), temperaturasDoCsvAusentesNoBancoDeDados.stream())
                .sorted(Comparator.comparing(RegistroMeteorologico::getDataHora).reversed())
                .collect(Collectors.toList());
    }

    public LocalDateTime obterUltimaAtualizacaoDeTemperaturas() {
        return ultimaAtualizacaoDeTemperaturas;
    }

    public LocalDateTime obterProximaAtualizacaoDeTemperaturas() {
        if (ultimaAtualizacaoDeTemperaturas != null) {
            return ultimaAtualizacaoDeTemperaturas.plus(QUANTIDADE_HORAS, UNIDADE_DE_TEMPO_DO_INTERVALO.toChronoUnit());
        }
        return null;
    }

    public RegistroMeteorologico preverRegistroMeteorologico(Double latitude, Double longitude, LocalDateTime dataHoraPrevisao) throws URISyntaxException {
        dados.iniciarTransacao();
        Ponto ponto = new Ponto(latitude, longitude);
        ponto.setFusoHorario(ponto.determinarFusoHorario());
        ponto.setEstacoesMeteorologicas(estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto));
        dados.confirmarTransacao();

        Map<EstacaoMeteorologica, Double> temperaturaPrevistaDeCadaEstacao = new HashMap<>();
        Map<EstacaoMeteorologica, Double> precipitacaoPrevistaDeCadaEstacao = new HashMap<>();
        Map<EstacaoMeteorologica, Double> radiacaoSolarPrevistaDeCadaEstacao = new HashMap<>();

        ZonedDateTime dataHoraPrevisaoComFusoHorario = dataHoraPrevisao.atZone(ZoneId.of(ponto.getFusoHorario()));
        LocalDateTime dataHoraPrevisaoEmUTC = dataHoraPrevisaoComFusoHorario.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        LocalDateTime dataHoraFimJanela = dataHoraPrevisaoEmUTC.minusHours(1);
        LocalDateTime dataHoraInicioJanela = dataHoraFimJanela.minusHours(JANELA_PRINCIPAL_HORAS - 1);

        for (EstacaoMeteorologica estacao : ponto.getEstacoesMeteorologicas()) {
            List<RegistroMeteorologico> registrosOriginais = LeitorArquivoUtil.lerJanelaDeRegistros(estacao.getCodigoEstacao(), dataHoraInicioJanela, dataHoraFimJanela);

            List<RegistroMeteorologico> janelaTemp = PrevisaoUtil.criarCopiaProfunda(registrosOriginais);
            PrevisaoUtil.limparJanelaDePrevisao(janelaTemp, estacao.getCodigoEstacao(), RegistroMeteorologico::getTemperaturaReal, RegistroMeteorologico::setTemperaturaReal);
            double temperaturaPrevista = PrevisaoUtil.preverValorDoRegistroMeteorologico(janelaTemp, RegistroMeteorologico::getTemperaturaReal);
            temperaturaPrevistaDeCadaEstacao.put(estacao, temperaturaPrevista);

            List<RegistroMeteorologico> janelaPrec = PrevisaoUtil.criarCopiaProfunda(registrosOriginais);
            PrevisaoUtil.limparJanelaDePrevisao(janelaPrec, estacao.getCodigoEstacao(), RegistroMeteorologico::getPrecipitacaoReal, RegistroMeteorologico::setPrecipitacaoReal);
            double precipitacaoPrevista = PrevisaoUtil.preverValorDoRegistroMeteorologico(janelaPrec, RegistroMeteorologico::getPrecipitacaoReal);
            precipitacaoPrevistaDeCadaEstacao.put(estacao, precipitacaoPrevista);

//            List<RegistroMeteorologico> janelaRad = PrevisaoUtil.criarCopiaProfunda(registrosOriginais);
//            PrevisaoUtil.limparJanelaDePrevisao(janelaRad, estacao.getCodigoEstacao(), RegistroMeteorologico::getRadiacaoSolarReal, RegistroMeteorologico::setRadiacaoSolarReal);
//            double radiacaoSolarPrevista = PrevisaoUtil.preverValorDoRegistroMeteorologico(janelaRad, RegistroMeteorologico::getRadiacaoSolarReal);
//            radiacaoSolarPrevistaDeCadaEstacao.put(estacao, radiacaoSolarPrevista);
        }

        RegistroMeteorologico registroMeteorologicoPrevisto = new RegistroMeteorologico();
        registroMeteorologicoPrevisto.setPonto(ponto);
        registroMeteorologicoPrevisto.setDataHora(dataHoraPrevisao);
        registroMeteorologicoPrevisto.setTemperaturaPrevista(calcularValorDoRegistroMeteorologico(temperaturaPrevistaDeCadaEstacao, ponto));
        registroMeteorologicoPrevisto.setPrecipitacaoPrevista(calcularValorDoRegistroMeteorologico(precipitacaoPrevistaDeCadaEstacao, ponto));
        registroMeteorologicoPrevisto.setRadiacaoSolarPrevista(calcularValorDoRegistroMeteorologico(radiacaoSolarPrevistaDeCadaEstacao, ponto));

        return registroMeteorologicoPrevisto;
    }

    public Double calcularValorDoRegistroMeteorologico(Map<EstacaoMeteorologica, Double> previsaoDeCadaEstacao, Ponto ponto) {
        double somaValoresPrevistos = 0.0;
        double somaPesos = 0.0;

        for (Map.Entry<EstacaoMeteorologica, Double> entry : previsaoDeCadaEstacao.entrySet()) {
            EstacaoMeteorologica estacaoMeteorologica = entry.getKey();
            Double valorPrevisto = entry.getValue();

            if (estacaoMeteorologica == null || valorPrevisto == null) {
                continue;
            }

            double peso = ponto.calcularPesoDeProximidadePara(estacaoMeteorologica);
            somaValoresPrevistos += valorPrevisto * peso;
            somaPesos += peso;
        }

        if (somaPesos == PESO_NULO) {
            return null;
        }

        return somaValoresPrevistos / somaPesos;
    }

}

