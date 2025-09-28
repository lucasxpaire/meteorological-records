package web.controller;

import dados.Dados;
import modelo.Cor;
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
import servico.ProprietarioServico;
import web.validator.ProprietarioValidator;
import web.command.ProprietarioCommand;

import java.util.List;

@Controller
public class ProprietarioController {

    @Autowired
    private Dados dados;

    @Autowired
    private ProprietarioServico proprietarioServico;

    @Autowired
    private ProprietarioValidator proprietarioValidator;

    @InitBinder("ProprietarioCommand")
    void validator(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(proprietarioValidator);
    }

    @ModelAttribute("cores")
    public List<Cor> todasAsCores() {
        return dados.listarTodos(Cor.class);
    }

    @GetMapping(value = {"/cadastroProprietario.html", "/alterarProprietario.html"})
    public ModelAndView exibirFormulario(@RequestParam(value = "idProprietario", required = false) Long idProprietario) {
        ModelAndView mv = new ModelAndView("cadastroProprietario");
        ProprietarioCommand command = new ProprietarioCommand();

        if (idProprietario != null) {
            Proprietario proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", idProprietario);
            command.setProprietario(proprietario);
            mv.addObject("proprietario", proprietario);
        }

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
                model.addAttribute("proprietario", dados.buscarUnicoPorCampo(Proprietario.class, "id", command.getId()));
            }
            return "cadastroProprietario";
        }

        Proprietario proprietario;
        if (command.getId() != null) {
            proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", command.getId());
            redirectAttributes.addFlashAttribute("resultado", "Proprietário atualizado com sucesso!");
        } else {
            proprietario = command.getProprietario();
            redirectAttributes.addFlashAttribute("resultado", "Proprietário cadastrado com sucesso!");
        }

        proprietario.setNome(command.getNome());
        proprietario.setCpf(command.getCpf());
        proprietario.setTelefone(command.getTelefone());
        proprietario.setCor(dados.buscarUnicoPorCampo(Cor.class, "id", command.getCorId()));

        dados.salvar(proprietario);

        return "redirect:/gerenciarProprietarios.html";
    }


    // Falta: adicionar redirect pra exclusão de propriedades se existerem
    @GetMapping("/deletarProprietario.html")
    public String deletar(@RequestParam("idProprietario") Long idProprietario, RedirectAttributes redirectAttributes) {
        Proprietario proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", idProprietario);
        if (proprietario.getPropriedades().isEmpty()) {
            dados.deletar(proprietario);
            redirectAttributes.addFlashAttribute("resultado", "Proprietário deletado com sucesso!");
            return "redirect:/gerenciarProprietarios.html";
        } else {
            redirectAttributes.addFlashAttribute("resultado", "Falha: Não é possível deletar proprietário com propriedades existentes!");
            return  "redirect:/gerenciarProprietarios.html";
        }
    }

}
