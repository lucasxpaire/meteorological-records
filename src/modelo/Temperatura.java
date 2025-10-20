package modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import util.FormatadorUtil;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TEMPERATURA")
public class Temperatura {

    private Long id;
    private LocalDateTime dataHora;
    private Double temperaturaReal;
    private Double temperaturaPrevista;
    private Double diferenca;
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

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    @Column(name = "DATA_HORA")
    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    @Column(name = "TEMPERATURA_REAL")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    public Double getTemperaturaReal() {
        return temperaturaReal;
    }

    public void setTemperaturaReal(Double temperaturaEstimada) {
        this.temperaturaReal = temperaturaEstimada;
    }

    @Column(name = "TEMPERATURA_PREVISTA")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    public Double getTemperaturaPrevista() {
        return temperaturaPrevista;
    }

    public void setTemperaturaPrevista(Double temperaturaPrevista) {
        this.temperaturaPrevista = temperaturaPrevista;
    }

    @Column(name = "DIFERENCA")
    public Double getDiferenca() {
        return diferenca;
    }

    public void setDiferenca(Double diferenca) {
        this.diferenca = diferenca;
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
    @JsonProperty("temperaturaFormatada")
    public String getTemperaturaFormatada() {
        if (this.temperaturaReal != null) {
            String temperaturaFormatada = FormatadorUtil.formatarTemperatura(temperaturaReal);
            String dataFormatada = dataHora.format(FormatadorUtil.FORMATADOR_DATAHORA_PARA_EXIBICAO_MAPA);

            return String.format("<span class='temperatura-destaque'>%s</span> (%s)", temperaturaFormatada, dataFormatada);
        }
        if (this.temperaturaPrevista != null) {
            String temperaturaFormatada = FormatadorUtil.formatarTemperatura(temperaturaPrevista);
            String dataFormatada = dataHora.format(FormatadorUtil.FORMATADOR_DATAHORA_PARA_EXIBICAO_MAPA);

            return String.format("<span class='temperatura-destaque'>%s (Prevista)</span> (%s)", temperaturaFormatada, dataFormatada);
        }
        return "Indisponível";
    }

    @Transient
    @JsonProperty("latitudeFormatada")
    public String getLatitudeFormatada() {
        return this.ponto.getLatitudeFormatada();
    }

    @Transient
    @JsonProperty("longitudeFormatada")
    public String getLongitudeFormatada() {
        return this.ponto.getLongitudeFormatada();
    }

    @Transient
    @JsonProperty("latitude")
    public Double getLatitude() {
        return this.ponto.getLatitude();
    }

    @Transient
    @JsonProperty("longitude")
    public Double getLongitude() {
        return this.ponto.getLongitude();
    }
}
