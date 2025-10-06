package modelo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import servico.EstacaoMeteorologicaServico;

public class PontoTest {

    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico = new EstacaoMeteorologicaServico();

    @Test
    public void gerarFusoHorario() throws JsonProcessingException {
        Ponto ponto = new Ponto(-29.72047902, -53.705404735);
        ponto.setFusoHorario(ponto.determinarFusoHorario());

        ponto.setEstacoesMeteorologicas(estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto));
        ponto.getHistoricoTemperaturas().add(ponto.interpolarTemperaturaAtual(ponto.getEstacoesMeteorologicas()));
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        String pontoJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(ponto);
        System.out.println(pontoJson);
    }

}
