package modelo;

import javax.persistence.*;

@Entity
@Table(name = "PROPRIEDADE")
public class Propriedade {

    private Long id;
    private String nome;
    private Poligono poligono;
    private Proprietario proprietario;
    private Ponto centroide;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PROPRIEDADE")
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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ID_POLIGONO", nullable = false)
    public Poligono getPoligono() {
        return poligono;
    }

    public void setPoligono(Poligono poligono) {
        this.poligono = poligono;
    }

    @ManyToOne
    @JoinColumn(name = "ID_PROPRIETARIO", nullable = false)
    public Proprietario getProprietario() {
        return proprietario;
    }

    public void setProprietario(Proprietario proprietario) {
        this.proprietario = proprietario;
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ID_CENTROIDE", nullable = false)
    public Ponto getCentroide() {
        return centroide;
    }

    public void setCentroide(Ponto centroide) {
        this.centroide = centroide;
    }

    @Transient
    public static boolean validarNome(String nome) {
        return nome != null && !nome.trim().isEmpty();
    }
}

