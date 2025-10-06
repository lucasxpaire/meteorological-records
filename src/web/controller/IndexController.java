package web.controller;

import modelo.Propriedade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import servico.EstacaoMeteorologicaServico;
import servico.PropriedadeServico;
import servico.ProprietarioServico;

@Controller
@RequestMapping("/index.html")
public class IndexController {

    @Autowired
    private ProprietarioServico proprietarioServico;

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private EstacaoMeteorologicaServico estacaoServico;


    @GetMapping
    public String index(Model model) {

        model.addAttribute("totalProprietarios", proprietarioServico.listarTodos().size());
        model.addAttribute("totalPropriedades", propriedadeServico.listarTodas().size());
        model.addAttribute("totalEstacoes", estacaoServico.listarTodas().size());

        try {
            Propriedade maisRecente = propriedadeServico.buscarMaisRecente();
            model.addAttribute("propriedadeMaisRecente", maisRecente);
        } catch (IllegalArgumentException e) {
            model.addAttribute("propriedadeMaisRecente", null);
        }

        return "index";
    }

}
