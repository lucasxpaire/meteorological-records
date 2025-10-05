package modelo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dados.Dados;
import org.junit.Test;

import java.util.List;

public class EstacaoMeteorologicaTest {

    private final Dados dados = new Dados();

    @Test
    public void verificarJsonDeEstacaoMeteorologica() throws JsonProcessingException {
        List<EstacaoMeteorologica> estacoes = dados.listarTodos(EstacaoMeteorologica.class);
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        String estacoesJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(estacoes);
        System.out.println(estacoesJson);
    }

    @Test
    public void verificarJsonDeEstacaoMeteorologicaEspecificaPorCodigo() throws JsonProcessingException {
        EstacaoMeteorologica estacao = dados.buscarUnicoPorCampo(EstacaoMeteorologica.class, "codigoEstacao", "A803");
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        String estacoesJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(estacao);
        System.out.println(estacoesJson);
    }
}