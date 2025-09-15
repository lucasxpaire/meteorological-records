package web;

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

import java.util.List;

@Controller
@RequestMapping({"cadastroProprietario.html"})
public class CadastroProprietarioController {

    @Autowired
    private Dados dados;

    @Autowired
    private CadastroProprietarioValidator cadastroProprietarioValidator;

    @InitBinder("CadastroProprietarioCommand")
    void validator(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(cadastroProprietarioValidator);
    }

    @ModelAttribute("cores")
    public List<Cor> todasAsCores() {
        return dados.listarTodos(Cor.class);
    }

    @GetMapping
    public ModelAndView exibirCadastro(@ModelAttribute CadastroProprietarioCommand cadastroProprietarioCommand, Long id) {
        ModelAndView mv = new ModelAndView("cadastroProprietario");

        if (id != null) {
            Proprietario proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", id);
            mv.addObject("proprietario", proprietario);
            cadastroProprietarioCommand.setProprietario(proprietario);
        }
        mv.addObject("CadastroProprietarioCommand", cadastroProprietarioCommand);
        return mv;
    }

    @PostMapping
    public ModelAndView salvar(@ModelAttribute("CadastroProprietarioCommand") @Validated CadastroProprietarioCommand cadastroProprietarioCommand, BindingResult errors) {
        ModelAndView mv = new ModelAndView("cadastroProprietario");

        if (errors.hasErrors()) {
            if (cadastroProprietarioCommand.getId() != null) {
                mv.addObject("proprietario", dados.buscarUnicoPorCampo(Proprietario.class, "id", cadastroProprietarioCommand.getId()));
            }
            return mv;
        }

        if (cadastroProprietarioCommand.getCor() != null && cadastroProprietarioCommand.getCor().getId() != null) {
            Cor corSelecionada = dados.buscarUnicoPorCampo(Cor.class, "id", cadastroProprietarioCommand.getCor().getId());
            cadastroProprietarioCommand.setCor(corSelecionada);
        }

        Proprietario proprietario;
        if (cadastroProprietarioCommand.getId() != null) {
            proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", cadastroProprietarioCommand.getId());
            proprietario.setNome(cadastroProprietarioCommand.getNome());
            proprietario.setCpf(cadastroProprietarioCommand.getCpf());
            proprietario.setTelefone(cadastroProprietarioCommand.getTelefone());
            proprietario.setCor(cadastroProprietarioCommand.getCor());
            dados.salvar(proprietario);
            mv.addObject("resultado", "Proprietário editado com sucesso");
        } else {
            proprietario = cadastroProprietarioCommand.getProprietario();
            dados.salvar(proprietario);
            mv.addObject("resultado", "Proprietário cadastrado com sucesso");
        }

        mv.addObject("proprietario", proprietario);
        mv.addObject("CadastroProprietarioCommand", cadastroProprietarioCommand);
        return mv;
    }

}
