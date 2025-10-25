package servico;

import com.fasterxml.jackson.databind.JsonNode;
import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import modelo.Temperatura;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static util.FormatadorUtil.ESPACO_EM_BRANCO;

@Service
public class TemperaturaServico {

    private static final String URL_TEMPERATURAS = "https://megatecnologia.com.br/silas.json?chave=A5AC5-6AA87-69587-2B9B2-A7BE1-C99F9-21&estacao=";
    private static final String URL_TEMPERATURAS2 = "http://192.168.1.2:8081/controle/silas.json?chave=A5AC5-6AA87-69587-2B9B2-A7BE1-C99F9-21&estacao=";

    private static final double PESO_NULO = 0.0;

    private static final int ANO_MAIS_ANTIGO_TEMPERATURAS_HISTORICAS = 2020;

    private static final int MINIMO_DE_TEMPERATURAS_PARA_PREVISAO = 2;
    private static final int CONDICAO_DE_TEMPERATURA_UNICA = 1;

    private static final long INTERVALO_PARA_EXECUTAR = 24L;
    private static final TimeUnit UNIDADE_DE_TEMPO_DO_INTERVALO = TimeUnit.HOURS;

    @Autowired
    private Dados dados;

    @Autowired
    private EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    private LocalDateTime ultimaAtualizacaoDeTemperaturas;

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

