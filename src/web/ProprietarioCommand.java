package web;

import modelo.Cor;
import modelo.Proprietario;
import util.FormatadorUtil;

public class ProprietarioCommand {

    private Proprietario proprietario = new Proprietario();

    public Proprietario getProprietario() {
        return proprietario;
    }

    public void setProprietario(Proprietario proprietario) {
        this.proprietario = proprietario;
    }

    public Long getId() {
        return proprietario.getId();
    }

    public void setId(Long id) {
        proprietario.setId(id);
    }

    public String getNome() {
        return proprietario.getNome();
    }

    public void setNome(String nome) {
        proprietario.setNome(nome);
    }

    public String getCpf() {
        return proprietario.getCpf();
    }

    public void setCpf(String cpf) {
        proprietario.setCpf(FormatadorUtil.removerFormatacaoCpf(cpf));
    }

    public String getTelefone() {
        return proprietario.getTelefone();
    }

    public void setTelefone(String telefone) {
        proprietario.setTelefone(FormatadorUtil.removerFormatacaoTelefone(telefone));
    }

    public Cor getCor() {
        return proprietario.getCor();
    }

    public void setCor(Cor cor) {
        proprietario.setCor(cor);
    }

}
