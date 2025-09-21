package servico;

import dados.Dados;
import modelo.Proprietario;
import org.springframework.stereotype.Service;
import util.FormatadorUtil;

import java.util.List;

@Service
public class ProprietarioServico {

    private final Dados dados;

    public ProprietarioServico(Dados dados) {
        this.dados = dados;
    }

    public void salvar(Proprietario proprietario) {
        if (validarProprietario(proprietario)) {
            dados.salvar(proprietario);
        } else {
            throw new IllegalArgumentException("Dados do proprietário são inválidos.");
        }
    }

    public Proprietario buscarPorCpf(String cpfBusca) {
        String cpfNormalizado = FormatadorUtil.removerFormatacaoCpf(cpfBusca);
        if (Proprietario.validarTamanhoCpf(cpfNormalizado)) {
            return dados.buscarUnicoPorCampo(Proprietario.class, "cpf", cpfNormalizado);
        } else {
            throw new IllegalArgumentException("CPF de busca é inválido.");
        }
    }

    public List<Proprietario> buscarPorCpfOuNome(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarTodos();
        }

        String termoNormalizado = FormatadorUtil.removerFormatacaoCpf(termo);
        if (!termoNormalizado.isEmpty() && termoNormalizado.matches("^[0-9]+$")) {
            return dados.buscarListaPorCampo(Proprietario.class, "cpf", termoNormalizado);
        } else {
            return dados.buscarPorCampoContendo(Proprietario.class, "nome", termo);
        }
    }

    public Proprietario buscarUltimoAdicionado() {
        return dados.buscarMaisRecente(Proprietario.class);
    }

    public List<Proprietario> listarTodos() {
        return dados.listarTodos(Proprietario.class);
    }

    public void deletar(Proprietario proprietario) {
        if (validarProprietario(proprietario)) {
            dados.deletar(proprietario);
        } else {
            throw new IllegalArgumentException("Dados do proprietário são inválidos.");
        }
    }

    public boolean existeAlgumProprietario() {
        return dados.existeAlgum(Proprietario.class);
    }

    public boolean cpfJaExiste(String cpf) {
        String cpfNormalizado = FormatadorUtil.removerFormatacaoCpf(cpf);
        if (!Proprietario.validarTamanhoCpf(cpfNormalizado)) {
            return false;
        }

        return dados.existeAlgumComEsseCampo(Proprietario.class, "cpf", cpfNormalizado);
    }

    public boolean telefoneJaExiste(String telefone) {
        String telefoneNormalizado = FormatadorUtil.removerFormatacaoTelefone(telefone);
        if (!Proprietario.validarTamanhoTelefone(telefone)) {
            return false;
        }

        return dados.existeAlgumComEsseCampo(Proprietario.class, "telefone", telefoneNormalizado);
    }

    private boolean validarProprietario(Proprietario proprietario) {
        return proprietario != null && Proprietario.validarNome(proprietario.getNome()) && Proprietario.validarTamanhoCpf(proprietario.getCpf()) && proprietario.getCor() != null;
    }

}
