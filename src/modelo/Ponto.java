package modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.iakovlev.timeshape.TimeZoneEngine;
import util.FormatadorUtil;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private Long id;
    private Double latitude;
    private Double longitude;
    private List<Temperatura> historicoTemperaturas = new ArrayList<>();
    private List<EstacaoMeteorologica> estacoesMeteorologicas = new ArrayList<>();
    private String fusoHorario;

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

    @JsonIgnore
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
    public String obterFusoHorario() {
        TimeZoneEngine inicializador = TimeZoneEngine.initialize();

        return inicializador.query(getLatitude(), getLongitude())
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
    public Temperatura interpolarTemperaturaAtual(List<EstacaoMeteorologica> estacoes) {
        double somaTemperaturasPonderadas = 0.0;
        double somaPesos = 0.0;

        for (EstacaoMeteorologica estacao : estacoes) {
            List<Temperatura> historico = estacao.getLocalizacao().getHistoricoTemperaturas();
            if (historico.isEmpty()) {
                continue;
            }

            Double temperaturaAtualDaEstacao = historico.getFirst().getTemperaturaReal();
            Double peso = calcularPesoDeProximidadePara(estacao);
            somaTemperaturasPonderadas += temperaturaAtualDaEstacao * peso;
            somaPesos += peso;
        }

        if (somaPesos == PESO_NULO) {
            throw new RuntimeException("Não foi possível atribuir um peso de distância entre o ponto e as estações");
        }

        Double temperaturaAtualInterpolada = somaTemperaturasPonderadas / somaPesos;
        LocalDateTime dataHoraEstimativa = calcularDataHoraEstimativa(estacoes);

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
    private LocalDateTime calcularDataHoraEstimativa(List<EstacaoMeteorologica> estacoes) {
        List<LocalDateTime> datasRecentes = new ArrayList<>();

        for (EstacaoMeteorologica estacao : estacoes) {
            List<Temperatura> historico = estacao.getLocalizacao().getHistoricoTemperaturas();
            if (historico.isEmpty()) {
                datasRecentes.add(null);
            } else {
                datasRecentes.add(historico.getFirst().getDataHora());
            }
        }
        if (datasRecentes.isEmpty() || datasRecentes.getFirst() == null) {
            return null;
        }

        LocalDateTime dataMaisRecente = datasRecentes.getFirst();

        return datasRecentes.stream()
                .filter(dataHora -> dataHora.toLocalDate().equals(dataMaisRecente.toLocalDate()))
                .collect(Collectors.groupingBy(dataHora -> dataHora, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
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
    public String obterCentroideFormatado() {
        return FormatadorUtil.formatarPontoDecimalParaVirgula(latitude) + FormatadorUtil.formatarPontoDecimalParaVirgula(longitude);
    }
}
