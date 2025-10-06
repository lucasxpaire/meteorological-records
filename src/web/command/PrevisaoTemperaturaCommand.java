package web.command;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class PrevisaoTemperaturaCommand {

    private Double latitude;
    private Double longitude;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;

    private Integer hora;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Integer getHora() { return hora; }
    public void setHora(Integer hora) { this.hora = hora; }

}