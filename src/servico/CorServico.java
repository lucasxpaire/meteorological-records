package servico;

import dados.Dados;
import modelo.Cor;
import org.springframework.stereotype.Service;
import util.CorUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@Service
public class CorServico {

    private final Dados dados;

    public CorServico(Dados dados) {
        this.dados = dados;
        inicializarCores();
    }

    public void inicializarCores() {
        if (dados.existeAlgum(Cor.class)) {
            return;
        }

        dados.iniciarTransacao();
        try {
            carregarCoresDoProperties();
            dados.confirmarTransacao();
        } catch (Exception e) {
            dados.desfazerTransacao();
            throw new RuntimeException("Falha: não foi possível inicializar cores.");
        }

    }

    private void carregarCoresDoProperties() {
        Properties properties = new Properties();
        try (InputStream valoresProperties = getClass().getResourceAsStream(CorUtil.CAMINHO_ARQUIVO_CORES)) {
            if (valoresProperties == null) {
                throw new RuntimeException("Arquivo de cores não encontrado: " + CorUtil.CAMINHO_ARQUIVO_CORES);
            }
            properties.load(valoresProperties);

            for (String chave : properties.stringPropertyNames()) {
                String valor = properties.getProperty(chave);
                Cor novaCor = new Cor();
                novaCor.setNome(chave);
                novaCor.setCodigoHexadecimal(valor);
                salvar(novaCor);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível carregar cores do properties.");
        }
    }

    public void salvar(Cor cor) {
        if (validarCor(cor)) {
            dados.salvar(cor);
        } else {
            throw new IllegalArgumentException("Dados da cor são inválidos.");
        }
    }

    public Cor selecionarCorPorEstado(String estado) {
        String nomeCor = CorUtil.ESTADO_PARA_COR.get(estado);
        return dados.buscarUnicoPorCampo(Cor.class, "nome", nomeCor);
    }

    public List<Cor> listarTodos() {
        return dados.listarTodos(Cor.class);
    }

    private boolean validarCor(Cor cor) {
        return Cor.validarNome(cor.getNome()) && Cor.validarCodigoHexadecimal(cor.getCodigoHexadecimal());
    }
}
