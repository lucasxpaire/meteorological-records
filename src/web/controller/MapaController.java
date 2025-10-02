package web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Propriedade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class MapaController {

    @Autowired
    private Dados dados;

    @GetMapping("/visualizarMapas.html")
    public ModelAndView visualizarMapa() {
        ModelAndView mv = new ModelAndView("visualizarMapas");

        try {
            ObjectMapper conversorJson = new ObjectMapper();
            conversorJson.findAndRegisterModules();

            List<EstacaoMeteorologica> estacoes = dados.listarTodos(EstacaoMeteorologica.class);
            String estacoesJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(estacoes);
            mv.addObject("estacoesJson", estacoesJson);

            List<Propriedade> propriedades = dados.listarTodos(Propriedade.class);
            String propriedadesJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(propriedades);
            mv.addObject("propriedadesJson", propriedadesJson);

            Propriedade propriedadeMaisRecente = dados.buscarMaisRecente(Propriedade.class);
            String propriedadeMaisRecenteJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(propriedadeMaisRecente);
            mv.addObject("propriedadeMaisRecenteJson", propriedadeMaisRecenteJson);

        } catch (Exception e) {
            // Aparecer mensagem de erro
        }

        return mv;
    }

}
