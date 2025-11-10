package modelo;

import com.fasterxml.jackson.databind.JsonNode;
import servico.CorServico;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "ESTACAO_METEOROLOGICA")
public class EstacaoMeteorologica {

    private Long id;
    private String codigoEstacao;
    private Ponto localizacao;
    private String nome;
    private Cor cor;
    private String estado;
    private String situacao;
    private String tipoEstacao;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_ESTACAO_METEOROLOGICA")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "CODIGO_ESTACAO", nullable = false, unique = true)
    public String getCodigoEstacao() {
        return codigoEstacao;
    }

    public void setCodigoEstacao(String codigoEstacao) {
        this.codigoEstacao = codigoEstacao;
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ID_LOCALIZACAO", nullable = false, unique = true)
    public Ponto getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(Ponto localizacao) {
        this.localizacao = localizacao;
    }

    @Column(name = "NOME", nullable = false)
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @ManyToOne
    @JoinColumn(name = "ID_COR", nullable = false)
    public Cor getCor() {
        return cor;
    }

    public void setCor(Cor cor) {
        this.cor = cor;
    }

    @Column(name = "ESTADO", nullable = false)
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Column(name = "SITUACAO", nullable = false)
    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    @Column(name = "TIPO_ESTACAO", nullable = false)
    public String getTipoEstacao() {
        return tipoEstacao;
    }

    public void setTipoEstacao(String tipoEstacao) {
        this.tipoEstacao = tipoEstacao;
    }

    @Transient
    public void definirDados(JsonNode dadosEstacao, CorServico corServico) {
        setNome(dadosEstacao.get("DC_NOME").asText());
        setSituacao(dadosEstacao.get("CD_SITUACAO").asText());
        setTipoEstacao(dadosEstacao.get("TP_ESTACAO").asText());
        Ponto ponto = new Ponto(dadosEstacao.get("VL_LATITUDE").asDouble(), dadosEstacao.get("VL_LONGITUDE").asDouble());
        ponto.setFusoHorario(ponto.determinarFusoHorario());
        setLocalizacao(ponto);
        setCodigoEstacao(dadosEstacao.get("CD_ESTACAO").asText());
        setEstado(dadosEstacao.get("SG_ESTADO").asText());
        setCor(corServico.selecionarCorPorEstado(getEstado()));
    }

    @Transient
    public Set<LocalDateTime> obterDatasHorasExistentesNoHistoricoTemperaturas() {
        return this.getLocalizacao().getHistoricoRegistrosMeteorologicos().stream()
                .map(RegistroMeteorologico::getDataHora)
                .collect(Collectors.toSet());
    }

}
