package servico;

import dados.Dados;
import modelo.Ponto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PontoServico {

    @Autowired
    private Dados dados;

    public Ponto buscarPorId(Long id) {
        if (dados.existeAlgumComEsseCampo(Ponto.class, "id", id)) {
            return dados.buscarUnicoPorCampo(Ponto.class, "id", id);
        } else {
            return null;
        }
    }

}
