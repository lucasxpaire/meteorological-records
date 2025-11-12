package modelo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dados.Dados;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import servico.EstacaoMeteorologicaServico;
import servico.RegistroMeteorologicoServico;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:web/WEB-INF/spring-config.xml"})
public class PontoTest {

    private final Dados dados = new Dados();
    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico = new EstacaoMeteorologicaServico(dados);
    private final RegistroMeteorologicoServico registroMeteorologicoServico = new RegistroMeteorologicoServico();

    @Test
    public void gerarFusoHorario() throws JsonProcessingException {
        Ponto ponto = new Ponto(-29.72047902, -53.705404735);
        ponto.setFusoHorario(ponto.determinarFusoHorario());

        ponto.getHistoricoRegistrosMeteorologicos().add(registroMeteorologicoServico.calcularRegistroMeteorologico(ponto));
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        String pontoJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(ponto);
        System.out.println(pontoJson);
    }

}
