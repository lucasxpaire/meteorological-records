package web.controller;

import dados.Dados;
import modelo.Cor;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
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
    public ModelAndView exibirFormulario(@RequestParam(value = "id", required = false) Long id) {
        ModelAndView mv = new ModelAndView("cadastroProprietario");
        ProprietarioCommand command = new ProprietarioCommand();

        if (id != null) {
            Proprietario proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", id);
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
    public ModelAndView salvar(@ModelAttribute("ProprietarioCommand") @Validated ProprietarioCommand command, BindingResult erros) {
        ModelAndView mv = new ModelAndView("cadastroProprietario");

        if (erros.hasErrors()) {
            if (command.getId() != null) {
                mv.addObject("proprietario", dados.buscarUnicoPorCampo(Proprietario.class, "id", command.getId()));
            }
            return mv;
        }

        Proprietario proprietario;
        if (command.getId() != null) {
            proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", command.getId());
            mv.addObject("resultado", "Proprietário atualizado com sucesso!");

        } else {
            proprietario = command.getProprietario();
            mv.addObject("resultado", "Proprietário cadastrado com sucesso!");
        }

        proprietario.setNome(command.getNome());
        proprietario.setCpf(command.getCpf());
        proprietario.setTelefone(command.getTelefone());

        if (command.getCorId() != null) {
            Cor corSelecionada = dados.buscarUnicoPorCampo(Cor.class, "id", command.getCorId());
            proprietario.setCor(corSelecionada);
        }

        dados.salvar(proprietario);

        mv.addObject("proprietario", proprietario);
        mv.addObject("ProprietarioCommand", command);

        return mv;
    }

    @GetMapping("/deletarProprietario.html")
    public String deletar(@RequestParam("id") Long id) {
        Proprietario proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", id);
        dados.deletar(proprietario);
        return "redirect:/gerenciarProprietarios.html";
    }

}
