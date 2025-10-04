package web.command;

import modelo.Propriedade;
import org.springframework.web.multipart.MultipartFile;
import util.FormatadorUtil;

public class PropriedadeCommand {

    Propriedade propriedade = new Propriedade();

    private Long idProprietario;
    private String cpfProprietario;

    private String paginaOrigemRequisicao;

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

    public Long getIdProprietario() {
        return idProprietario;
    }

    public void setIdProprietario(Long idProprietario) {
        this.idProprietario = idProprietario;
    }

    public String getCpfProprietario() {
        return cpfProprietario;
    }

    public void setCpfProprietario(String cpfProprietario) {
        this.cpfProprietario = FormatadorUtil.removerFormatacaoCpf(cpfProprietario);
    }

    public String getPaginaOrigemRequisicao() {
        return paginaOrigemRequisicao;
    }

    public void setPaginaOrigemRequisicao(String paginaOrigemRequisicao) {
        this.paginaOrigemRequisicao = paginaOrigemRequisicao;
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