    private void executarAtualizacaoDeTemperaturas() {
        ultimaAtualizacaoDeTemperaturas = LocalDateTime.now();
        atualizarTemperaturasDeEstacoesAssociadasACentroides();
        atualizarTemperaturasDeCentroidesAssociadosAEstacoes();
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
                        Temperatura temperatura = new Temperatura();
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
                Temperatura temperaturaCalculada = calcularTemperatura(ponto);
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

    public Temperatura calcularTemperatura(Ponto ponto) {
        List<EstacaoMeteorologica> estacoesRelevantes = estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto);
        if (estacoesRelevantes == null) {
            return null;
        }
        ponto.setEstacoesMeteorologicas(estacoesRelevantes);

        Optional<LocalDateTime> dataHoraMaisRecenteDeTemperaturaEntreEstacoes = estacoesRelevantes.stream()
                .map(e -> e.getLocalizacao().getHistoricoTemperaturas().stream()
                        .filter(t -> t.getTemperaturaReal() != null)
                        .map(Temperatura::getDataHora)
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
            Optional<Temperatura> temperaturaMaisRecenteDaEstacao = estacao.getLocalizacao().getHistoricoTemperaturas().stream()
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

        Temperatura temperaturaCalculada = new Temperatura();
        temperaturaCalculada.setPonto(ponto);
        temperaturaCalculada.setTemperaturaCalculada(temperatura);
        temperaturaCalculada.setDataHora(dataHoraMaisRecenteDeTemperaturaEntreEstacoes.get());
        return temperaturaCalculada;

    }

    public Temperatura preverTemperatura(Ponto ponto, LocalDateTime dataHoraPrevisao) {
        List<EstacaoMeteorologica> estacoesRelevantes = estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto);
        if (estacoesRelevantes == null) {
            return null;
        }
        ponto.setEstacoesMeteorologicas(estacoesRelevantes);

        Map<EstacaoMeteorologica, Temperatura> temperaturasPrevistasDasEstacoes = preverTemperaturasParaEstacoes(estacoesRelevantes, dataHoraPrevisao);

        double somaTemperaturas = 0.0;
        double somaPesos = 0.0;

        for (Map.Entry<EstacaoMeteorologica, Temperatura> entry : temperaturasPrevistasDasEstacoes.entrySet()) {
            EstacaoMeteorologica estacaoMeteorologica = entry.getKey();
            Temperatura temperaturaPrevista = entry.getValue();

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

        Temperatura temperaturaPrevista = new Temperatura();
        temperaturaPrevista.setDataHora(dataHoraPrevisao);
        temperaturaPrevista.setTemperaturaPrevista(temperatura);
        temperaturaPrevista.setPonto(ponto);

        return temperaturaPrevista;
    }

    private Map<EstacaoMeteorologica, Temperatura> preverTemperaturasParaEstacoes (List<EstacaoMeteorologica> estacaoMeteorologicas, LocalDateTime dataHoraPrevisao) {
        Map<EstacaoMeteorologica, List<Temperatura>> temperaturasPorEstacao = new HashMap<>();

        for (EstacaoMeteorologica estacao : estacaoMeteorologicas) {
            List<LocalDateTime> datasHorasDecrescentes = obterDatasHorasDescrescentes(dataHoraPrevisao);
            List<Temperatura> temperaturasCombinadas = combinarTemperaturasDoBancoDeDadosComDadosHistoricos(estacao, datasHorasDecrescentes);

            if (!temperaturasCombinadas.isEmpty()) {
                temperaturasPorEstacao.put(estacao, temperaturasCombinadas);
            }
        }

        Map<EstacaoMeteorologica, Temperatura> previsoesDeCadaEstacao = new HashMap<>();

        for (Map.Entry<EstacaoMeteorologica, List<Temperatura>> temperaturasDaEstacao : temperaturasPorEstacao.entrySet()) {
            List<Temperatura> temperaturas = temperaturasDaEstacao.getValue();
            if (temperaturas == null || temperaturas.isEmpty()) {
                continue;
            }

            double[] serieTemporal = temperaturas.stream()
                    .mapToDouble(Temperatura::getTemperaturaReal)
                    .toArray();

            Temperatura temperaturaPrevistaDaEstacao = new Temperatura();
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

    private List<Temperatura> combinarTemperaturasDoBancoDeDadosComDadosHistoricos(EstacaoMeteorologica estacaoMeteorologica, List<LocalDateTime> datasHorasDecrescentes) {
        if (datasHorasDecrescentes == null || datasHorasDecrescentes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Temperatura> temperaturasDoBancoDeDados = dados.buscarTemperaturasHistoricas(estacaoMeteorologica.getLocalizacao(), datasHorasDecrescentes);
        Set<LocalDateTime> datasHorasObtidasDoBanco = temperaturasDoBancoDeDados.stream()
                .map(Temperatura::getDataHora)
                .collect(Collectors.toSet());

        LocalDateTime dataHoraDoAnoAnteriorDaPrevisao = datasHorasDecrescentes.getFirst();
        String dataFormatada = FormatadorUtil.formatarDataParaComparacao(dataHoraDoAnoAnteriorDaPrevisao);
        String horaFormatada = FormatadorUtil.formatarHoraParaComparacao(dataHoraDoAnoAnteriorDaPrevisao);

        List<Temperatura> temperaturasDosDadosHistoricos = LeitorArquivoUtil.lerTemperaturasHistoricasCsv(dataFormatada, horaFormatada, estacaoMeteorologica);
        List<Temperatura> temperaturasDoCsvAusentesNoBanco = temperaturasDosDadosHistoricos.stream()
                .filter(t -> !datasHorasObtidasDoBanco.contains(t.getDataHora()))
                .toList();

        return Stream.concat(temperaturasDoBancoDeDados.stream(), temperaturasDoCsvAusentesNoBanco.stream())
                .sorted(Comparator.comparing(Temperatura::getDataHora).reversed())
                .collect(Collectors.toList());
    }

    public LocalDateTime obterUltimaAtualizacaoDeTemperaturas() {
        return ultimaAtualizacaoDeTemperaturas;
    }

    public LocalDateTime obterProximaAtualizacaoDeTemperaturas() {
        if (ultimaAtualizacaoDeTemperaturas != null) {
            return ultimaAtualizacaoDeTemperaturas.plus(INTERVALO_PARA_EXECUTAR, UNIDADE_DE_TEMPO_DO_INTERVALO.toChronoUnit());
        }
        return null;
    }
}

