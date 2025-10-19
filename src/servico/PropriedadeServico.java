package servico;

import dados.Dados;
import modelo.Arquivo;
import modelo.Poligono;
import modelo.Propriedade;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import util.FormatadorUtil;
import util.PoligonoUtil;
import web.command.PropriedadeCommand;

import java.util.List;

@Service
public class PropriedadeServico {

    @Autowired
    private Dados dados;

    @Autowired
    private ProprietarioServico proprietarioServico;


    public Propriedade prepapararPropriedade(PropriedadeCommand command) {
        Propriedade propriedade;

        if (command.getId() != null) {
            propriedade = dados.buscarUnicoPorCampo(Propriedade.class, "id", command.getId());
        } else {
            propriedade = new Propriedade();
        }

        Proprietario proprietario = proprietarioServico.buscarPorCpf(command.getCpfProprietario());
        propriedade.setProprietario(proprietario);
        propriedade.setNome(command.getNome());

        Arquivo arquivo;
        if (command.getId() != null) {
            arquivo = propriedade.getArquivoPontos();
        } else {
            arquivo = new Arquivo();
        }

        if (command.getNomeArquivoPontos() != null && !command.getNomeArquivoPontos().isEmpty()) {
            arquivo.setNomeOriginal(command.getNomeArquivoPontos());
        } else {
            arquivo.setNomeOriginal(command.getNome() + ".csv");
        }

        arquivo.setTipoConteudo("text/plain");
        arquivo.setConteudo(command.getPontos().getBytes());
        propriedade.setArquivoPontos(arquivo);

        Poligono poligono = PoligonoUtil.criarPoligonoPorArquivo(arquivo);

        propriedade.setPoligono(poligono);
        propriedade.setCentroide(poligono.calcularCentroide());

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

    public void validarPoligono(PropriedadeCommand command) {
        String textoPontos = command.getPontos();
        if (textoPontos == null || textoPontos.trim().isEmpty()) {
            throw new IllegalArgumentException("Falha: As coordenadas são obrigatórias.");
        }

        Arquivo arquivoPontos = new Arquivo();
        arquivoPontos.setNomeOriginal(command.getNomeArquivoPontos());
        arquivoPontos.setTipoConteudo("text/plain");
        arquivoPontos.setConteudo(command.getPontos().getBytes());

        Poligono poligono = PoligonoUtil.criarPoligonoPorArquivo(arquivoPontos);
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
