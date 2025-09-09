package web;

import modelo.Cor;
import modelo.Propriedade;
import modelo.Proprietario;
import util.FormatadorUtil;

import javax.persistence.*;
import java.util.Set;

public class CadastroProprietarioCommand {

    public Proprietario proprietario = new Proprietario();

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

    public void setCor(Cor corProprietario) {
        proprietario.setCor(corProprietario);
    }

    public Set<Propriedade> getPropriedades() {
        return proprietario.getPropriedades();
    }

    public void setPropriedades(Set<Propriedade> propriedades) {
        proprietario.setPropriedades(propriedades);
    }
}
