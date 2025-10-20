package servico;

import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import modelo.Temperatura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PontoServico {

    @Autowired
    private Dados dados;

    @Autowired
    private EstacaoMeteorologicaServico estacaoMeteorologicaServico;

    public Ponto calcularEAdicionarTemperaturaAtual(Ponto ponto) {
        List<EstacaoMeteorologica> estacoesRelevantes = estacaoMeteorologicaServico.buscarEstacoesRelevantes(ponto);
        ponto.setFusoHorario(ponto.determinarFusoHorario());
        ponto.setEstacoesMeteorologicas(estacoesRelevantes);
        Temperatura temperaturaCalculada = ponto.interpolarTemperaturaAtual(ponto.getEstacoesMeteorologicas());
        ponto.getHistoricoTemperaturas().add(temperaturaCalculada);

        return ponto;
    }

    public Ponto buscarPorId(Long id) {
        if (dados.existeAlgumComEsseCampo(Ponto.class, "id", id)) {
            return dados.buscarUnicoPorCampo(Ponto.class, "id", id);
        } else {
            throw new IllegalArgumentException("Falha: Não existe nenhum ponto com esse id.");
        }
    }

}
