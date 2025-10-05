package modelo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dados.Dados;
import org.junit.Test;

import java.util.List;

public class PropriedadeTest {

    private final Dados dados = new Dados();

    @Test
    public void verificarJsonDePropriedade() throws JsonProcessingException {
        List<Propriedade> propriedades = dados.listarTodos(Propriedade.class);
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        String propriedadesJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(propriedades);
        System.out.println(propriedadesJson);
    }

    @Test
    public void verificarJsonDePropriedadeEspecificaPorId() throws JsonProcessingException {
        Propriedade propriedade = dados.buscarUnicoPorCampo(Propriedade.class, "id", 12L);
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        String propriedadeJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(propriedade);
        System.out.println(propriedadeJson);
    }
}