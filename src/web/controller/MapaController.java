package web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import modelo.EstacaoMeteorologica;
import modelo.Propriedade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import servico.EstacaoMeteorologicaServico;
import servico.PropriedadeServico;

import java.util.Collections;
import java.util.List;

@Controller
public class MapaController {

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    @GetMapping("/visualizarMapas.html")
    public ModelAndView visualizarMapa() throws JsonProcessingException {
        ModelAndView mv = new ModelAndView("visualizarMapas");
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();

        try {
            List<EstacaoMeteorologica> estacoes = estacaoMeteorologicaServico.listarTodas();
            String estacoesJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(estacoes);
            mv.addObject("estacoesJson", estacoesJson);

            List<Propriedade> propriedades = propriedadeServico.listarTodas();
            String propriedadesJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(propriedades);
            mv.addObject("propriedadesJson", propriedadesJson);

            Propriedade propriedadeMaisRecente = propriedadeServico.buscarMaisRecente();
            String propriedadeMaisRecenteJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(propriedadeMaisRecente);
            mv.addObject("propriedadeMaisRecenteJson", propriedadeMaisRecenteJson);

        } catch (IllegalArgumentException e) {
            mv.addObject("propriedadesJson", conversorJson.writeValueAsString(Collections.emptyList()));
            mv.addObject("propriedadeMaisRecenteJson", "null");
        }
        return mv;
    }

}
