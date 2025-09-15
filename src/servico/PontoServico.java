package servico;

import dados.Dados;
import modelo.Ponto;
import org.springframework.stereotype.Service;

@Service
public class PontoServico {

    private final Dados dados;

    public PontoServico(Dados dados) {
        this.dados = dados;
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
