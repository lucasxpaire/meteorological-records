package web.controller;

import modelo.Propriedade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import servico.EstacaoMeteorologicaServico;
import servico.PropriedadeServico;
import servico.ProprietarioServico;
import servico.TemperaturaServico;
import util.FormatadorUtil;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/index.html")
public class IndexController {

    @Autowired
    private ProprietarioServico proprietarioServico;

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private EstacaoMeteorologicaServico estacaoServico;

    @Autowired
    private TemperaturaServico temperaturaServico;

    @GetMapping
    public String index(Model model) {

        model.addAttribute("totalProprietarios", proprietarioServico.listarTodos().size());
        model.addAttribute("totalPropriedades", propriedadeServico.listarTodas().size());
        model.addAttribute("totalEstacoes", estacaoServico.listarTodas().size());

        try {
            Propriedade maisRecente = propriedadeServico.buscarMaisRecente();
            model.addAttribute("propriedadeMaisRecente", maisRecente);
        } catch (IllegalArgumentException e) {
            model.addAttribute("propriedadeMaisRecente", null);
        }

        LocalDateTime ultimaAtualizacaoEstacoesECentroides = temperaturaServico.getUltimaAtualizacaoEstacoesECentroides();
        LocalDateTime ultimaAtualizacaoPrevisoes = temperaturaServico.getUltimaAtualizacaoPrevisoesReais();
        LocalDateTime proximaAtualizacao = temperaturaServico.getProximaExecucaoAgendada();

        if (ultimaAtualizacaoEstacoesECentroides == null) {
            model.addAttribute("ultimaAtualizacaoEstacoesECentroides", "Aguardando execução");
        } else {
            model.addAttribute("ultimaAtualizacaoEstacoesECentroides", ultimaAtualizacaoEstacoesECentroides.format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO));
        }

        if (ultimaAtualizacaoPrevisoes == null) {
            model.addAttribute("ultimaAtualizacaoPrevisoes", "Aguardando execução");
        } else {
            model.addAttribute("ultimaAtualizacaoPrevisoes", ultimaAtualizacaoPrevisoes.format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO));
        }

        if (proximaAtualizacao == null) {
            model.addAttribute("proximaAtualizacao", "Aguardando primeira execução");
        } else {
            model.addAttribute("proximaAtualizacao", proximaAtualizacao.format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO));
        }

        return "index";
    }

}
