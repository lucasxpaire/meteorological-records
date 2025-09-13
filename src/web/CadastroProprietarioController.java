package web;

import dados.Dados;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping({"cadastroProprietario"})
public class CadastroProprietarioController {

    @Autowired
    private Dados dados;

    @Autowired
    private CadastroProprietarioValidator cadastroProprietarioValidator;

    @InitBinder("CadastroProprietarioCommand")
    void validator(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(cadastroProprietarioValidator);
    }

    @GetMapping
    public ModelAndView exibirCadastro(@ModelAttribute CadastroProprietarioCommand cadastroProprietarioCommand, Long id) {
        ModelAndView mv = new ModelAndView("cadastroProprietario");
        if (id != null) {
            Proprietario proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "ID_PROPRIETARIO", id);
            mv.addObject("proprietario", proprietario);
            cadastroProprietarioCommand.setProprietario(proprietario);
        }
        mv.addObject("CadastroProprietarioCommand", cadastroProprietarioCommand);
        return mv;
    }

    @PostMapping
    public ModelAndView salvar(@ModelAttribute("CadastroProprietarioCommand") @Validated CadastroProprietarioCommand cadastroProprietarioCommand, BindingResult errors, Long id) {
        ModelAndView mv = new ModelAndView("cadastroProprietario");
        if (errors.hasErrors()) {
            if (cadastroProprietarioCommand.getId() != null) {
                mv.addObject("proprietario", dados.buscarUnicoPorCampo(Proprietario.class, "ID_PROPRIETARIO", cadastroProprietarioCommand.getId()));
            }
            return mv;
        }

        Proprietario proprietario;
        if (id != null) {
            proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "ID_PROPRIETARIO", cadastroProprietarioCommand.getId());
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
        cadastroProprietarioCommand.setProprietario(proprietario);
        mv.addObject("CadastroProprietarioCommmand", cadastroProprietarioCommand);
        return mv;
    }

}
