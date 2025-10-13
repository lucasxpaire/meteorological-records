package web.command;

public class ControleMapaCommand {

    private Integer opcaoSelecionada;
    private String cpfBusca;
    private String nomeBusca;

    public Integer getOpcaoSelecionada() {
        return opcaoSelecionada;
    }

    public void setOpcaoSelecionada(Integer opcaoSelecionada) {
        this.opcaoSelecionada = opcaoSelecionada;
    }

    public String getCpfBusca() {
        return cpfBusca;
    }

    public void setCpfBusca(String cpfBusca) {
        this.cpfBusca = cpfBusca;
    }

    public String getNomeBusca() {
        return nomeBusca;
    }

    public void setNomeBusca(String nomeBusca) {
        this.nomeBusca = nomeBusca;
    }

}