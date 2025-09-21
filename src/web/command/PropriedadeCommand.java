package web.command;

import modelo.Propriedade;
import org.springframework.web.multipart.MultipartFile;

public class PropriedadeCommand {

    Propriedade propriedade = new Propriedade();

    private Long proprietarioId;

    private String tipoEntradaPoligono;
    private String coordenadasPorInsercaoManual;
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

    public String getCoordenadasPorInsercaoManual() {
        return coordenadasPorInsercaoManual;
    }

    public void setCoordenadasPorInsercaoManual(String coordenadasPorInsercaoManual) {
        this.coordenadasPorInsercaoManual = coordenadasPorInsercaoManual;
    }

    public MultipartFile getCoordenadasPorArquivo() {
        return coordenadasPorArquivo;
    }

    public void setCoordenadasPorArquivo(MultipartFile coordenadasPorArquivo) {
        this.coordenadasPorArquivo = coordenadasPorArquivo;
    }

}
