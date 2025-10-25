package modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.iakovlev.timeshape.TimeZoneEngine;
import util.FormatadorUtil;

import javax.persistence.*;
import java.time.ZoneId;
import java.util.*;

import static util.FormatadorUtil.INDISPONIVEL;
import static util.FormatadorUtil.STRING_VAZIA;

@Entity
@Table(name = "PONTO")
public class Ponto {

    public static final int PRIMEIRO_QUADRANTE = 1;
    public static final int SEGUNDO_QUADRANTE = 2;
    public static final int TERCEIRO_QUADRANTE = 3;
    public static final int QUARTO_QUADRANTE = 4;

    public static final int LATITUDE_MINIMA = -90;
    public static final int LATITUDE_MAXIMA = 90;
    public static final int LONGITUDE_MINIMA = -180;
    public static final int LONGITUDE_MAXIMA = 180;

    private static final double PESO_MAXIMO = 10.0;
    private static final double DISTANCIA_MINIMA = 0.1;
    private static final int PESO_PROXIMIDADE = 1;

    private static final int RAIO_DA_TERRA_EM_METROS = 6371000;

    private Long id;
    private Double latitude;
    private Double longitude;
    private List<Temperatura> historicoTemperaturas = new ArrayList<>();
    private List<EstacaoMeteorologica> estacoesMeteorologicas = new ArrayList<>();
    private String fusoHorario;

    private static final TimeZoneEngine timeZoneEngine = TimeZoneEngine.initialize();

    public Ponto(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Ponto() {}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PONTO")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "LATITUDE",  nullable = false, precision = 10, scale = 8)
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Column(name = "LONGITUDE", nullable = false, precision = 10, scale = 8)
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @OneToMany(mappedBy = "ponto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataHora DESC")
    public List<Temperatura> getHistoricoTemperaturas() {
        return historicoTemperaturas;
    }

    public void setHistoricoTemperaturas(List<Temperatura> historicoTemperaturas) {
        this.historicoTemperaturas = historicoTemperaturas;
    }

    @ManyToMany
    @JoinTable(name = "PONTO_ESTACAO", joinColumns = @JoinColumn(name = "ID_PONTO"), inverseJoinColumns = @JoinColumn(name = "ID_ESTACAO_METEOROLOGICA"))
    public List<EstacaoMeteorologica> getEstacoesMeteorologicas() {
        return estacoesMeteorologicas;
    }

    public void setEstacoesMeteorologicas(List<EstacaoMeteorologica> estacoesMeteorologicas) {
        this.estacoesMeteorologicas = estacoesMeteorologicas;
    }

    @Column(name = "FUSO_HORARIO")
    public String getFusoHorario() {
        return fusoHorario;
    }

    public void setFusoHorario(String fusoHorario) {
        this.fusoHorario = fusoHorario;
    }

    @Transient
    public String determinarFusoHorario() {
        return timeZoneEngine.query(getLatitude(), getLongitude())
                .map(ZoneId::getId)
                .orElse(null);
    }
    
    @Transient
    public double distanciaAte(Ponto outroPonto) {
        double deltaLatitude = Math.toRadians(outroPonto.getLatitude() - this.latitude);
        double deltaLongitude = Math.toRadians(outroPonto.getLongitude() - this.longitude);

        double diferencaAngular = Math.sin(deltaLatitude / 2) * Math.sin(deltaLatitude / 2) + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(outroPonto.getLatitude())) * Math.sin(deltaLongitude / 2) * Math.sin(deltaLongitude / 2);

        double anguloCentralEmRadianos = 2 * Math.atan2(Math.sqrt(diferencaAngular), Math.sqrt(1 - diferencaAngular));

        return RAIO_DA_TERRA_EM_METROS * anguloCentralEmRadianos;
    }

    @Transient
    public Double calcularPesoDeProximidadePara(EstacaoMeteorologica estacao) {
        double distancia = distanciaAte(estacao.getLocalizacao());
        if (distancia < DISTANCIA_MINIMA) {
            return PESO_MAXIMO;
        }
        return PESO_PROXIMIDADE / distancia;
    }

