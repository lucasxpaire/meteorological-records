package web.controller;

import dados.Dados;
import modelo.Poligono;
import modelo.Propriedade;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import servico.PropriedadeServico;
import util.FormatadorUtil;
import util.PoligonoUtil;
import web.StringMultipartFile;
import web.command.PropriedadeCommand;
import web.validator.PropriedadeValidator;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@Controller
public class PropriedadeController {

    @Autowired
    private Dados dados;

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private PropriedadeValidator propriedadeValidator;

    @InitBinder("PropriedadeCommand")
    void validator (WebDataBinder webDataBinder) {
        webDataBinder.addValidators(propriedadeValidator);
    }

    @GetMapping(value = {"/cadastroPropriedade.html", "/alterarPropriedade.html"})
    public ModelAndView exibirFormulario(@RequestParam(value = "idPropriedade", required = false) Long idPropriedade, @RequestParam(value = "idProprietario", required = false) Long idProprietario) {
        ModelAndView mv = new ModelAndView("cadastroPropriedade");
        PropriedadeCommand command = new PropriedadeCommand();
        Proprietario proprietario = null;

        if (idPropriedade != null) {
            Propriedade propriedade = dados.buscarUnicoPorCampo(Propriedade.class, "id", idPropriedade);
            command.setPropriedade(propriedade);
            proprietario = propriedade.getProprietario();
            mv.addObject("propriedade", propriedade);

            String tipoEntrada = propriedade.getTipoEntradaPoligono();
            command.setTipoEntradaPoligono(tipoEntrada);

            if (tipoEntrada.equals(PoligonoUtil.TIPO_MANUAL)) {
                command.setCoordenadasPorInsercaoManual(new String(propriedade.getArquivoPoligonos()));
            } else if (tipoEntrada.equals(PoligonoUtil.TIPO_ARQUIVO)){
                command.setCoordenadasPorArquivo(new StringMultipartFile(Arrays.toString(propriedade.getArquivoPoligonos()), "coordenadas", "coordenadas.txt", "text/plain"));
            }
        } else if (idProprietario != null) {
            proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", idProprietario);

        }

        if (proprietario != null) {
            command.setIdProprietario(proprietario.getId());
            command.setCpfProprietario(FormatadorUtil.formatarCpf(proprietario.getCpf()));
            mv.addObject("proprietario", proprietario);
        }

        if (idProprietario != null) {
            command.setPaginaOrigemRequisicao("gerenciarPropriedadesDoProprietario");
        } else {
            command.setPaginaOrigemRequisicao("gerenciarPropriedades");
        }

        mv.addObject("PropriedadeCommand", command);
        return mv;
    }

    @GetMapping("/gerenciarPropriedadesDoProprietario.html")
    public ModelAndView listarPropriedadesDoProprietario(@RequestParam(value = "idProprietario") Long idProprietario, @RequestParam(value = "busca", required = false) String busca) {
        ModelAndView mv = new ModelAndView("gerenciarPropriedadesDoProprietario");

        Proprietario proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", idProprietario);
        if (busca != null && !busca.trim().isEmpty()) {
            List<Propriedade> propriedadesFiltradas = propriedadeServico.buscarPorNome(busca)
                    .stream()
                    .filter(p -> p.getProprietario().getId().equals(idProprietario))
                    .toList();
            mv.addObject("propriedades", propriedadesFiltradas);
            mv.addObject("proprietario", proprietario);
            return mv;
        } else {
            List<Propriedade> propriedades = dados.buscarListaPorCampo(Propriedade.class, "proprietario.id", idProprietario);
            mv.addObject("propriedades", propriedades);
            mv.addObject("proprietario", proprietario);
            return mv;
        }
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
                model.addAttribute("propriedade", dados.buscarUnicoPorCampo(Propriedade.class, "id", command.getId()));
            }
            return "cadastroPropriedade";
        }

        Propriedade propriedade;
        if (command.getId() != null) {
            propriedade = dados.buscarUnicoPorCampo(Propriedade.class, "id", command.getId());
            redirectAttributes.addFlashAttribute("resultado", "Propriedade atualizada com sucesso!");
        } else {
            propriedade = command.getPropriedade();
            redirectAttributes.addFlashAttribute("resultado", "Propriedade cadastrada com sucesso!");
        }

        MultipartFile arquivoRecebido = null;
        if (PoligonoUtil.TIPO_MANUAL.equals(command.getTipoEntradaPoligono())) {
            String textoCoordenadas = command.getCoordenadasPorInsercaoManual();
            arquivoRecebido = new StringMultipartFile(textoCoordenadas, "coordenadas", "coordenadas.txt", "text/plain");
        } else if (PoligonoUtil.TIPO_ARQUIVO.equals(command.getTipoEntradaPoligono())) {
            arquivoRecebido = command.getCoordenadasPorArquivo();
        }

        Poligono poligono = PoligonoUtil.criarPoligonoPorArquivo(arquivoRecebido);

        propriedade.setTipoEntradaPoligono(command.getTipoEntradaPoligono());
        propriedade.setArquivoPoligonos(arquivoRecebido.getBytes());
        propriedade.setNome(command.getNome());
        propriedade.setPoligono(poligono);
        propriedade.setCentroide(poligono.calcularCentroide());

        if (command.getIdProprietario() != null) {
            propriedade.setProprietario(dados.buscarUnicoPorCampo(Proprietario.class, "id", command.getIdProprietario()));
        } else if (command.getCpfProprietario() != null) {
            propriedade.setProprietario(dados.buscarUnicoPorCampo(Proprietario.class, "cpf", FormatadorUtil.removerFormatacaoCpf(command.getCpfProprietario())));
        }

        propriedadeServico.salvar(propriedade);

        if (command.getPaginaOrigemRequisicao().equals("gerenciarPropriedadesDoProprietario")) {
            redirectAttributes.addAttribute("idProprietario", command.getIdProprietario());
            return "redirect:/gerenciarPropriedadesDoProprietario.html";
        } else {
            return "redirect:/gerenciarPropriedades.html";
        }
    }

    @GetMapping("/deletarPropriedade.html")
    public String deletar(@RequestParam(value = "idPropriedade") Long idPropriedade, @RequestParam(value = "paginaOrigemRequisicao", defaultValue = "gerenciarPropriedades") String paginaOrigemRequisicao, RedirectAttributes redirectAttributes) {
        Propriedade propriedade = dados.buscarUnicoPorCampo(Propriedade.class, "id", idPropriedade);
        dados.deletar(propriedade);

        if (paginaOrigemRequisicao.equals("gerenciarPropriedadesDoProprietario")) {
            redirectAttributes.addAttribute("idProprietario", propriedade.getProprietario().getId());
            redirectAttributes.addFlashAttribute("resultado", "Propriedade deletada com sucesso");
            return "redirect:/gerenciarPropriedadesDoProprietario.html";
        } else {
            return "redirect:/gerenciarPropriedades.html";
        }
    }

}
