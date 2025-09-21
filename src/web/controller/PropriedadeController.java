package web.controller;

import dados.Dados;
import modelo.Poligono;
import modelo.Ponto;
import modelo.Propriedade;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import servico.PropriedadeServico;
import util.FormatadorUtil;
import web.command.PropriedadeCommand;
import web.validator.PropriedadeValidator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
    public ModelAndView exibirFormulario(@RequestParam(value = "propriedadeId", required = false) Long propriedadeId, @RequestParam(value = "proprietarioId", required = false) Long proprietarioId) {
        ModelAndView mv = new ModelAndView("cadastroPropriedade");
        PropriedadeCommand command = new PropriedadeCommand();

        if (propriedadeId != null) {
            Propriedade propriedade = dados.buscarUnicoPorCampo(Propriedade.class, "id", propriedadeId);
            command.setPropriedade(propriedade);
            command.setProprietarioId(propriedade.getProprietario().getId());
            mv.addObject("propriedade", propriedade);
            mv.addObject("proprietario", propriedade.getProprietario());
        } else if (proprietarioId != null) {
            Proprietario proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", proprietarioId);
            command.setProprietarioId(proprietarioId);
            mv.addObject("proprietario", proprietario);
        } else {
            return new ModelAndView("redirect:/gerenciarProprietarios.html");
        }

        mv.addObject("PropriedadeCommand", command);
        return mv;
    }

    @GetMapping("/gerenciarPropriedadesDoProprietario.html")
    public ModelAndView gerenciarPropriedadesDoProprietario(@RequestParam(value = "id") Long idProprietario, @RequestParam(value = "busca", required = false) String busca) {
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

    @PostMapping("/cadastroPropriedade.html")
    public ModelAndView salvar(@ModelAttribute("PropriedadeCommand") @Validated PropriedadeCommand command, BindingResult errors) {
        ModelAndView mv = new ModelAndView("cadastroPropriedade");

        if (errors.hasErrors()) {
            if (command.getId() != null) {
                mv.addObject("propriedade", dados.buscarUnicoPorCampo(Propriedade.class, "id", command.getId()));
            }
            return mv;
        }

        Poligono poligono = null;
        try {
            String tipoEntrada = command.getTipoEntradaPoligono();
            if (tipoEntrada.equals("manual")) {
                poligono = criarPoligonoManual(command.getCoordenadasPorInsercaoManual());
            } else if (tipoEntrada.equals("arquivo")) {
                poligono = criarPoligonoPorCSV(command.getCoordenadasPorArquivo());
            }

            if (poligono == null) {
                errors.rejectValue("tipoEntradaPoligono", "poligono.vazio", "Falha: polígono vazio.");
            }
            if (poligono.possuiAutoIntersecao()) {
                errors.rejectValue("tipoEntradaPoligono", "poligono.autoIntersecao", "Falha: o polígono possui auto-interseção. Tente novamente.");
            }
            if (poligono.getPontos().size() < Poligono.QUANTIDADE_MINIMA_DE_PONTOS) {
                errors.rejectValue("tipoEntradaPoligono", "poligono.pontosInsuficientes", "Falha: o polígono deve possuir ao menos 3 pontos.");
            }
        } catch (Exception e) {
            errors.rejectValue("tipoEntradaPoligono", "poligono.invalido", "Falha: " + e.getMessage());
        }

        if (errors.hasErrors()) {
            return mv;
        }

        Propriedade propriedade;
        if (command.getId() != null) {
            propriedade = dados.buscarUnicoPorCampo(Propriedade.class, "id", command.getId());
            mv.addObject("resultado", "Propriedade alterada com sucesso!");
        } else {
            propriedade = command.getPropriedade();
            mv.addObject("resultado", "Propriedade cadastrada com sucesso!");
        }

        propriedade.setNome(command.getNome());
        propriedade.setProprietario(dados.buscarUnicoPorCampo(Proprietario.class, "id", command.getProprietarioId()));
        propriedade.setPoligono(poligono);

        dados.salvar(propriedade);

        mv.addObject("propriedade", propriedade);
        return mv;
    }

    private Poligono criarPoligonoManual(String textoCoordenadas) {
        List<Ponto> pontos = new ArrayList<>();
        

    }

    private Poligono criarPoligonoPorArquivo(MultipartFile arquivo) throws IOException {
        List<Ponto> pontos = new ArrayList<>();
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(arquivo.getInputStream()))) {
            String linha;
            while (leitor.readLine() != null) {
                String[] partes = leitor.readLine().split(";");
                Double latitude = FormatadorUtil.StringParaDouble(partes[0]);
                Double longitude = FormatadorUtil.StringParaDouble(partes[1]);
                pontos.add(new Ponto(latitude, longitude));
            }
            return new Poligono(pontos);
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

}
