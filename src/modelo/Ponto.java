package modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.iakovlev.timeshape.TimeZoneEngine;
import util.FormatadorUtil;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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

    private static final double PESO_NULO = 0.0;
    private static final double PESO_MAXIMO_INTERPOLACAO = 10.0;
    private static final double DISTANCIA_MINIMA_PARA_PESO_MAXIMO = 0.1;
    private static final int NUMERADOR_PESO_PROXIMIDADE = 1;

    private static final int RAIO_DA_TERRA_EM_METROS = 6371000;
    private static final double GRAU_PARA_METROS = 111320.0;

    public static final String TEMPERATURA_INDISPONIVEL = "Indisponível";

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

    public Ponto(Double latitude, Double longitude, List<Temperatura> historicoTemperaturas) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.historicoTemperaturas = historicoTemperaturas;
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
    private Double calcularPesoDeProximidadePara(EstacaoMeteorologica estacao) {
        double distancia = distanciaAte(estacao.getLocalizacao());
        if (distancia < DISTANCIA_MINIMA_PARA_PESO_MAXIMO) {
            return PESO_MAXIMO_INTERPOLACAO;
        }
        return NUMERADOR_PESO_PROXIMIDADE / distancia;
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
    private LocalDateTime calcularDataHoraEstimativa(List<Temperatura> temperaturas) {
        if (temperaturas == null || temperaturas.isEmpty()) {
            return null;
        }

        return temperaturas.stream()
                .map(Temperatura::getDataHora)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    @Transient
    public Temperatura interpolarTemperaturaAtual(List<EstacaoMeteorologica> estacoes) {
        double somaTemperaturasPonderadas = 0.0;
        double somaPesos = 0.0;

        List<Temperatura> temperaturasRecentes = new ArrayList<>();

        for (EstacaoMeteorologica estacao : estacoes) {
            Temperatura temperaturaMaisRecente = estacao.getLocalizacao().getHistoricoTemperaturas().stream()
                    .max(Comparator.comparing(Temperatura::getDataHora))
                    .orElse(null);

            if (temperaturaMaisRecente == null) {
                continue;
            }

            temperaturasRecentes.add(temperaturaMaisRecente);

            Double temperatura = temperaturaMaisRecente.getTemperaturaReal();
            Double peso = calcularPesoDeProximidadePara(estacao);
            somaTemperaturasPonderadas += temperatura * peso;
            somaPesos += peso;
        }

        if (somaPesos == PESO_NULO) {
            throw new RuntimeException("Não foi possível atribuir um peso de distância entre o ponto e as estações");
        }

        Double temperaturaAtualInterpolada = somaTemperaturasPonderadas / somaPesos;
        LocalDateTime dataHoraEstimativa = calcularDataHoraEstimativa(temperaturasRecentes);

        Temperatura novaTemperatura = new Temperatura();
        novaTemperatura.setPonto(this);
        novaTemperatura.setTemperaturaReal(temperaturaAtualInterpolada);
        novaTemperatura.setDataHora(dataHoraEstimativa);
        return novaTemperatura;
    }

    @Transient
    public Double interpolarTemperaturaHistorica(List<EstacaoMeteorologica> estacoes, LocalDateTime dataHora) {
        double somaTemperaturasReaisPonderadas = 0.0;
        double somaPesos = 0.0;
        boolean dadosReaisEncontrados = false;

        for (EstacaoMeteorologica estacao : estacoes) {
            Optional<Temperatura> temperaturaRealDaEstacao = estacao.getLocalizacao().getHistoricoTemperaturas().stream()
                    .filter(t -> t.getDataHora().equals(dataHora) && t.getTemperaturaReal() != null)
                    .findFirst();

            if (temperaturaRealDaEstacao.isPresent()) {
                dadosReaisEncontrados = true;
                Double temperaturaReal = temperaturaRealDaEstacao.get().getTemperaturaReal();
                Double peso = calcularPesoDeProximidadePara(estacao);
                somaTemperaturasReaisPonderadas += temperaturaReal * peso;
                somaPesos += peso;
            }
        }

        if (dadosReaisEncontrados && somaPesos > PESO_NULO) {
            return somaTemperaturasReaisPonderadas / somaPesos;
        }

        return null;
    }

    @Transient
    public Temperatura preverTemperatura(LocalDateTime dataHoraPrevista, Map<EstacaoMeteorologica, Temperatura> previsoesPorEstacao) {
        double somatorioTemperaturasPrevistasPonderadas = 0.0;
        double somaPesos = 0.0;

        for (Map.Entry<EstacaoMeteorologica, Temperatura> entrada : previsoesPorEstacao.entrySet()) {
            EstacaoMeteorologica estacao = entrada.getKey();
            Temperatura previsaoCalculadaPelaEstacao = entrada.getValue();

            if (previsaoCalculadaPelaEstacao == null) {
                continue;
            }

            double peso = calcularPesoDeProximidadePara(estacao);
            somatorioTemperaturasPrevistasPonderadas += previsaoCalculadaPelaEstacao.getTemperaturaPrevista() * peso;
            somaPesos += peso;
        }

        Double temperaturaPrevistaFinal;
        if (somaPesos == PESO_NULO) {
            temperaturaPrevistaFinal = null;
        } else {
            temperaturaPrevistaFinal = somatorioTemperaturasPrevistasPonderadas / somaPesos;
        }

        Temperatura previsaoFinal = new Temperatura();
        previsaoFinal.setDataHora(dataHoraPrevista);
        previsaoFinal.setTemperaturaPrevista(temperaturaPrevistaFinal);
        previsaoFinal.setPonto(this);

        return previsaoFinal;
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
    @JsonProperty("temperaturaRecente")
    public String obterTemperaturaRecenteFormatada() {
        if (historicoTemperaturas.isEmpty()) {
            return TEMPERATURA_INDISPONIVEL;
        }

        Temperatura temperatura = getHistoricoTemperaturas().stream().max(Comparator.comparing(Temperatura::getDataHora))
                .orElse(null);

        if (temperatura == null || temperatura.getTemperaturaReal() == null || temperatura.getDataHora() == null) {
            return TEMPERATURA_INDISPONIVEL;
        }

        String temperaturaFormatada = FormatadorUtil.formatarTemperatura(temperatura.getTemperaturaReal());
        String dataFormatada = temperatura.getDataHora().format(FormatadorUtil.FORMATADOR_DATAHORA_PARA_EXIBICAO_MAPA);

        return String.format("<span class='temperatura-destaque'>%s</span> (%s)", temperaturaFormatada, dataFormatada);
    }

    @Transient
    @JsonProperty("temperaturaRecenteParaLabel")
    public String obterTemperaturaRecenteParaLabel() {
        if (historicoTemperaturas.isEmpty()) {
            return TEMPERATURA_INDISPONIVEL;
        }

        Temperatura temperatura = getHistoricoTemperaturas().stream().max(Comparator.comparing(Temperatura::getDataHora))
                .orElse(null);

        if (temperatura == null || temperatura.getTemperaturaReal() == null) {
            return TEMPERATURA_INDISPONIVEL;
        }

        return FormatadorUtil.formatarTemperatura(temperatura.getTemperaturaReal());
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
