package servico;

import dados.Dados;
import modelo.Arquivo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArquivoServico {

    @Autowired
    private Dados dados;

    public Arquivo buscarPorId(Long id) {
        if (dados.existeAlgumComEsseCampo(Arquivo.class, "id", id)) {
            return dados.buscarUnicoPorCampo(Arquivo.class, "id", id);
        } else {
            return null;
        }
    }

}
