package modelo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import servico.EstacaoMeteorologicaServico;
import servico.TemperaturaServico;

public class PontoTest {

    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico = new EstacaoMeteorologicaServico();
    private final TemperaturaServico temperaturaServico = new TemperaturaServico();

    @Test
    public void gerarFusoHorario() throws JsonProcessingException {
        Ponto ponto = new Ponto(-29.72047902, -53.705404735);
        ponto.setFusoHorario(ponto.determinarFusoHorario());

        ponto.getHistoricoTemperaturas().add(temperaturaServico.calcularTemperatura(ponto));
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        String pontoJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(ponto);
        System.out.println(pontoJson);
    }

}
