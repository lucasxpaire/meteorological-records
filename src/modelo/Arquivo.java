package modelo;

import javax.persistence.*;

@Entity
@Table(name = "ARQUIVO")
public class Arquivo {

    private Long id;
    private String nomeOriginal;
    private String tipoConteudo;
    private byte[] conteudo;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_ARQUIVO")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NOME_ORIGINAL")
    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    @Column(name = "TIPO_CONTEUDO")
    public String getTipoConteudo() {
        return tipoConteudo;
    }

    public void setTipoConteudo(String tipoConteudo) {
        this.tipoConteudo = tipoConteudo;
    }

    @Lob
    @Column(name = "CONTEUDO", nullable = false)
    public byte[] getConteudo() {
        return conteudo;
    }

    public void setConteudo(byte[] conteudo) {
        this.conteudo = conteudo;
    }
}
