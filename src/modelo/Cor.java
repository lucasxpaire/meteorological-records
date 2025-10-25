package modelo;

import javax.persistence.*;

import static util.FormatadorUtil.REGEX_CODIGO_HEXADECIMAL_VALIDO;

@Entity
@Table(name = "COR")
public class Cor {

    private Long id;
    private String nome;
    private String codigoHexadecimal;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_COR")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NOME", unique = true)
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Column(name = "CODIGO_HEXADECIMAL", unique = true)
    public String getCodigoHexadecimal() {
        return codigoHexadecimal;
    }

    public void setCodigoHexadecimal(String codigoHexadecimal) {
        this.codigoHexadecimal = codigoHexadecimal;
    }

    @Transient
    public static boolean validarCodigoHexadecimal(String codigo) {
        return codigo != null && codigo.matches(REGEX_CODIGO_HEXADECIMAL_VALIDO);
    }
    @Transient
    public static boolean validarNome(String nome) {
        return nome != null && !nome.trim().isEmpty();
    }

}
