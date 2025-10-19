package web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import modelo.Ponto;
import modelo.Propriedade;
import modelo.Temperatura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import servico.EstacaoMeteorologicaServico;
import servico.PontoServico;
import servico.PropriedadeServico;
import servico.TemperaturaServico;
import util.JsonUtil;
import web.command.ControleMapaCommand;
import web.validator.ControleMapaValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class MapaController {

    public static final String BUSCAR_TODAS_PROPRIEDADES = "Todas as propriedades";
    public static final String BUSCAR_PROPRIEDADE_MAIS_RECENTE = "Propriedade mais recente";
    public static final String BUSCAR_PROPRIEDADES_POR_NOME = "Buscar por nome";
    public static final String BUSCAR_PROPRIEDADES_POR_CPF = "Buscar por CPF";

    public static final Map<Integer, String> OPCOES_CONTROLE_MAPA = Map.of(1, BUSCAR_TODAS_PROPRIEDADES, 2, BUSCAR_PROPRIEDADE_MAIS_RECENTE, 3, BUSCAR_PROPRIEDADES_POR_NOME, 4, BUSCAR_PROPRIEDADES_POR_CPF);

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    @Autowired
    private TemperaturaServico temperaturaServico;

    @Autowired
    private PontoServico pontoServico;

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
        mv.addObject("exibirControleMapa", true);

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

        try {
            String estacoesJson = JsonUtil.converterObjetoParaJson(estacaoMeteorologicaServico.listarTodas());
            mv.addObject("estacoesJson", estacoesJson);

            String propriedadesJson = JsonUtil.converterObjetoParaJson(propriedadesEncontradas);
            mv.addObject("propriedadesJson", propriedadesJson);

        } catch (JsonProcessingException e) {
            mv.addObject("propriedadesJson", "[]");
            mv.addObject("estacoesJson", "[]");
        }

        return mv;
    }

    @GetMapping("/visualizarPropriedade.html")
    public ModelAndView visualizarPropriedade(@RequestParam("idPropriedade") Long idPropriedade) {
        ModelAndView mv = new ModelAndView("visualizarMapa");

        mv.addObject("exibirControleMapa", false);

        if (idPropriedade != null) {
            try {
                mv.addObject("estacoesJson", JsonUtil.converterObjetoParaJson(estacaoMeteorologicaServico.listarTodas()));
                mv.addObject("propriedadesJson", JsonUtil.converterObjetoParaJson(List.of(propriedadeServico.buscarPorId(idPropriedade))));
            } catch (JsonProcessingException e) {
                mv.addObject("propriedadesJson", "[]");
                mv.addObject("estacoesJson", "[]");
            }
        }

        return mv;
    }

    @ResponseBody
    @GetMapping(value = "/preverTemperatura", produces = MediaType.APPLICATION_JSON_VALUE)
    public Temperatura calcularPrevisao(@RequestParam("idCentroide") Long idCentroide) {
        Ponto centroide = pontoServico.buscarPorId(idCentroide);

        LocalDateTime dataHoraAgora = LocalDateTime.now();
        LocalDateTime dataHoraPrevista = dataHoraAgora.plusHours(1).withMinute(0).withSecond(0);
        return temperaturaServico.preverTemperaturaParaPonto(centroide, dataHoraPrevista);
    }

}
