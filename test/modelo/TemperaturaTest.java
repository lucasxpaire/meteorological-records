package modelo;

import org.junit.Test;

import servico.TemperaturaServico;
import util.FormatadorUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TemperaturaTest {

    public static final double LATITUDE = -29.689734243074223;
    public static final double LONGITUDE = -53.78460970215507;

    private final TemperaturaServico temperaturaServico = new TemperaturaServico();

    @Test
    public void gerarPrevisaoTemperatura() {
        Ponto ponto = new Ponto(LATITUDE, LONGITUDE);
        ponto.setFusoHorario(ponto.determinarFusoHorario());
        int hora = 1;
        LocalDate data = LocalDate.of(2025, 10, 6);
        LocalDateTime dataHoraPrevisao = data.atTime(hora, 0);


        Temperatura temperatura = temperaturaServico.preverTemperatura(ponto, dataHoraPrevisao);
        System.out.println(" DataHora: " + temperatura.getDataHora().format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO) + " | Temperatura: " + FormatadorUtil.formatarPontoDecimalParaVirgula(temperatura.getTemperaturaPrevista()) + " °C");

    }
}