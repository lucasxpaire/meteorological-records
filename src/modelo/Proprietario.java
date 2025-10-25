package modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import util.FormatadorUtil;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

import static util.FormatadorUtil.*;

@Entity
@Table(name = "PROPRIETARIO")
public class Proprietario {

    private Long id;
    private String nome;
    private String cpf;
    private String telefone;
    private Cor cor;
    private Set<Propriedade> propriedades = new LinkedHashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PROPRIETARIO")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NOME", nullable = false)
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Column(name = "CPF", nullable = false, unique = true)
    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    @Column(name = "TELEFONE", unique = true)
    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @ManyToOne
    @JoinColumn(name = "ID_COR", nullable = false)
    public Cor getCor() {
        return cor;
    }

    public void setCor(Cor corProprietario) {
        this.cor = corProprietario;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "proprietario", orphanRemoval = true)
    public Set<Propriedade> getPropriedades() {
        return propriedades;
    }

    public void setPropriedades(Set<Propriedade> propriedades) {
        this.propriedades = propriedades;
    }

    @Transient
    public static boolean validarTamanhoCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }

        String cpfSemFormatacao = cpf.replaceAll(REGEX_QUALQUER_NAO_DIGITO_NUMERICO, STRING_VAZIA);
        return cpfSemFormatacao.length() == QUANTIDADE_DIGITOS_VALIDOS_CPF;
    }

    @Transient
    public static boolean validarNome(String nome) {
        return nome != null && !nome.trim().isEmpty();
    }

    @Transient
    public static boolean validarTamanhoTelefone(String telefone) {
        if (telefone == null) {
            return false;
        }
        String telefoneSemFormatacao = telefone.replaceAll(REGEX_QUALQUER_NAO_DIGITO_NUMERICO, STRING_VAZIA);
        return telefoneSemFormatacao.length() == QUANTIDADE_DIGITOS_VALIDOS_TELEFONE;
    }

    @Transient
    public String getCpfFormatado() {
        return FormatadorUtil.formatarCpf(cpf);
    }

    @Transient
    public String getTelefoneFormatado() {
        return FormatadorUtil.formatarTelefone(telefone);
    }

}
