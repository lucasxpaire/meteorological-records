package servico;

import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import modelo.Temperatura;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PontoServico {

    private final Dados dados;
    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    public PontoServico(Dados dados, EstacaoMeteorologicaServico estacaoMeteorologicaServico) {
        this.dados = dados;
        this.estacaoMeteorologicaServico = estacaoMeteorologicaServico;
    }

    public void calcularEAdicionarTemperaturaAtual(Ponto ponto) {
        List<EstacaoMeteorologica> estacoesRelevantes = estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto);
        if (estacoesRelevantes.isEmpty()) {
            return;
        }

        ponto.setFusoHorario(ponto.determinarFusoHorario());
        ponto.setEstacoesMeteorologicas(estacoesRelevantes);
        Temperatura temperaturaCalculada = ponto.interpolarTemperaturaAtual(ponto.getEstacoesMeteorologicas());
        ponto.getHistoricoTemperaturas().add(temperaturaCalculada);

    }

    public void salvar(Ponto ponto) {
        if (validarPonto(ponto)) {
            dados.salvar(ponto);
        } else {
            throw new IllegalArgumentException("Dados do ponto são inválidos.");
        }
    }

    private boolean validarPonto(Ponto ponto) {
        return ponto != null && ponto.getLatitude() != null && ponto.getLongitude() != null;
    }

}
