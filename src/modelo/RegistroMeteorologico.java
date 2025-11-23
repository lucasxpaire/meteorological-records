package modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import util.FormatadorUtil;
import web.json.RegistroMeteorologicoJson;

import javax.persistence.*;
import java.time.LocalDateTime;

import static util.FormatadorUtil.INDISPONIVEL;

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

    private Double diferencaTemperatura;
    private Double diferencaPrecipitacao;
    private Double diferencaRadiacaoSolar;

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

    @JsonView(RegistroMeteorologicoJson.Previsao.class)
    @Column(name = "TEMPERATURA_PREVISTA")
    public Double getTemperaturaPrevista() {
        return temperaturaPrevista;
    }

    public void setTemperaturaPrevista(Double temperaturaPrevista) {
        this.temperaturaPrevista = temperaturaPrevista;
    }

    @JsonView(RegistroMeteorologicoJson.Calculada.class)
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

    @JsonView(RegistroMeteorologicoJson.Previsao.class)
    @Column(name = "PRECIPITACAO_PREVISTA")
    public Double getPrecipitacaoPrevista() {
        return precipitacaoPrevista;
    }

    public void setPrecipitacaoPrevista(Double precipitacaoPrevista) {
        this.precipitacaoPrevista = precipitacaoPrevista;
    }

    @JsonView(RegistroMeteorologicoJson.Calculada.class)
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

    @JsonView(RegistroMeteorologicoJson.Previsao.class)
    @Column(name = "RADIACAO_SOLAR_PREVISTA")
    public Double getRadiacaoSolarPrevista() {
        return radiacaoSolarPrevista;
    }

    public void setRadiacaoSolarPrevista(Double radiacaoPrevista) {
        this.radiacaoSolarPrevista = radiacaoPrevista;
    }

    @JsonView(RegistroMeteorologicoJson.Calculada.class)
    @Column(name = "RADIACAO_SOLAR_CALCULADA")
    public Double getRadiacaoSolarCalculada() {
        return radiacaoSolarCalculada;
    }

    public void setRadiacaoSolarCalculada(Double radiacaoCalculada) {
        this.radiacaoSolarCalculada = radiacaoCalculada;
    }

    @Column(name = "DIFERENCA_TEMPERATURA")
    public Double getDiferencaTemperatura() {
        return diferencaTemperatura;
    }

    public void setDiferencaTemperatura(Double diferencaTemperatura) {
        this.diferencaTemperatura = diferencaTemperatura;
    }

    @Column(name = "DIFERENCA_PRECIPITACAO")
    public Double getDiferencaPrecipitacao() {
        return diferencaPrecipitacao;
    }

    public void setDiferencaPrecipitacao(Double diferencaPrecipitacao) {
        this.diferencaPrecipitacao = diferencaPrecipitacao;
    }

    @Column(name = "DIFERENCA_RADIACAO_SOLAR")
    public Double getDiferencaRadiacaoSolar() {
        return diferencaRadiacaoSolar;
    }

    public void setDiferencaRadiacaoSolar(Double diferencaRadiacaoSolar) {
        this.diferencaRadiacaoSolar = diferencaRadiacaoSolar;
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

    @JsonView(RegistroMeteorologicoJson.Publico.class)
    @Transient
    @JsonProperty("latitude")
    private Double obterLatitude() {
        return ponto.getLatitude();
    }

    @JsonView(RegistroMeteorologicoJson.Publico.class)
    @Transient
    @JsonProperty("longitude")
    private Double obterLongitude() {
        return ponto.getLongitude();
    }

    @JsonView(RegistroMeteorologicoJson.Previsao.class)
    @Transient
    @JsonProperty("dataHoraPrevisao")
    private String obterDataHoraPrevisao() {
        return dataHora.format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO);
    }

    @JsonView(RegistroMeteorologicoJson.Calculada.class)
    @Transient
    @JsonProperty("dataHoraCalculada")
    private String obterDataHoraRegistroCalculado() {
        return dataHora.format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO);
    }

}
