package web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import modelo.Temperatura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import servico.EstacaoMeteorologicaServico;
import servico.TemperaturaServico;
import web.command.PrevisaoTemperaturaCommand;
import web.validator.PrevisaoTemperaturaValidator;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PrevisaoTemperaturaController {

    @Autowired
    private EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    @Autowired
    private TemperaturaServico temperaturaServico;

    @Autowired
    private PrevisaoTemperaturaValidator previsaoTemperaturaValidator;

    @InitBinder("previsaoCommand")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(previsaoTemperaturaValidator);
    }

    private void adicionarEstacoesAoModelo(Model model) throws JsonProcessingException {
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();

        List<EstacaoMeteorologica> estacoes = estacaoMeteorologicaServico.listarTodas();
        String estacoesJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(estacoes);
        model.addAttribute("estacoesJson", estacoesJson);
    }

    @GetMapping("/previsaoTemperatura.html")
    public String exibirPaginaPrevisao(@ModelAttribute("previsaoCommand") PrevisaoTemperaturaCommand previsaoCommand, Model model) throws JsonProcessingException {
        adicionarEstacoesAoModelo(model);
        return "previsaoTemperatura";
    }

    @PostMapping("/previsaoTemperatura.html")
    public String realizarPrevisao(@Validated @ModelAttribute("previsaoCommand") PrevisaoTemperaturaCommand previsaoCommand, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) throws JsonProcessingException {

        if (bindingResult.hasErrors()) {
            adicionarEstacoesAoModelo(model);
            return "previsaoTemperatura";
        }

        Ponto ponto = new Ponto(previsaoCommand.getLatitude(), previsaoCommand.getLongitude());

        LocalDateTime dataHoraPrevista = previsaoCommand.getData().atTime(previsaoCommand.getHora(), 0);


        Temperatura temperaturaPrevista = temperaturaServico.preverTemperaturaParaPonto(ponto, dataHoraPrevista);
        temperaturaServico.salvar(temperaturaPrevista);

        if (temperaturaPrevista != null) {
            ObjectMapper conversorJson = new ObjectMapper();
            conversorJson.findAndRegisterModules();
            String previsaoJson = conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(temperaturaPrevista);
            redirectAttributes.addFlashAttribute("previsaoJson", previsaoJson);
        } else {
            redirectAttributes.addFlashAttribute("alertaFalha", "Não há dados suficientes para realizar a previsão para este local e data.");
        }

        redirectAttributes.addFlashAttribute("previsaoCommand", previsaoCommand);

        return "redirect:/previsaoTemperatura.html";
    }
}