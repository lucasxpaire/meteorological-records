package servico;

import dados.Dados;
import modelo.Propriedade;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropriedadeServico {

    private final Dados dados;

    public PropriedadeServico(Dados dados) {
        this.dados = dados;
    }

    public void salvar(Propriedade propriedade) {
        if (validarPropriedade(propriedade)) {
            dados.salvar(propriedade);
        } else {
            throw new IllegalArgumentException("Dados da propriedade são inválidos.");
        }
    }

    public Propriedade buscarMaisRecenteAdicionada() {
        return dados.buscarMaisRecente(Propriedade.class);
    }

    public List<Propriedade> buscarPorTermoDeBusca (String termoDeBusca) {
        if (termoDeBusca == null || termoDeBusca.trim().isEmpty()) {
            return listarTodas();
        }

        return dados.buscarPorCampoContendo(Propriedade.class, "nome", termoDeBusca);
    }

    public List<Propriedade> buscarPorNome(String nome) {
        if (Propriedade.validarNome(nome)) {
            return dados.buscarListaPorCampo(Propriedade.class, "nome", nome);
        } else {
            throw new IllegalArgumentException("Nome de busca é inválido.");
        }
    }

    public List<Propriedade> listarTodas() {
        return dados.listarTodos(Propriedade.class);
    }

    public void deletar(Propriedade propriedade) {
        if (validarPropriedade(propriedade)) {
            dados.deletar(propriedade);
        } else {
            throw new IllegalArgumentException("Dados da propriedade são inválidos.");
        }
    }

    public boolean existeAlgumaPropriedade() {
        return dados.existeAlgum(Propriedade.class);
    }

    public boolean existeComEsseNome(String nome) {
        return dados.existeAlgumComEsseCampo(Propriedade.class, "nome", nome);
    }

    private boolean validarPropriedade(Propriedade propriedade) {
        return propriedade != null && propriedade.getNome() != null && propriedade.getPoligono() != null && propriedade.getProprietario() != null;
    }

}
