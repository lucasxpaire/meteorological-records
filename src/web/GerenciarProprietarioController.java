package web;

import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import servico.ProprietarioServico;

import java.util.List;

@Controller
public class GerenciarProprietarioController {

    @Autowired
    private ProprietarioServico proprietarioServico;

    @GetMapping("/gerenciarProprietarios.html")
    public ModelAndView listarProprietarios() {
        List<Proprietario> proprietarios = proprietarioServico.listarTodos();

        ModelAndView mv = new ModelAndView("gerenciarProprietarios");
        mv.addObject("proprietarios", proprietarios);
        return mv;
    }


}
