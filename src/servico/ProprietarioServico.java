package servico;

import dados.Dados;
import modelo.Cor;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import util.FormatadorUtil;
import web.command.ProprietarioCommand;

import java.util.List;

import static util.FormatadorUtil.REGEX_APENAS_DIGITOS_NUMERICOS;

@Service
public class ProprietarioServico {

    @Autowired
    private Dados dados;

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

    public Proprietario buscarPorCpf(String cpfBusca) {
        if (Proprietario.validarTamanhoCpf(cpfBusca)) {
            return dados.buscarUnicoPorCampo(Proprietario.class, "cpf", FormatadorUtil.removerFormatacaoCpf(cpfBusca));
        } else {
            throw new IllegalArgumentException("CPF de busca é inválido.");
        }
    }

    public Proprietario buscarPorTelefone(String telefoneBusca) {
        if (Proprietario.validarTamanhoTelefone(telefoneBusca)) {
            return dados.buscarUnicoPorCampo(Proprietario.class, "telefone", FormatadorUtil.removerFormatacaoTelefone(telefoneBusca));
        } else {
            throw new IllegalArgumentException("Telefone de busca é inválido.");
        }
    }

    public List<Proprietario> buscarPorCpfOuNome(String busca) {
        if (busca == null || busca.trim().isEmpty()) {
            return listarTodos();
        }

        String cpfBuscaSemFormatacao = FormatadorUtil.removerFormatacaoCpf(busca);
        if (!cpfBuscaSemFormatacao.isEmpty() && cpfBuscaSemFormatacao.matches(REGEX_APENAS_DIGITOS_NUMERICOS)) {
            return dados.buscarListaPorCampo(Proprietario.class, "cpf", cpfBuscaSemFormatacao);
        } else {
            return dados.buscarPorCampoContendo(Proprietario.class, "nome", busca);
        }
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

    public boolean existeComEsseCpf(String cpf) {
        if (!Proprietario.validarTamanhoCpf(cpf)) {
            return false;
        }

        return dados.existeAlgumComEsseCampo(Proprietario.class, "cpf", FormatadorUtil.removerFormatacaoCpf(cpf));
    }

    public boolean existeComEsseTelefone(String telefone) {
        if (!Proprietario.validarTamanhoTelefone(telefone)) {
            return false;
        }

        return dados.existeAlgumComEsseCampo(Proprietario.class, "telefone", FormatadorUtil.removerFormatacaoTelefone(telefone));
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
