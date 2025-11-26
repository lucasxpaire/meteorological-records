package modelo;

import dados.Dados;
import org.junit.Test;

import servico.PontoServico;
import servico.RegistroMeteorologicoServico;
import util.FormatadorUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class RegistroMeteorologicoTest {

    public static final double LATITUDE = -29.689734243074223;
    public static final double LONGITUDE = -53.78460970215507;

    private final RegistroMeteorologicoServico registroMeteorologicoServico = new RegistroMeteorologicoServico();
    private final Dados dados = new Dados();
    private final PontoServico pontoServico = new PontoServico(dados);

    @Test
    public void gerarPrevisaoTemperatura() {
        Ponto ponto = new Ponto(LATITUDE, LONGITUDE);
        ponto.setFusoHorario(ponto.determinarFusoHorario());
        int hora = 1;
        LocalDate data = LocalDate.of(2025, 10, 6);
        LocalDateTime dataHoraPrevisao = data.atTime(hora, 0);


        RegistroMeteorologico temperatura = registroMeteorologicoServico.preverRegistroMeteorologico(ponto, dataHoraPrevisao);
        System.out.println(" DataHora: " + temperatura.getDataHora().format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO) + " | Temperatura: " + FormatadorUtil.formatarPontoDecimalParaVirgula(temperatura.getTemperaturaPrevista()) + " °C");

    }

    @Test
    public void testarPesos() {
        dados.iniciarTransacao();
        Ponto ponto = pontoServico.buscarPorId(780L);
        Map<String, Double> pesos = ponto.calcularPesosDasEstacoes();

        for (String chave : pesos.keySet()) {
            System.out.println(chave + ": " + pesos.get(chave));
        }

        dados.confirmarTransacao();
    }
}