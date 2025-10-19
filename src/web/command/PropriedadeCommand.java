package web.command;

import modelo.Propriedade;
import util.FormatadorUtil;

public class PropriedadeCommand {

    Propriedade propriedade = new Propriedade();

    private String cpfProprietario;

    private String pontos;
    private String nomeArquivoPontos;

    public Propriedade getPropriedade() {
        return propriedade;
    }

    public void setPropriedade(Propriedade propriedade) {
        this.propriedade = propriedade;
    }

    public Long getId() {
        return propriedade.getId();
    }

    public void setId(Long id) {
        propriedade.setId(id);
    }

    public String getNome() {
        return propriedade.getNome();
    }

    public void setNome(String nome) {
        propriedade.setNome(nome);
    }

    public String getCpfProprietario() {
        return cpfProprietario;
    }

    public void setCpfProprietario(String cpfProprietario) {
        this.cpfProprietario = FormatadorUtil.removerFormatacaoCpf(cpfProprietario);
    }

    public String getPontos() {
        return pontos;
    }

    public void setPontos(String pontos) {
        this.pontos = pontos;
    }

    public String getNomeArquivoPontos() {
        return nomeArquivoPontos;
    }

    public void setNomeArquivoPontos(String nomeArquivoPontos) {
        this.nomeArquivoPontos = nomeArquivoPontos;
    }

}