    @Transient
    public int obterQuadranteEmRelacaoA(Ponto pontoDeReferencia) {
        double latitudeReferencia = pontoDeReferencia.getLatitude();
        double longitudeReferencia = pontoDeReferencia.getLongitude();

        if (latitude >= latitudeReferencia && longitude >= longitudeReferencia) {
            return PRIMEIRO_QUADRANTE;
        } else if (latitude >= latitudeReferencia && longitude <= longitudeReferencia) {
            return SEGUNDO_QUADRANTE;
        } else if (latitude <= latitudeReferencia && longitude <= longitudeReferencia) {
            return TERCEIRO_QUADRANTE;
        } else {
            return QUARTO_QUADRANTE;
        }
    }

    @Transient
    public static boolean validarLatitude(double latitude) {
        return latitude >= LATITUDE_MINIMA && latitude <= LATITUDE_MAXIMA;
    }

    @Transient
    public static boolean validarLongitude(double longitude) {
        return longitude >= LONGITUDE_MINIMA && longitude <= LONGITUDE_MAXIMA;
    }

    @Transient
    private Optional<Temperatura> obterTemperaturaRealMaisRecente() {
        return getHistoricoTemperaturas().stream()
                .filter(t -> t != null && t.getTemperaturaReal() != null && t.getDataHora() != null)
                .max(Comparator.comparing(Temperatura::getDataHora));
    }

    @Transient
    private Optional<Temperatura> obterTemperaturaPrevistaMaisRecente() {
        return getHistoricoTemperaturas().stream()
                .filter(t -> t != null && t.getTemperaturaPrevista() != null && t.getDataHora() != null)
                .max(Comparator.comparing(Temperatura::getDataHora));
    }

    @Transient
    private Optional<Temperatura> obterTemperaturaCalculadaMaisRecente() {
        return getHistoricoTemperaturas().stream()
                .filter(t -> t != null && t.getTemperaturaCalculada() != null && t.getDataHora() != null)
                .max(Comparator.comparing(Temperatura::getDataHora));
    }

    @Transient
    @JsonProperty("temperaturaReal")
    private String obterTemperaturaReal() {
        return obterTemperaturaRealMaisRecente()
                .map(t -> FormatadorUtil.formatarTemperatura(t.getTemperaturaReal()))
                .orElse(INDISPONIVEL);
    }

    @Transient
    @JsonProperty("dataHoraTemperaturaReal")
    private String obterDataHoraTemperaturaReal() {
        return obterTemperaturaRealMaisRecente()
                .map(t -> String.format("(%s)", t.getDataHora().format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO)))
                .orElse(STRING_VAZIA);
    }

    @Transient
    @JsonProperty("temperaturaPrevista")
    private String obterTemperaturaPrevista() {
        return obterTemperaturaPrevistaMaisRecente()
                .map(t -> FormatadorUtil.formatarTemperatura(t.getTemperaturaPrevista()))
                .orElse(INDISPONIVEL);
    }

    @Transient
    @JsonProperty("dataHoraTemperaturaPrevista")
    public String obterDataHoraTemperaturaPrevista() {
        return obterTemperaturaPrevistaMaisRecente()
                .map(t -> String.format("(%s)", t.getDataHora().format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO)))
                .orElse(STRING_VAZIA);
    }

    @Transient
    @JsonProperty("temperaturaCalculada")
    private String obterTemperaturaCalculada() {
        return obterTemperaturaCalculadaMaisRecente()
                .map(t -> FormatadorUtil.formatarTemperatura(t.getTemperaturaCalculada()))
                .orElse(INDISPONIVEL);
    }

    @Transient
    @JsonProperty("dataHoraTemperaturaCalculada")
    public String obterDataHoraTemperaturaCalculada() {
        return obterTemperaturaCalculadaMaisRecente()
                .map(t -> String.format("(%s)", t.getDataHora().format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO)))
                .orElse(STRING_VAZIA);
    }

    @Transient
    @JsonProperty("latitudeFormatada")
    public String getLatitudeFormatada() {
        return FormatadorUtil.formatarPontoDecimalParaVirgula(latitude);
    }

    @Transient
    @JsonProperty("longitudeFormatada")
    public String getLongitudeFormatada() {
        return FormatadorUtil.formatarPontoDecimalParaVirgula(longitude);
    }

}
