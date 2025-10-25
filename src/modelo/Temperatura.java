package modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import util.FormatadorUtil;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TEMPERATURA")
@JsonPropertyOrder({"latitude", "longitude", "dataHoraPrevisao", "temperaturaPrevista"})
public class Temperatura {

    private Long id;
    private LocalDateTime dataHora;
    private Double temperaturaReal;
    private Double temperaturaPrevista;
    private Double temperaturaCalculada;
    private Ponto ponto;

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_TEMPERATURA")
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
