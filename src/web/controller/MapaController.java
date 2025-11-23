package web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import modelo.Propriedade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import servico.*;
import util.FormatadorUtil;
import web.command.ControleMapaCommand;
import web.validator.ControleMapaValidator;

import java.util.ArrayList;
import java.util.Collections;
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
    private ControleMapaValidator controleMapaValidator;

    @InitBinder("ControleMapaCommand")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(controleMapaValidator);
    }

    @GetMapping("/visualizarMapa.html")
    public ModelAndView visualizarMapa(@ModelAttribute("ControleMapaCommand") @Validated ControleMapaCommand command, BindingResult erros) {
        ModelAndView mv = new ModelAndView("mapa");

        List<Propriedade> propriedadesEncontradas = new ArrayList<>();

        if (!erros.hasErrors() && command.getOpcaoSelecionada() != null) {
            try {
                switch (OPCOES_CONTROLE_MAPA.get(command.getOpcaoSelecionada())) {
                    case BUSCAR_TODAS_PROPRIEDADES -> propriedadesEncontradas = propriedadeServico.listarTodas();
                    case BUSCAR_PROPRIEDADE_MAIS_RECENTE -> propriedadesEncontradas = Collections.singletonList(propriedadeServico.buscarMaisRecente());
                    case BUSCAR_PROPRIEDADES_POR_NOME -> propriedadesEncontradas = propriedadeServico.buscarPorNome(command.getNomeBusca());
                    case BUSCAR_PROPRIEDADES_POR_CPF -> propriedadesEncontradas = propriedadeServico.buscarPorCpfDoProprietario(command.getCpfBusca());
                }
            } catch (Exception e) {
                propriedadesEncontradas = new ArrayList<>();
            }
        } else {
            mv.addObject("ControleMapaCommand", command);
        }

        try {
            String estacoesJson = FormatadorUtil.converterObjetoParaJson(estacaoMeteorologicaServico.listarTodas());
            mv.addObject("estacoesJson", estacoesJson);

            String propriedadesJson = FormatadorUtil.converterObjetoParaJson(propriedadesEncontradas);
            mv.addObject("propriedadesJson", propriedadesJson);

        } catch (JsonProcessingException e) {
            mv.addObject("propriedadesJson", "[]");
            mv.addObject("estacoesJson", "[]");
        }

        mv.addObject("opcoesControleMapa", OPCOES_CONTROLE_MAPA);

        return mv;
    }

    @GetMapping("/visualizarPropriedade.html")
    public ModelAndView visualizarPropriedade(@ModelAttribute("ControleMapaCommand") @Validated ControleMapaCommand command, @RequestParam("idPropriedade") Long idPropriedade) {
        ModelAndView mv = new ModelAndView("mapa");

        if (idPropriedade != null) {
            try {
                mv.addObject("estacoesJson", FormatadorUtil.converterObjetoParaJson(estacaoMeteorologicaServico.listarTodas()));
                mv.addObject("propriedadesJson", FormatadorUtil.converterObjetoParaJson(List.of(propriedadeServico.buscarPorId(idPropriedade))));
            } catch (JsonProcessingException e) {
                mv.addObject("propriedadesJson", "[]");
                mv.addObject("estacoesJson", "[]");
            }
        }

        mv.addObject("ControleMapaCommand", command);
        mv.addObject("opcoesControleMapa", OPCOES_CONTROLE_MAPA);

        return mv;
    }

}
