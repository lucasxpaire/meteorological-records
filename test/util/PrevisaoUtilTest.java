package util;

import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import modelo.RegistroMeteorologico;
import org.junit.Test;
import servico.EstacaoMeteorologicaServico;

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
}