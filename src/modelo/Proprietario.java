package modelo;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import util.FormatadorUtil;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "PROPRIETARIO")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Proprietario {

    public static final int QUANTIDADE_DIGITOS_VALIDOS_CPF = 11;
    public static final String PONTOS_E_TRACOS = "\\D";
    public static final String ESPACO_EM_BRANCO = "";

    public static final int QUANTIDADE_DIGITOS_COM_NOVE_NO_COMECO = 11;

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

    @OneToMany(mappedBy = "proprietario", cascade = CascadeType.ALL, orphanRemoval = true)
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

        String cpfSemFormatacao = cpf.replaceAll(PONTOS_E_TRACOS, ESPACO_EM_BRANCO);
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
        String telefoneSemFormatacao = telefone.replaceAll(PONTOS_E_TRACOS, ESPACO_EM_BRANCO);
        return telefoneSemFormatacao.length() == QUANTIDADE_DIGITOS_COM_NOVE_NO_COMECO;
    }

    @Transient
    public String getCpfFormatado() {
        return FormatadorUtil.formatarCpf(this.cpf);
    }

    @Transient
    public String getTelefoneFormatado() {
        return FormatadorUtil.formatarTelefone(this.telefone);
    }

}
