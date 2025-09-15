package web;

import dados.Dados;
import modelo.Cor;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import servico.ProprietarioServico;

import java.util.List;

@Controller
public class ProprietarioController {

    @Autowired
    private Dados dados;

    @Autowired
    private ProprietarioServico proprietarioServico;

    @Autowired
    private ProprietarioValidator proprietarioValidator;

    @InitBinder("CadastroProprietarioCommand")
    void validator(org.springframework.web.bind.WebDataBinder webDataBinder) {
        webDataBinder.addValidators(proprietarioValidator);
    }

    @ModelAttribute("cores")
    public List<Cor> todasAsCores() {
        return dados.listarTodos(Cor.class);
    }

    @GetMapping("/gerenciarProprietarios.html")
    public ModelAndView listar() {
        List<Proprietario> proprietarios = proprietarioServico.listarTodos();
        ModelAndView mv = new ModelAndView("gerenciarProprietarios");
        mv.addObject("proprietarios", proprietarios);
        return mv;
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

        mv.addObject("CadastroProprietarioCommand", command);
        return mv;
    }

    @PostMapping("/cadastroProprietario.html")
    public ModelAndView salvar(@ModelAttribute("CadastroProprietarioCommand") @Validated ProprietarioCommand command, BindingResult errors) {
        ModelAndView mv = new ModelAndView("cadastroProprietario");
        if (errors.hasErrors()) {
            if (command.getId() != null) {
                mv.addObject("proprietario", dados.buscarUnicoPorCampo(Proprietario.class, "id", command.getId()));
            }
            return mv;
        }

        if (command.getCor() != null && command.getCor().getId() != null) {
            Cor corSelecionada = dados.buscarUnicoPorCampo(Cor.class, "id", command.getCor().getId());
            command.setCor(corSelecionada);
        }

        Proprietario proprietario;
        if (command.getId() != null) {
            proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", command.getId());
            proprietario.setNome(command.getNome());
            proprietario.setCpf(command.getCpf());
            proprietario.setTelefone(command.getTelefone());
            proprietario.setCor(command.getCor());
            dados.salvar(proprietario);
            mv.addObject("resultado", "Proprietário atualizado com sucesso!");
        } else {
            proprietario = command.getProprietario();
            dados.salvar(proprietario);
            mv.addObject("resultado", "Proprietário cadastrado com sucesso!");
        }

        mv.addObject("proprietario", proprietario);
        mv.addObject("CadastroProprietarioCommand", command);
        return mv;
    }

    @GetMapping("/deletarProprietario.html")
    public String deletar(@RequestParam("id") Long id) {
        Proprietario proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", id);
        dados.deletar(proprietario);
        return "redirect:/gerenciarProprietarios.html";
    }

}
