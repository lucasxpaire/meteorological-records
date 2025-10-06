package servico;

import com.fasterxml.jackson.databind.JsonNode;
import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import modelo.Temperatura;
import org.springframework.stereotype.Service;
import smile.timeseries.AR;
import util.EscritorUtil;
import util.FormatadorUtil;
import util.JsonUtil;
import util.LeitorArquivoUtil;

import javax.annotation.PostConstruct;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TemperaturaServico {

    public static final long ATRASO_INICIAL_PARA_EXECUCAO = 0L;
    public static final long INTERVALO_PARA_EXECUTAR = 24L;
    public static final TimeUnit UNIDADE_DE_TEMPO_DO_INTERVALO = TimeUnit.HOURS;

    private static final int MINIMO_DE_TEMPERATURAS_PARA_PREVISAO = 2;
    private static final int CONDICAO_DE_TEMPERATURA_UNICA = 1;

    private static final int ANO_INICIO_DADOS_HISTORICOS = 2020;
    private static final int MES_FEVEREIRO = 2;
    private static final int DIA_BISSEXTO = 29;
    private static final int DIA_NAO_BISSEXTO = 28;

    private final Dados dados;
    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    private final ScheduledExecutorService agendador = Executors.newSingleThreadScheduledExecutor();

    public TemperaturaServico(Dados dados, EstacaoMeteorologicaServico estacaoMeteorologicaServico) {
        this.dados = dados;
        this.estacaoMeteorologicaServico = estacaoMeteorologicaServico;
    }

    @PostConstruct
    public void inicializarServicos() {
        popularHistoricosIniciaisSeNecessario();
        iniciarRotinaDeAtualizacaoAutomatica();
    }

    public void iniciarRotinaDeAtualizacaoAutomatica() {
        Runnable tarefa = this::executarAtualizacaoPeriodica;
        agendador.scheduleAtFixedRate(tarefa, ATRASO_INICIAL_PARA_EXECUCAO, INTERVALO_PARA_EXECUTAR, UNIDADE_DE_TEMPO_DO_INTERVALO);
    }

    public void executarAtualizacaoPeriodica() {
        dados.iniciarTransacao();
        try {
            atualizarTemperaturasDeEstacoesEPontosAssociados();
            preencherTemperaturasReaisNasPrevisoes();
            dados.confirmarTransacao();
        } catch (Exception e) {
            dados.desfazerTransacao();
            EscritorUtil.escreverEmNovaLinha("ROTINA AGENDADA: Ocorreu um erro durante a execução da tarefa agendada: " + e.getMessage());
        }
    }

    private void popularHistoricosIniciaisSeNecessario() {
        dados.iniciarTransacao();
        List<EstacaoMeteorologica> estacoesSemHistorico = dados.buscarComCampoNaoVazio(EstacaoMeteorologica.class, "localizacao.historicoTemperaturas");

        if (estacoesSemHistorico.isEmpty()) {
            return;
        }

        for (EstacaoMeteorologica estacao : estacoesSemHistorico) {
            try {
                popularHistoricoInicialPara(estacao);
                dados.salvar(estacao);
            } catch (Exception e) {
                EscritorUtil.escreverEmNovaLinha("Falha: estação " + estacao.getCodigoEstacao() + ", não possui histórico de temperaturas.");
            }
        }
        dados.confirmarTransacao();
    }

    public void salvar(Temperatura temperatura) {
        if (validarTemperatura(temperatura)) {
            dados.salvar(temperatura);
        } else {
            throw new RuntimeException("Dados da temperatura são inválidos.");
        }
    }

    public void atualizarTemperaturasDeEstacoesEPontosAssociados() {
        List<EstacaoMeteorologica> estacoesAssociadas = dados.buscarEstacoesAssociadasAPropriedades();
        if (!estacoesAssociadas.isEmpty()) {
            atualizarTemperaturasDasEstacoes(estacoesAssociadas);
        }

        List<Ponto> pontosAssociados = dados.buscarPontosCentraisDePropriedadesComEstacoes();
        if (!pontosAssociados.isEmpty()) {
            atualizarTemperaturaDePontosAssociados(pontosAssociados);
        }
    }

    public void preencherTemperaturasReaisNasPrevisoes() {
        List<Temperatura> listaTemperaturasPrevistas = dados.buscarPrevisoesComTemperaturaRealNula();

        if (listaTemperaturasPrevistas.isEmpty()) {
            return;
        }

        for (Temperatura temperaturaPrevista : listaTemperaturasPrevistas) {
            Ponto ponto = temperaturaPrevista.getPonto();
            List<EstacaoMeteorologica> estacoesRelevantes = estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto);

            Double temperaturaRealEstimada = ponto.interpolarTemperaturaHistorica(estacoesRelevantes, temperaturaPrevista.getDataHora());

            if (temperaturaRealEstimada != null) {
                temperaturaPrevista.setTemperaturaReal(temperaturaRealEstimada);
                if (temperaturaPrevista.getTemperaturaPrevista() != null) {
                    temperaturaPrevista.setDiferenca(temperaturaPrevista.getTemperaturaPrevista() - temperaturaRealEstimada);
                }
                salvar(temperaturaPrevista);
            }
        }
    }

    public Temperatura preverTemperaturaParaPonto(Ponto ponto, LocalDateTime dataHoraPrevista) {
        List<EstacaoMeteorologica> estacoes = estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto);

        Map<EstacaoMeteorologica, List<Temperatura>> historicoTemperaturasPorEstacao = new HashMap<>();
        for (EstacaoMeteorologica estacao : estacoes) {
            List<LocalDateTime> datasAnteriores = obterDatasAnterioresParaBusca(dataHoraPrevista);
            List<Temperatura> temperaturasCombinadas = buscarECombinarHistoricoParaPrevisao(estacao, datasAnteriores);

            if (!temperaturasCombinadas.isEmpty()) {
                historicoTemperaturasPorEstacao.put(estacao, temperaturasCombinadas);
            }
        }

        Map<EstacaoMeteorologica, Temperatura> temperaturasPrevistasPorEstacao = preverTemperaturasParaEstacoes(historicoTemperaturasPorEstacao, dataHoraPrevista);
        Temperatura temperaturaPrevita = ponto.preverTemperatura(dataHoraPrevista, temperaturasPrevistasPorEstacao);
        if (temperaturaPrevita != null) {
            ponto.getHistoricoTemperaturas().add(temperaturaPrevita);
        }
        return temperaturaPrevita;
    }

    public List<Temperatura> listarTodasAsPrevisoes() {
        return dados.listarTodasAsPrevisoes();
    }

    private List<Temperatura> buscarECombinarHistoricoParaPrevisao(EstacaoMeteorologica estacao, List<LocalDateTime> datasNecessarias) {
        List<Temperatura> temperaturasDoBanco = buscarTemperaturasHistoricas(estacao.getLocalizacao(), datasNecessarias);
        Set<LocalDateTime> datasDoBanco = temperaturasDoBanco.stream()
                .map(Temperatura::getDataHora)
                .collect(Collectors.toSet());

        LocalDateTime dataHoraReferencia = datasNecessarias.getFirst();
        String dataReferenciaFormatada = FormatadorUtil.formatarDataParaComparacao(dataHoraReferencia);
        String horaReferenciaFormatada = FormatadorUtil.formatarHoraParaComparacao(dataHoraReferencia);

        List<Temperatura> temperaturasDoCsv = LeitorArquivoUtil.lerTemperaturasHistoricasCsv(dataReferenciaFormatada, horaReferenciaFormatada, estacao);

        List<Temperatura> temperaturasFaltantesDoCsv = temperaturasDoCsv.stream()
                .filter(t -> !datasDoBanco.contains(t.getDataHora()))
                .toList();

        return Stream.concat(temperaturasDoBanco.stream(), temperaturasFaltantesDoCsv.stream())
                .sorted(Comparator.comparing(Temperatura::getDataHora).reversed())
                .collect(Collectors.toList());
    }

    private void atualizarTemperaturasDasEstacoes(List<EstacaoMeteorologica> estacoesAssociadas) {
        try {
            for (EstacaoMeteorologica estacao : estacoesAssociadas) {
                popularHistoricoInicialPara(estacao);
                dados.salvar(estacao);
            }
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível atualizar as temperaturas das estações associadas.");
        }
    }

    private void atualizarTemperaturaDePontosAssociados(List<Ponto> pontos) {
        for (Ponto ponto : pontos) {
            List<EstacaoMeteorologica> estacoesAssociadas = ponto.getEstacoesMeteorologicas();
            if (!estacoesAssociadas.isEmpty()) {
                Temperatura novaTemperatura = ponto.interpolarTemperaturaAtual(estacoesAssociadas);
                if (novaTemperatura != null) {
                    boolean jaExisteTemperatura = ponto.getHistoricoTemperaturas().stream()
                            .anyMatch(t -> t.getDataHora().equals(novaTemperatura.getDataHora()));

                    if (!jaExisteTemperatura) {
                        salvar(novaTemperatura);
                    }
                }
            }
        }
    }

    private List<Temperatura> buscarTemperaturasHistoricas(Ponto ponto, List<LocalDateTime> datas) {
        return dados.buscarTemperaturasHistoricas(ponto, datas);
    }

    private Map<EstacaoMeteorologica, Temperatura> preverTemperaturasParaEstacoes(Map<EstacaoMeteorologica, List<Temperatura>> temperaturasPorEstacao, LocalDateTime dataHoraPrevista) {
        Map<EstacaoMeteorologica, Temperatura> previsoes = new HashMap<>();

        for (Map.Entry<EstacaoMeteorologica, List<Temperatura>> historicoPorEstacao : temperaturasPorEstacao.entrySet()) {
            List<Temperatura> temperaturasHistoricasDaEstacao = historicoPorEstacao.getValue();
            if (temperaturasHistoricasDaEstacao == null || temperaturasHistoricasDaEstacao.isEmpty()) {
                continue;
            }

            double[] serieTemporalTemperaturas = temperaturasHistoricasDaEstacao.stream()
                    .mapToDouble(Temperatura::getTemperaturaReal)
                    .toArray();

            Temperatura temperaturaFutura = new Temperatura();
            if (serieTemporalTemperaturas.length == CONDICAO_DE_TEMPERATURA_UNICA) {
                temperaturaFutura.setTemperaturaPrevista(serieTemporalTemperaturas[0]);
            } else if (serieTemporalTemperaturas.length >= MINIMO_DE_TEMPERATURAS_PARA_PREVISAO) {
                AR modelo = AR.fit(serieTemporalTemperaturas, serieTemporalTemperaturas.length - 1);
                double previsao = modelo.forecast();
                temperaturaFutura.setTemperaturaPrevista(previsao);
            } else {
                throw new IllegalArgumentException("É necessário pelo menos " + MINIMO_DE_TEMPERATURAS_PARA_PREVISAO + " temperaturas de anos passados para prever a temperatura para a estação: " + historicoPorEstacao.getKey().getCodigoEstacao());
            }

            Temperatura ultimoRegisto = temperaturasHistoricasDaEstacao.getLast();
            if (ultimoRegisto == null || ultimoRegisto.getDataHora() == null) {
                throw new IllegalStateException("O registo histórico mais antigo da estação " + historicoPorEstacao.getKey().getCodigoEstacao() + " contém dados de data inválidos.");
            }

            LocalDateTime dataHoraProjetada = projetarDataParaAnoDaPrevisao(ultimoRegisto.getDataHora(), dataHoraPrevista.getYear());
            temperaturaFutura.setDataHora(dataHoraProjetada);

            previsoes.put(historicoPorEstacao.getKey(), temperaturaFutura);
        }
        return previsoes;
    }

    private List<LocalDateTime> obterDatasAnterioresParaBusca(LocalDateTime dataHoraPrevista) {
        List<LocalDateTime> datasNecessarias = new ArrayList<>();
        int anoDaPrevisao = dataHoraPrevista.getYear();
        for (int ano = anoDaPrevisao - 1; ano >= ANO_INICIO_DADOS_HISTORICOS; ano--) {
            datasNecessarias.add(dataHoraPrevista.withYear(ano));
        }
        return datasNecessarias;
    }

    public void popularHistoricoInicialPara(EstacaoMeteorologica estacao) {
        try {
            JsonNode dadosJson = JsonUtil.obterDadosDoJson(JsonUtil.URL_TEMPERATURAS2 + estacao.getCodigoEstacao());

            List<Temperatura> historicoExistente = estacao.getLocalizacao().getHistoricoTemperaturas();
            Set<LocalDateTime> datasExistentes = historicoExistente.stream()
                    .map(Temperatura::getDataHora)
                    .collect(Collectors.toSet());

            List<Temperatura> novasTemperaturas = new ArrayList<>();

            for (JsonNode registro : dadosJson) {
                if (registro.get("data") == null || registro.get("hora") == null || registro.get("temperatura") == null || registro.get("temperatura").isNull()) {
                    continue;
                }

                String data = registro.get("data").asText();
                String hora = String.format("%02d", registro.get("hora").asInt());
                LocalDateTime dataHoraUtc = LocalDateTime.parse(data + " " + hora, FormatadorUtil.FORMATADOR_DATA_HORA_PARA_COMPARACAO_JSON);

                ZonedDateTime dataHoraLocal = dataHoraUtc.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of(estacao.getLocalizacao().getFusoHorario()));
                LocalDateTime dataHoraFormatado = dataHoraLocal.toLocalDateTime();

                if (!datasExistentes.contains(dataHoraFormatado)) {
                    Temperatura temperatura = new Temperatura();
                    temperatura.setTemperaturaReal(registro.get("temperatura").asDouble());
                    temperatura.setDataHora(dataHoraFormatado);
                    temperatura.setPonto(estacao.getLocalizacao());
                    novasTemperaturas.add(temperatura);

                    datasExistentes.add(dataHoraFormatado);
                }
            }

            historicoExistente.addAll(novasTemperaturas);
            historicoExistente.sort(Comparator.comparing(Temperatura::getDataHora).reversed());

        } catch (IOException e) {
            throw new RuntimeException("Falha ao obter histórico de temperaturas para a estação " + estacao.getCodigoEstacao(), e);
        }
    }

    private LocalDateTime projetarDataParaAnoDaPrevisao(LocalDateTime dataHistorica, int anoDaPrevisao) {
        boolean ehDiaBissexto = dataHistorica.getMonthValue() == MES_FEVEREIRO && dataHistorica.getDayOfMonth() == DIA_BISSEXTO;
        boolean anoPrevisaoNaoEhBissexto = !Year.isLeap(anoDaPrevisao);

        if (ehDiaBissexto && anoPrevisaoNaoEhBissexto) {
            return dataHistorica.withDayOfMonth(DIA_NAO_BISSEXTO).withYear(anoDaPrevisao);
        } else {
            return dataHistorica.withYear(anoDaPrevisao);
        }
    }

    private boolean validarTemperatura(Temperatura temperatura) {
        return temperatura != null && temperatura.getPonto() != null;
    }
}
