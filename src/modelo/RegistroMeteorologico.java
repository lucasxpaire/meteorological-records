package modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import util.FormatadorUtil;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "REGISTRO_METEOROLOGICO")
@JsonPropertyOrder({"latitude", "longitude", "dataHoraPrevisao", "temperaturaPrevista"})
public class RegistroMeteorologico {

    private Long id;
    private LocalDateTime dataHora;

    private Double temperaturaReal;
    private Double temperaturaPrevista;
    private Double temperaturaCalculada;

    private Double precipitacaoReal;
    private Double precipitacaoPrevista;
    private Double precipitacaoCalculada;

    private Double radiacaoSolarReal;
    private Double radiacaoSolarPrevista;
    private Double radiacaoSolarCalculada;

    private Ponto ponto;

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_REGISTRO_METEOROLOGICO")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    @Column(name = "DATA_HORA")
    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    @JsonIgnore
    @Column(name = "TEMPERATURA_REAL")
    public Double getTemperaturaReal() {
        return temperaturaReal;
    }

    public void setTemperaturaReal(Double temperaturaReal) {
        this.temperaturaReal = temperaturaReal;
    }

    @Column(name = "TEMPERATURA_PREVISTA")
    public Double getTemperaturaPrevista() {
        return temperaturaPrevista;
    }

    public void setTemperaturaPrevista(Double temperaturaPrevista) {
        this.temperaturaPrevista = temperaturaPrevista;
    }

    @JsonIgnore
    @Column(name = "TEMPERATURA_CALCULADA")
    public Double getTemperaturaCalculada() {
        return temperaturaCalculada;
    }

    public void setTemperaturaCalculada(Double temperaturaCalculada) {
        this.temperaturaCalculada = temperaturaCalculada;
    }

    @JsonIgnore
    @Column(name = "PRECIPITACAO_REAL")
    public Double getPrecipitacaoReal() {
        return precipitacaoReal;
    }

    public void setPrecipitacaoReal(Double precipitacaoReal) {
        this.precipitacaoReal = precipitacaoReal;
    }

    @Column(name = "PRECIPITACAO_PREVISTA")
    public Double getPrecipitacaoPrevista() {
        return precipitacaoPrevista;
    }

    public void setPrecipitacaoPrevista(Double precipitacaoPrevista) {
        this.precipitacaoPrevista = precipitacaoPrevista;
    }

    @Column(name = "PRECIPITACAO_CALCULADA")
    public Double getPrecipitacaoCalculada() {
        return precipitacaoCalculada;
    }

    public void setPrecipitacaoCalculada(Double precipitacaoCalculada) {
        this.precipitacaoCalculada = precipitacaoCalculada;
    }

    @JsonIgnore
    @Column(name = "RADIACAO_SOLAR_REAL")
    public Double getRadiacaoSolarReal() {
        return radiacaoSolarReal;
    }

    public void setRadiacaoSolarReal(Double radiacaoSolar) {
        this.radiacaoSolarReal = radiacaoSolar;
    }

    @Column(name = "RADIACAO_SOLAR_PREVISTA")
    public Double getRadiacaoSolarPrevista() {
        return radiacaoSolarPrevista;
    }

    public void setRadiacaoSolarPrevista(Double radiacaoPrevista) {
        this.radiacaoSolarPrevista = radiacaoPrevista;
    }

    @Column(name = "RADIACAO_SOLAR_CALCULADA")
    public Double getRadiacaoSolarCalculada() {
        return radiacaoSolarCalculada;
    }

    public void setRadiacaoSolarCalculada(Double radiacaoCalculada) {
        this.radiacaoSolarCalculada = radiacaoCalculada;
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ID_PONTO")
    public Ponto getPonto() {
        return ponto;
    }

    public void setPonto(Ponto ponto) {
        this.ponto = ponto;
    }

    @Transient
    @JsonProperty("latitude")
    private Double obterLatitude() {
        return ponto.getLatitude();
    }

    @Transient
    @JsonProperty("longitude")
    private Double obterLongitude() {
        return ponto.getLongitude();
    }

    @Transient
    @JsonProperty("dataHoraPrevisao")
    private String obterDataHoraPrevisao() {
        return dataHora.format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO);
    }

}
