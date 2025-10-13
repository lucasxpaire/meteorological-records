package web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import modelo.Propriedade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import servico.EstacaoMeteorologicaServico;
import servico.PropriedadeServico;
import servico.TemperaturaServico;
import web.command.ControleMapaCommand;
import web.validator.ControleMapaValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class MapaController {

    public static final String BUSCAR_TODAS_PROPRIEDADES = "Todas as propriedades";
    public static final String BUSCAR_PROPRIEDADE_MAIS_RECENTE = "Propriedade mais recente";
    public static final String BUSCAR_PROPRIEDADES_POR_NOME = "Buscar por nome";
    public static final String BUSCAR_PROPRIEDADES_POR_CPF = "Buscar por cpf";

    public static final Map<Integer, String> OPCOES_CONTROLE_MAPA = Map.of(1, BUSCAR_TODAS_PROPRIEDADES, 2, BUSCAR_PROPRIEDADE_MAIS_RECENTE, 3, BUSCAR_PROPRIEDADES_POR_NOME, 4, BUSCAR_PROPRIEDADES_POR_CPF);

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    @Autowired
    private TemperaturaServico temperaturaServico;

    @Autowired
    private ControleMapaValidator controleMapaValidator;

    @InitBinder("ControleMapaCommand")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(controleMapaValidator);
    }

    @ModelAttribute("opcoesControleMapa")
    public Map<Integer, String> opcoesControleMapa() {
        return OPCOES_CONTROLE_MAPA;
    }

    @GetMapping("/visualizarMapa.html")
    public ModelAndView visualizarMapa(@ModelAttribute("ControleMapaCommand") @Validated ControleMapaCommand command, BindingResult erros) {
        ModelAndView mv = new ModelAndView("visualizarMapa");

        List<Propriedade> propriedadesEncontradas = new ArrayList<>();

        if (!erros.hasErrors() && command.getOpcaoSelecionada() != null) {
            String acao = OPCOES_CONTROLE_MAPA.get(command.getOpcaoSelecionada());

            if (acao.equalsIgnoreCase(BUSCAR_TODAS_PROPRIEDADES)) {
                propriedadesEncontradas = propriedadeServico.listarTodas();
            } else if (acao.equalsIgnoreCase(BUSCAR_PROPRIEDADE_MAIS_RECENTE)) {
                propriedadesEncontradas = List.of(propriedadeServico.buscarMaisRecente());
            } else if (acao.equalsIgnoreCase(BUSCAR_PROPRIEDADES_POR_NOME)) {
                propriedadesEncontradas = propriedadeServico.buscarPorNome(command.getNomeBusca());
            } else if (acao.equalsIgnoreCase(BUSCAR_PROPRIEDADES_POR_CPF)) {
                propriedadesEncontradas = propriedadeServico.buscarPorCpfDoProprietario(command.getCpfBusca());
            }
        } else {
            mv.addObject("ControleMapaCommand", command);
        }

        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        try {
            String estacoesJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(estacaoMeteorologicaServico.listarTodas());
            mv.addObject("estacoesJson", estacoesJson);

            String propriedadesJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(propriedadesEncontradas);
            mv.addObject("propriedadesJson", propriedadesJson);

        } catch (JsonProcessingException e) {
            mv.addObject("propriedadesJson", "[]");
            mv.addObject("estacoesJson", "[]");
        }

        return mv;
    }

//    @ResponseBody
//    @GetMapping(value = "/estacoesMeteorologicas", produces = MediaType.APPLICATION_JSON_VALUE)
//    @Transactional(readOnly = true)
//    public List<EstacaoMeteorologica> listarEstacoesMeteorologicas() {
//        return estacaoMeteorologicaServico.listarTodas();
//    }
//
//    @ResponseBody
//    @GetMapping(value = "/propriedades", produces = MediaType.APPLICATION_JSON_VALUE)
//    @Transactional(readOnly = true)
//    public List<Propriedade> buscarPropriedades(@RequestParam("opcaoSelecionada") Integer opcaoSelecionada, @RequestParam(value = "nomeBusca", required = false) String nomeBusca, @RequestParam(value = "cpfBusca", required = false) String cpfBusca) {
//        String opcao = OPCOES_CONTROLE_MAPA.get(opcaoSelecionada);
//
//        if (opcao.equalsIgnoreCase(BUSCAR_TODAS_PROPRIEDADES)) {
//            return propriedadeServico.listarTodas();
//        }
//
//        if (opcao.equalsIgnoreCase(BUSCAR_PROPRIEDADE_MAIS_RECENTE)) {
//            return List.of(propriedadeServico.buscarMaisRecente());
//        }
//
//        if (opcao.equalsIgnoreCase(BUSCAR_PROPRIEDADES_POR_NOME) && nomeBusca != null && !nomeBusca.trim().isEmpty()) {
//            return propriedadeServico.buscarPorNome(nomeBusca);
//        }
//
//        if (opcao.equalsIgnoreCase(BUSCAR_PROPRIEDADES_POR_CPF) && cpfBusca != null && !cpfBusca.trim().isEmpty()) {
//            return propriedadeServico.buscarPorCpfDoProprietario(cpfBusca);
//        }
//
//        return null;
//    }
    

    // produces
    // response body
    // previsao temperatura
    // web server

//    @ResponseBody
//    @GetMapping(value = "/previsaoTemperatura", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Temperatura calcularPrevisao(@ModelAttribute("PrevisaoTemperaturaCommand") @Validated PrevisaoTemperaturaCommand command, BindingResult errors) {
//        //return temperaturaServico.calcularPrevisao(command);
//    }

}
