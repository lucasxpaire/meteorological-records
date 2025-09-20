package web.command;

import modelo.Ponto;
import modelo.Propriedade;
import org.springframework.web.multipart.MultipartFile;

public class PropriedadeCommand {

    Propriedade propriedade = new Propriedade();
    Ponto centroide = new Ponto();

    private Long proprietarioId;
    private String tipoEntradaPoligono;
    private String coordenasPorInsercaoManual;
    private MultipartFile coordenadasPorArquivo;

    public Propriedade getPropriedade() {
        return propriedade;
    }

    public void setPropriedade(Propriedade propriedade) {
        this.propriedade = propriedade;
    }

    public Long getId() {
        return propriedade.getId();
    }

    public void setId(Long id) {
        propriedade.setId(id);
    }

    public String getNome() {
        return propriedade.getNome();
    }

    public void setNome(String nome) {
        propriedade.setNome(nome);
    }

    public Long getProprietarioId() {
        return proprietarioId;
    }

    public void setProprietarioId(Long proprietarioId) {
        this.proprietarioId = proprietarioId;
    }

    public String getTipoEntradaPoligono() {
        return tipoEntradaPoligono;
    }

    public void setTipoEntradaPoligono(String tipoEntradaPoligono) {
        this.tipoEntradaPoligono = tipoEntradaPoligono;
    }

    public String getCoordenasPorInsercaoManual() {
        return coordenasPorInsercaoManual;
    }

    public void setCoordenasPorInsercaoManual(String coordenasPorInsercaoManual) {
        this.coordenasPorInsercaoManual = coordenasPorInsercaoManual;
    }

    public MultipartFile getCoordenadasPorArquivo() {
        return coordenadasPorArquivo;
    }

    public void setCoordenadasPorArquivo(MultipartFile coordenadasPorArquivo) {
        this.coordenadasPorArquivo = coordenadasPorArquivo;
    }

}
