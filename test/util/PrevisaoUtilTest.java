package util;

import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import modelo.RegistroMeteorologico;
import org.junit.Test;
import servico.EstacaoMeteorologicaServico;
import servico.RegistroMeteorologicoServico;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class PrevisaoUtilTest {

    public static double LATITUDE = -29.72047902;
    public static double LONGITUDE = -53.705404735;
    public static Ponto ponto = new Ponto(LATITUDE, LONGITUDE);

    private final Dados dados = new Dados();
    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico = new EstacaoMeteorologicaServico(dados);
    private final RegistroMeteorologicoServico registroMeteorologicoServico = new RegistroMeteorologicoServico(dados, estacaoMeteorologicaServico);

    @Test
    public void obterDataHoraComFusoHorario() {
        ponto.setFusoHorario(ponto.determinarFusoHorario());
        LocalDateTime dataHoraPrevisao = LocalDateTime.of(2024, 1, 1, 12, 0);
        ZonedDateTime dataHoraPrevisaoComFusoHorario = dataHoraPrevisao.atZone(ZoneId.of(ponto.getFusoHorario()));
        LocalDateTime dataHoraPrevisaoEmUTC = dataHoraPrevisaoComFusoHorario.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        System.out.println(dataHoraPrevisaoEmUTC);
    }

    @Test
    public void obterTodosOsArquivosDeEstacao() {
        dados.iniciarTransacao();
        EstacaoMeteorologica estacao = dados.buscarUnicoPorCampo(EstacaoMeteorologica.class, "codigoEstacao", "A803");
        dados.confirmarTransacao();
        List<String> caminhosArquivos = LeitorArquivoUtil.obterCaminhosDeArquivosCsvDaEstacao(estacao.getCodigoEstacao());
        for (String caminho : caminhosArquivos) {
            System.out.println(caminho);
        }

    }

    @Test
    public void prever() {

        LocalDateTime dataHoraAgora = LocalDateTime.now();
        LocalDateTime dataHoraPrevista = dataHoraAgora.plusHours(1).withMinute(0).withSecond(0);

        dados.iniciarTransacao();
        ponto.setEstacoesMeteorologicas(estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto));
        RegistroMeteorologico registroMeteorologico = registroMeteorologicoServico.preverRegistroMeteorologico(ponto, dataHoraPrevista);
        dados.confirmarTransacao();

        System.out.println(registroMeteorologico.getTemperaturaCalculada());
    }

    @Test
    public void testarLeituraCsvHistorico() {
        String dataDeBusca = "01/02/2024";
        String horaDeBusca = "1200";
        String codigoEstacao = "A803";

        System.out.println("Iniciando teste de leitura para Estação: " + codigoEstacao);
        System.out.println("Buscando Data: " + dataDeBusca + " | Hora: " + horaDeBusca);
        System.out.println("------------------------------------------------------");

        dados.iniciarTransacao();
        try {
            EstacaoMeteorologica estacao = dados.buscarUnicoPorCampo(EstacaoMeteorologica.class, "codigoEstacao", codigoEstacao);
            assertNotNull("Estação " + codigoEstacao + " não encontrada no banco.", estacao);

            System.out.println("Arquivos CSV encontrados para " + codigoEstacao + ":");
            List<String> caminhosArquivos = LeitorArquivoUtil.obterCaminhosDeArquivosCsvDaEstacao(estacao.getCodigoEstacao());
            caminhosArquivos.forEach(System.out::println);
            System.out.println("------------------------------------------------------");

            List<RegistroMeteorologico> registros = LeitorArquivoUtil.lerRegistrosMeteorologicosHistoricosCsv(dataDeBusca, horaDeBusca, estacao);

            System.out.println("Registros lidos do CSV (" + registros.size() + " encontrados):");
            if (registros.isEmpty()) {
                System.out.println("Nenhum registro encontrado para a data/hora especificada.");
            } else {
                for (RegistroMeteorologico registro : registros) {
                    System.out.println(
                            "Data: " + registro.getDataHora().format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_COMPARACAO_CSV) +
                                    " | Temp: " + registro.getTemperaturaReal() +
                                    " | Precip: " + registro.getPrecipitacaoReal() +
                                    " | Rad: " + registro.getRadiacaoSolarReal()
                    );
                }
            }

            assertFalse("O leitor não deveria retornar uma lista vazia. Verifique os caminhos e os dados de entrada.", registros.isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            fail("O teste falhou com uma exceção: " + e.getMessage());
        } finally {
            dados.confirmarTransacao();
        }
    }
}
