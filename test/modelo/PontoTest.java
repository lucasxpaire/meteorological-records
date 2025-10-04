package modelo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dados.Dados;
import org.junit.Test;
import servico.CorServico;
import servico.EstacaoMeteorologicaServico;

public class PontoTest {

    private final Dados dados = new Dados();
    private final CorServico corServico = new CorServico(dados);
    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico = new EstacaoMeteorologicaServico(dados, corServico);

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
