package web.controller;

import modelo.Propriedade;
import modelo.RegistroMeteorologico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import servico.PontoServico;
import servico.PropriedadeServico;
import servico.RegistroMeteorologicoServico;
import util.FormatadorUtil;
import web.command.PropriedadeCommand;
import web.validator.PropriedadeValidator;

import java.util.List;

@Controller
public class PropriedadeController {

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private PontoServico pontoServico;

    @Autowired
    private RegistroMeteorologicoServico registroMeteorologicoServico;

    @Autowired
    private PropriedadeValidator propriedadeValidator;

    @InitBinder("PropriedadeCommand")
    void validator (WebDataBinder webDataBinder) {
        webDataBinder.addValidators(propriedadeValidator);
    }

    @GetMapping(value = {"/cadastroPropriedade.html", "/alterarPropriedade.html"})
    public ModelAndView exibirFormulario(@RequestParam(value = "idPropriedade", required = false) Long idPropriedade) {
        ModelAndView mv = new ModelAndView("cadastroPropriedade");
        PropriedadeCommand command = new PropriedadeCommand();

        if (idPropriedade != null) {
            Propriedade propriedade = propriedadeServico.buscarPorId(idPropriedade);
            command.setPropriedade(propriedade);
            command.setNomeArquivoPontos(propriedade.getArquivoPontos().getNomeOriginal());
            command.setPontos(new String(propriedade.getArquivoPontos().getConteudo()));
            command.setCpfProprietario(FormatadorUtil.formatarCpf(propriedade.getProprietario().getCpf()));

            mv.addObject("proprietario", propriedade.getProprietario());
            mv.addObject("propriedade", propriedade);
        }

        mv.addObject("PropriedadeCommand", command);
        return mv;
    }

    @GetMapping("/gerenciarPropriedades.html")
    public ModelAndView listarTodasOuBuscar(@RequestParam(value = "busca", required = false) String busca) {
        ModelAndView mv = new ModelAndView("gerenciarPropriedades");
        List<Propriedade> propriedades;
        if (busca == null || busca.trim().isEmpty()) {
            propriedades = propriedadeServico.listarTodas();
        } else {
            propriedades = propriedadeServico.buscarPorNome(busca);
        }

        mv.addObject("propriedades", propriedades);
        return mv;
    }

    @PostMapping("/cadastroPropriedade.html")
    public String salvar(@ModelAttribute("PropriedadeCommand") @Validated PropriedadeCommand command, BindingResult errors, Model model) {
        if (errors.hasErrors()) {
            if (command.getId() != null) {
                model.addAttribute("propriedade", propriedadeServico.buscarPorId(command.getId()));
            }
            return "cadastroPropriedade";
        }

        Propriedade propriedade = propriedadeServico.prepararPropriedade(command);
        RegistroMeteorologico temperaturaCalculada = registroMeteorologicoServico.calcularTemperatura(propriedade.getCentroide());
        propriedade.getCentroide().getHistoricoTemperaturas().add(temperaturaCalculada);

        propriedadeServico.salvar(propriedade);

        if (command.getId() != null) {
            model.addAttribute("sucesso", "Propriedade atualizada com sucesso!");
            model.addAttribute("propriedade", propriedade);
        } else {
            model.addAttribute("sucesso", "Propriedade cadastrada com sucesso!");
        }

        return "cadastroPropriedade";
    }

    @GetMapping("/deletarPropriedade.html")
    public String deletar(@RequestParam(value = "idPropriedade") Long idPropriedade, RedirectAttributes redirectAttributes) {
        Propriedade propriedade = propriedadeServico.buscarPorId(idPropriedade);
        try {
            propriedadeServico.deletar(propriedade);
            redirectAttributes.addFlashAttribute("sucesso", "Propriedade deletada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("falha", "Falha: Não foi possível deletar a propriedade.");
        }

        return "redirect:/gerenciarPropriedades.html";
    }

}
