package modelo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "POLIGONO")
public class Poligono {

    public static final int QUANTIDADE_MINIMA_DE_PONTOS = 3;
    private static final int PRODUTO_ORIENTACAO_OPOSTAS = 0;

    private Long id;
    private List<Ponto> pontos = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_POLIGONO")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ID_POLIGONO")
    @OrderColumn(name = "ORDEM_PONTO")
    public List<Ponto> getPontos() {
        return pontos;
    }

    public void setPontos(List<Ponto> pontos) {
        this.pontos = pontos;
    }

    @Transient
    public Ponto calcularCentroide() {
        if (pontos == null || pontos.isEmpty()) {
            throw new IllegalArgumentException("Polígono sem pontos para calcular o centroide.");
        }

        double latitudeMinima = Ponto.LATITUDE_MAXIMA;
        double latitudeMaxima = Ponto.LATITUDE_MINIMA;
        double longitudeMinima = Ponto.LONGITUDE_MAXIMA;
        double longitudeMaxima = Ponto.LONGITUDE_MINIMA;

        for (Ponto p : pontos) {
            latitudeMinima = Math.min(latitudeMinima, p.getLatitude());
            latitudeMaxima = Math.max(latitudeMaxima, p.getLatitude());
            longitudeMinima = Math.min(longitudeMinima, p.getLongitude());
            longitudeMaxima = Math.max(longitudeMaxima, p.getLongitude());
        }

        double latitudeCentroide = (latitudeMinima + latitudeMaxima) / 2.0;
        double longitudeCentroide = (longitudeMinima + longitudeMaxima) / 2.0;

        return new Ponto(latitudeCentroide, longitudeCentroide);
    }

    @Transient
    public boolean possuiAutoIntersecao() {
        int totalDePontos = pontos.size();
        if (totalDePontos <= QUANTIDADE_MINIMA_DE_PONTOS) {
            return false;
        }

        for (int i = 0; i < totalDePontos; i++) {
            Ponto inicioArestaA = pontos.get(i);
            Ponto fimArestaA = pontos.get((i + 1) % totalDePontos);

            for (int j = i + 1; j < totalDePontos; j++) {
                Ponto inicioArestaB = pontos.get(j);
                Ponto fimArestaB = pontos.get((j + 1) % totalDePontos);

                if (arestasSaoAdjacentes(inicioArestaA, fimArestaA, inicioArestaB, fimArestaB)) {
                    continue;
                }

                if (arestasSeCruzam(inicioArestaA, fimArestaA, inicioArestaB, fimArestaB)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Transient
    private boolean arestasSaoAdjacentes(Ponto inicioArestaA, Ponto fimArestaA, Ponto inicioArestaB, Ponto fimArestaB) {
        return inicioArestaA.equals(inicioArestaB) || inicioArestaA.equals(fimArestaB) || fimArestaA.equals(inicioArestaB) || fimArestaA.equals(fimArestaB);
    }

    @Transient
    private boolean arestasSeCruzam(Ponto inicioArestaA, Ponto fimArestaA, Ponto inicioArestaB, Ponto fimArestaB) {
        double orientacaoInicioArestaB = calcularOrientacaoGeometrica(inicioArestaA, fimArestaA, inicioArestaB);
        double orientacaoFimArestaB = calcularOrientacaoGeometrica(inicioArestaA, fimArestaA, fimArestaB);

        double orientacaoInicioArestaA = calcularOrientacaoGeometrica(inicioArestaB, fimArestaB, inicioArestaA);
        double orientacaoFimArestaA = calcularOrientacaoGeometrica(inicioArestaB, fimArestaB, fimArestaA);

        if (orientacaoInicioArestaB * orientacaoFimArestaB < PRODUTO_ORIENTACAO_OPOSTAS && orientacaoInicioArestaA * orientacaoFimArestaA < 0) {
            return true;
        }

        return false;
    }

    @Transient
    private double calcularOrientacaoGeometrica(Ponto pontoReferencia, Ponto pontoA, Ponto pontoB) {
        return (pontoA.getLatitude() - pontoReferencia.getLatitude()) * (pontoB.getLongitude() - pontoA.getLongitude()) - (pontoA.getLongitude() - pontoReferencia.getLongitude()) * (pontoB.getLatitude() - pontoA.getLatitude());
    }

}
