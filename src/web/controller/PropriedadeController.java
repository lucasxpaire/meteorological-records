package web.controller;

import modelo.Propriedade;
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

import servico.PontoServico;
import servico.PropriedadeServico;
import util.FormatadorUtil;
import util.PoligonoUtil;
import web.CoordenadasMultiPartFile;
import web.command.PropriedadeCommand;
import web.validator.PropriedadeValidator;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@Controller
public class PropriedadeController {

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private PropriedadeValidator propriedadeValidator;

    @Autowired
    private PontoServico pontoServico;

    @InitBinder("PropriedadeCommand")
    void validator (WebDataBinder webDataBinder) {
        webDataBinder.addValidators(propriedadeValidator);
    }

    @GetMapping(value = {"/cadastroPropriedade.html", "/alterarPropriedade.html"})
    public ModelAndView exibirFormulario(@RequestParam(value = "idPropriedade", required = false) Long idPropriedade) {
        ModelAndView mv = new ModelAndView("cadastroPropriedade");
        PropriedadeCommand command = new PropriedadeCommand();
        Proprietario proprietario = null;

        if (idPropriedade != null) {
            Propriedade propriedade = propriedadeServico.buscarPorId(idPropriedade);
            command.setPropriedade(propriedade);
            proprietario = propriedade.getProprietario();
            mv.addObject("propriedade", propriedade);

            String tipoEntrada = propriedade.getTipoEntradaPoligono();
            command.setTipoEntradaPoligono(tipoEntrada);

            if (tipoEntrada.equals(PoligonoUtil.TIPO_MANUAL)) {
                command.setCoordenadasPorInsercaoManual(new String(propriedade.getArquivoPoligonos()));
            } else if (tipoEntrada.equals(PoligonoUtil.TIPO_ARQUIVO)){
                command.setCoordenadasPorArquivo(new CoordenadasMultiPartFile(Arrays.toString(propriedade.getArquivoPoligonos()), "coordenadas", "coordenadas.txt", "text/plain"));
            }
        }

        if (proprietario != null) {
            command.setIdProprietario(proprietario.getId());
            command.setCpfProprietario(FormatadorUtil.formatarCpf(proprietario.getCpf()));
            mv.addObject("proprietario", proprietario);
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
    public String salvar(@ModelAttribute("PropriedadeCommand") @Validated PropriedadeCommand command, BindingResult errors, Model model, RedirectAttributes redirectAttributes) throws IOException {
        if (errors.hasErrors()) {
            if (command.getId() != null) {
                model.addAttribute("propriedade", propriedadeServico.buscarPorId(command.getId()));
            }
            return "cadastroPropriedade";
        }

        if (command.getId() != null) {
            redirectAttributes.addFlashAttribute("sucesso", "Propriedade atualizada com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("sucesso", "Propriedade cadastrada com sucesso!");
        }

        Propriedade propriedade = propriedadeServico.prepapararPropriedade(command);
        pontoServico.calcularEAdicionarTemperaturaAtual(propriedade.getCentroide());

        propriedadeServico.salvar(propriedade);

        return "redirect:/gerenciarPropriedades.html";
    }

    @GetMapping("/deletarPropriedade.html")
    public String deletar(@RequestParam(value = "idPropriedade") Long idPropriedade, RedirectAttributes redirectAttributes) {
        Propriedade propriedade = propriedadeServico.buscarPorId(idPropriedade);
        propriedadeServico.deletar(propriedade);

        redirectAttributes.addFlashAttribute("sucesso", "Propriedade deletada com sucesso");
        return "redirect:/gerenciarPropriedades.html";
    }

}
