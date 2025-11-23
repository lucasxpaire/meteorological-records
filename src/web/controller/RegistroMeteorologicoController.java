package web.controller;

import com.fasterxml.jackson.annotation.JsonView;
import modelo.Ponto;
import modelo.RegistroMeteorologico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import servico.EstacaoMeteorologicaServico;
import servico.PontoServico;
import servico.RegistroMeteorologicoServico;
import web.json.RegistroMeteorologicoJson;

import java.time.LocalDateTime;

@Controller
public class RegistroMeteorologicoController {

    @Autowired
    private EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    @Autowired
    private PontoServico pontoServico;

    @Autowired
    private RegistroMeteorologicoServico registroMeteorologicoServico;

    @JsonView(RegistroMeteorologicoJson.Previsao.class)
    @ResponseBody
    @GetMapping(value = "/preverRegistroMeteorologico", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegistroMeteorologico preverRegistroDoPonto(@RequestParam("latitude") Double latitude, @RequestParam("longitude") Double longitude) {
        LocalDateTime dataHoraPrevista = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0);

        Ponto ponto = new Ponto(latitude, longitude);
        ponto.setEstacoesMeteorologicas(estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto));
        return registroMeteorologicoServico.preverRegistroMeteorologico(ponto, dataHoraPrevista);
    }

    @JsonView(RegistroMeteorologicoJson.Previsao.class)
    @ResponseBody
    @GetMapping(value = "/preverRegistroMeteorologicoParaCentroide", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegistroMeteorologico preverRegistroParaCentroide(@RequestParam("idCentroide") Long idCentroide) {
        Ponto ponto = pontoServico.buscarPorId(idCentroide);

        LocalDateTime dataHoraPrevista = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0);
        RegistroMeteorologico registroMeteorologico = registroMeteorologicoServico.preverRegistroMeteorologico(ponto, dataHoraPrevista);
        registroMeteorologicoServico.salvar(registroMeteorologico);
        return registroMeteorologico;
    }

    @JsonView(RegistroMeteorologicoJson.Calculada.class)
    @ResponseBody
    @GetMapping(value = "/calcularRegistroMeteorologico", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegistroMeteorologico calcularRegistroDoPonto(@RequestParam("latitude") Double latitude, @RequestParam("longitude") Double longitude) {
        Ponto ponto = new Ponto(latitude, longitude);
        ponto.setEstacoesMeteorologicas(estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto));
        return registroMeteorologicoServico.calcularRegistroMeteorologico(ponto);
    }
}
