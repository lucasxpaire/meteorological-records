package web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import servico.*;
import util.FormatadorUtil;

import java.time.LocalDateTime;

@Controller
public class InicioController {

    @Autowired
    private ProprietarioServico proprietarioServico;

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    @Autowired
    private RegistroMeteorologicoServico registroMeteorologicoServico;

    @GetMapping(value = { "/", "inicio.html" })
    public ModelAndView inicio() {
        ModelAndView mv = new ModelAndView("inicio");

        mv.addObject("totalProprietarios", proprietarioServico.listarTodos().size());
        mv.addObject("totalPropriedades", propriedadeServico.listarTodas().size());
        mv.addObject("totalEstacoes", estacaoMeteorologicaServico.listarTodas().size());

        try {
            mv.addObject("propriedadeMaisRecente", propriedadeServico.buscarMaisRecente());
        } catch (IllegalArgumentException e) {
            mv.addObject("propriedadeMaisRecente", null);
        }

        LocalDateTime ultimaAtualizacaoTemperaturas = registroMeteorologicoServico.obterUltimaAtualizacaoDeTemperaturas();
        LocalDateTime proximaAtualizacaoTemperaturas = registroMeteorologicoServico.obterProximaAtualizacaoDeTemperaturas();

        if (ultimaAtualizacaoTemperaturas == null) {
            mv.addObject("ultimaAtualizacaoTemperaturas", "Aguardando execução");
        } else {
            mv.addObject("ultimaAtualizacaoTemperaturas", ultimaAtualizacaoTemperaturas.format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO));
        }

        if (proximaAtualizacaoTemperaturas == null) {
            mv.addObject("proximaAtualizacaoTemperaturas", "Aguardando execução");
        } else {
            mv.addObject("proximaAtualizacaoTemperaturas", proximaAtualizacaoTemperaturas.format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO));
        }

        return mv;
    }

}
