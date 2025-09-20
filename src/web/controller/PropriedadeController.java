package web.controller;

import dados.Dados;
import modelo.Propriedade;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import servico.PropriedadeServico;
import servico.ProprietarioServico;
import web.validator.PropriedadeValidator;

import java.util.List;

@Controller
public class PropriedadeController {

    @Autowired
    private Dados dados;

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private PropriedadeValidator propriedadeValidator;

    @InitBinder("PropriedadeCommand")
    void validator (WebDataBinder webDataBinder) {
        webDataBinder.addValidators(propriedadeValidator);
    }

    public ModelAndView listarPropriedadesPorProprietario(@RequestParam("proprietarioId") Long proprietarioId) {
        Proprietario proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", proprietarioId);

        ModelAndView mv = new ModelAndView("gerenciarPropriedadesPorProprietario");

        mv.addObject("proprietario", proprietario);
        mv.addObject("propriedades", proprietario.getPropriedades());
        return mv;
    }


}
