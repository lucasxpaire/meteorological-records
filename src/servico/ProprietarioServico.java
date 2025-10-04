package servico;

import dados.Dados;
import modelo.Cor;
import modelo.Proprietario;
import org.springframework.stereotype.Service;
import util.FormatadorUtil;
import web.command.ProprietarioCommand;

import java.util.List;

@Service
public class ProprietarioServico {

    private final Dados dados;

    public ProprietarioServico(Dados dados) {
        this.dados = dados;
    }

    public Proprietario prepararProprietario(ProprietarioCommand command) {
        Proprietario proprietario;

        if (command.getId() != null) {
            proprietario = dados.buscarUnicoPorCampo(Proprietario.class, "id", command.getId());
        } else {
            proprietario = command.getProprietario();
        }

        proprietario.setNome(command.getNome());
        proprietario.setCpf(command.getCpf());
        proprietario.setTelefone(command.getTelefone());
        proprietario.setCor(dados.buscarUnicoPorCampo(Cor.class, "id", command.getCorId()));

        return proprietario;
    }

    public void salvar(Proprietario proprietario) {
        if (validarProprietario(proprietario)) {
            dados.salvar(proprietario);
        } else {
            throw new IllegalArgumentException("Dados do proprietário são inválidos.");
        }
    }

    public Proprietario buscarPorId(Long id) {
        if (existeAlgumProprietario()) {
            return dados.buscarUnicoPorCampo(Proprietario.class, "id", id);
        } else {
            throw new IllegalArgumentException("Falha: Não existe nenhum proprietário");
        }
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

    public Proprietario buscarPorCpf(String cpfBusca) {
        String cpfSemFormatacao = FormatadorUtil.removerFormatacaoCpf(cpfBusca);
        if (Proprietario.validarTamanhoCpf(cpfSemFormatacao)) {
            return dados.buscarUnicoPorCampo(Proprietario.class, "cpf", cpfSemFormatacao);
        } else {
            throw new IllegalArgumentException("CPF de busca é inválido.");
        }
    }

    public Proprietario buscarPorTelefone(String telefoneBusca) {
        String telefoneSemFormatacao = FormatadorUtil.removerFormatacaoTelefone(telefoneBusca);
        if (Proprietario.validarTamanhoTelefone(telefoneSemFormatacao)) {
            return dados.buscarUnicoPorCampo(Proprietario.class, "telefone", telefoneSemFormatacao);
        } else {
            throw new IllegalArgumentException("Telefone de busca é inválido.");
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

    public List<Proprietario> listarTodos() {
        return dados.listarTodos(Proprietario.class);
    }

    public boolean cpfPertenceAOutroProprietario(Long idAtual, String cpf) {
        Proprietario proprietarioExistente = buscarPorCpf(cpf);
        return proprietarioExistente != null && !proprietarioExistente.getId().equals(idAtual);
    }

    public boolean telefonePertenceAOutroProprietario(Long idAtual, String telefone) {
        Proprietario proprietarioExistente = buscarPorTelefone(telefone);
        return proprietarioExistente != null && !proprietarioExistente.getId().equals(idAtual);
    }

    private boolean validarProprietario(Proprietario proprietario) {
        return proprietario != null && Proprietario.validarNome(proprietario.getNome()) && Proprietario.validarTamanhoCpf(proprietario.getCpf()) && proprietario.getCor() != null;
    }
}
