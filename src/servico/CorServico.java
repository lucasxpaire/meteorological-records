package servico;

import dados.Dados;
import modelo.Cor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class CorServico {

    @Autowired
    private Dados dados;

    private static final String CAMINHO_ARQUIVO_CORES = "/cores.properties";

    private static final Map<String, String> ESTADO_PARA_COR = Map.ofEntries(
            Map.entry("AC", "Verde"),
            Map.entry("AL", "Azul"),
            Map.entry("AP", "Amarelo"),
            Map.entry("AM", "Laranja"),
            Map.entry("BA", "Roxo"),
            Map.entry("CE", "Rosa"),
            Map.entry("DF", "Marrom"),
            Map.entry("ES", "Cinza"),
            Map.entry("GO", "Preto"),
            Map.entry("MA", "Branco"),
            Map.entry("MT", "Turquesa"),
            Map.entry("MS", "Vinho"),
            Map.entry("MG", "Dourado"),
            Map.entry("PA", "Prata"),
            Map.entry("PB", "Bege"),
            Map.entry("PR", "Oliva"),
            Map.entry("PE", "Lima"),
            Map.entry("PI", "Ciano"),
            Map.entry("RJ", "Magenta"),
            Map.entry("RN", "Salmão"),
            Map.entry("RS", "Vermelho"),
            Map.entry("RO", "Coral"),
            Map.entry("RR", "Lavanda"),
            Map.entry("SC", "Indigo"),
            Map.entry("SP", "Chocolate"),
            Map.entry("SE", "Aqua"),
            Map.entry("TO", "AzulMarinho")
    );

    @PostConstruct
    public void inicializarCores() {
        dados.iniciarTransacao();

        try {
            if (dados.existeAlgum(Cor.class)) {
                return;
            }
            carregarCoresDoProperties();
            dados.confirmarTransacao();
        } catch (Exception e) {
            dados.desfazerTransacao();
            throw e;
        }
    }

    private void carregarCoresDoProperties() {
        Properties properties = new Properties();
        try (InputStream valoresProperties = getClass().getResourceAsStream(CAMINHO_ARQUIVO_CORES)) {
            if (valoresProperties == null) {
                throw new RuntimeException("Arquivo de cores não encontrado: " + CAMINHO_ARQUIVO_CORES);
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
        String nomeCor = ESTADO_PARA_COR.get(estado);
        return dados.buscarUnicoPorCampo(Cor.class, "nome", nomeCor);
    }

    public List<Cor> listarTodos() {
        if (dados.existeAlgum(Cor.class)) {
            return dados.listarTodos(Cor.class);
        } else {
            throw new IllegalArgumentException("Falha: Não existe nenhuma cor.");
        }
    }

    private boolean validarCor(Cor cor) {
        return Cor.validarNome(cor.getNome()) && Cor.validarCodigoHexadecimal(cor.getCodigoHexadecimal());
    }
}
