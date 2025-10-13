package web.controller;

import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import servico.CorServico;
import servico.ProprietarioServico;
import web.validator.ProprietarioValidator;
import web.command.ProprietarioCommand;

import java.util.List;

@Controller
public class ProprietarioController {

    @Autowired
    private ProprietarioServico proprietarioServico;

    @Autowired
    private CorServico corServico;

    @Autowired
    private ProprietarioValidator proprietarioValidator;

    @InitBinder("ProprietarioCommand")
    void validator(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(proprietarioValidator);
    }

    @GetMapping(value = {"/cadastroProprietario.html", "/alterarProprietario.html"})
    public ModelAndView exibirFormulario(@RequestParam(value = "idProprietario", required = false) Long idProprietario) {
        ModelAndView mv = new ModelAndView("cadastroProprietario");
        ProprietarioCommand command = new ProprietarioCommand();

        if (idProprietario != null) {
            Proprietario proprietario = proprietarioServico.buscarPorId(idProprietario);
            command.setProprietario(proprietario);
            command.setCorId(proprietario.getCor().getId());
            mv.addObject("proprietario", proprietario);
        }

        mv.addObject("cores", corServico.listarTodos());

        mv.addObject("ProprietarioCommand", command);
        return mv;
    }

    @GetMapping("/gerenciarProprietarios.html")
    public ModelAndView listarTodosOuBuscar(@RequestParam(value = "busca", required = false) String busca) {
        ModelAndView mv = new ModelAndView("gerenciarProprietarios");
        List<Proprietario> proprietarios;
        if (busca == null || busca.trim().isEmpty()) {
            proprietarios = proprietarioServico.listarTodos();
        } else {
            proprietarios = proprietarioServico.buscarPorCpfOuNome(busca);
        }

        mv.addObject("proprietarios", proprietarios);
        return mv;
    }

    @PostMapping("/cadastroProprietario.html")
    public String salvar(@ModelAttribute("ProprietarioCommand") @Validated ProprietarioCommand command, BindingResult errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            if (command.getId() != null) {
                model.addAttribute("proprietario", proprietarioServico.buscarPorId(command.getId()));
            }
            return "cadastroProprietario";
        }

        if (command.getId() != null) {
            redirectAttributes.addFlashAttribute("sucesso", "Proprietário atualizado com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("sucesso", "Proprietário cadastrado com sucesso!");
        }

        Proprietario proprietario = proprietarioServico.prepararProprietario(command);
        proprietarioServico.salvar(proprietario);

        return "redirect:/gerenciarProprietarios.html";
    }

    @GetMapping("/deletarProprietario.html")
    public String deletar(@RequestParam("idProprietario") Long idProprietario, RedirectAttributes redirectAttributes) {
        Proprietario proprietario = proprietarioServico.buscarPorId(idProprietario);
        if (proprietario.getPropriedades().isEmpty()) {
            proprietarioServico.deletar(proprietario);
            redirectAttributes.addFlashAttribute("sucesso", "Proprietário deletado com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("falha", "Falha: Não é possível deletar proprietário com propriedades existentes!");
        }
        return "redirect:/gerenciarProprietarios.html";
    }

}
