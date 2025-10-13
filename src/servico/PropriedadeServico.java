package servico;

import dados.Dados;
import modelo.Poligono;
import modelo.Propriedade;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import util.FormatadorUtil;
import util.PoligonoUtil;
import web.CoordenadasMultiPartFile;
import web.command.PropriedadeCommand;

import java.io.IOException;
import java.util.List;

@Service
public class PropriedadeServico {

    @Autowired
    private Dados dados;

    @Autowired
    private ProprietarioServico proprietarioServico;

    public Propriedade prepapararPropriedade(PropriedadeCommand command) throws IOException {
        Propriedade propriedade;

        if (command.getId() != null) {
            propriedade = dados.buscarUnicoPorCampo(Propriedade.class, "id", command.getId());
        } else {
            propriedade = new Propriedade();
        }

        Proprietario proprietario;
        if (command.getIdProprietario() != null) {
            proprietario = proprietarioServico.buscarPorId(command.getIdProprietario());
        } else {
            proprietario = proprietarioServico.buscarPorCpf(command.getCpfProprietario());
        }
        propriedade.setProprietario(proprietario);

        propriedade.setNome(command.getNome());
        propriedade.setTipoEntradaPoligono(command.getTipoEntradaPoligono());

        MultipartFile arquivoPoligono = obterArquivoDoCommand(command);
        Poligono poligono = PoligonoUtil.criarPoligonoPorArquivo(arquivoPoligono);

        propriedade.setTipoEntradaPoligono(command.getTipoEntradaPoligono());
        if (arquivoPoligono != null) {
            propriedade.setArquivoPoligonos(arquivoPoligono.getBytes());
        }

        propriedade.setPoligono(poligono);
        propriedade.setCentroide(poligono.calcularCentroide());

        propriedade.setPoligono(poligono);

        return propriedade;
    }

    public void salvar(Propriedade propriedade) {
        if (validarPropriedade(propriedade)) {
            dados.salvar(propriedade);
        } else {
            throw new IllegalArgumentException("Falha: Dados da propriedade são inválidos.");
        }
    }

    public Propriedade buscarPorId(Long id) {
        if (existeAlgumaPropriedade()) {
            return dados.buscarUnicoPorCampo(Propriedade.class, "id", id);
        } else {
            throw new IllegalArgumentException("Falha: Não existe nenhuma propriedade");
        }
    }

    public List<Propriedade> buscarPorCpfDoProprietario(String cpfBusca) {
        String cpfSemFormatacao = FormatadorUtil.removerFormatacaoCpf(cpfBusca);
        if (Proprietario.validarTamanhoCpf(cpfSemFormatacao)) {
            return dados.buscarListaPorCampo(Propriedade.class, "proprietario.cpf", cpfSemFormatacao);
        } else {
            throw new IllegalArgumentException("Falha: CPF de busca é inválido.");
        }
    }

    public List<Propriedade> buscarPorNome(String nome) {
        if (Propriedade.validarNome(nome)) {
            return dados.buscarListaPorCampo(Propriedade.class, "nome", nome);
        } else {
            throw new IllegalArgumentException("Falha: Nome de busca é inválido.");
        }
    }

    public Propriedade buscarMaisRecente() {
        return dados.buscarMaisRecente(Propriedade.class);
    }

    public List<Propriedade> listarTodas() {
        return dados.listarTodos(Propriedade.class);
    }

    private MultipartFile obterArquivoDoCommand(PropriedadeCommand command) {
        if (PoligonoUtil.TIPO_MANUAL.equals(command.getTipoEntradaPoligono())) {
            if (command.getCoordenadasPorInsercaoManual() != null && !command.getCoordenadasPorInsercaoManual().trim().isEmpty()) {
                return new CoordenadasMultiPartFile(command.getCoordenadasPorInsercaoManual(), "coordenadas", "coordenadas.txt", "text/plain");
            }
        } else if (PoligonoUtil.TIPO_ARQUIVO.equals(command.getTipoEntradaPoligono())) {
            if (command.getCoordenadasPorArquivo() != null && !command.getCoordenadasPorArquivo().isEmpty()) {
                return command.getCoordenadasPorArquivo();
            }
        }
        return null;
    }

    public void validarPoligono(PropriedadeCommand command) {
        MultipartFile arquivoRecebido = obterArquivoDoCommand(command);

        if (arquivoRecebido == null || arquivoRecebido.isEmpty()) {
            if (PoligonoUtil.TIPO_MANUAL.equals(command.getTipoEntradaPoligono())) {
                throw new IllegalArgumentException("Falha: As coordenadas são obrigatórias para inserção manual.");
            } else {
                throw new IllegalArgumentException("Falha: O arquivo de coordenadas é obrigatório.");
            }
        }

        Poligono poligono = PoligonoUtil.criarPoligonoPorArquivo(arquivoRecebido);
        if (poligono.getPontos().size() < Poligono.QUANTIDADE_MINIMA_DE_PONTOS) {
            throw new IllegalArgumentException("Falha: O polígono deve possuir pelo menos 3 pontos.");
        }
        if (poligono.possuiAutoIntersecao()) {
            throw new IllegalArgumentException("Falha: O polígono possui auto-interseção.");
        }
    }

    public void deletar(Propriedade propriedade) {
        if (validarPropriedade(propriedade)) {
            propriedade.getCentroide().getHistoricoTemperaturas().clear();
            propriedade.getPoligono().getPontos().clear();
            dados.deletar(propriedade);
        } else {
            throw new IllegalArgumentException("Falha: Dados da propriedade são inválidos.");
        }
    }

    public boolean existeAlgumaPropriedade() {
        return dados.existeAlgum(Propriedade.class);
    }

    public boolean existePropriedadesNesseCpf(String cpfBusca) {
        String cpfSemFormatacao = FormatadorUtil.removerFormatacaoCpf(cpfBusca);
        return dados.existeAlgumComEsseCampo(Propriedade.class, "proprietario.cpf", cpfSemFormatacao);
    }

    public boolean existePropriedadesNesseNome(String nomeBusca) {
        return dados.existeAlgumComEsseCampo(Propriedade.class, "nome", nomeBusca);
    }

    private boolean validarPropriedade(Propriedade propriedade) {
        return propriedade != null && propriedade.getNome() != null && propriedade.getPoligono() != null && propriedade.getProprietario() != null;
    }

}
