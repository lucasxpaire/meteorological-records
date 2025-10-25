package servico;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstacaoMeteorologicaServico {

    private static final double METROS_PARA_GRAU = 111320.0;
    private static final int LIMITE_RAIO_BUSCA = 100000;

    private static final String URL_ESTACOES_AUTOMATICAS = "https://apitempo.inmet.gov.br/estacoes/T";
    private static final String URL_ESTACOES_MANUAIS = "https://apitempo.inmet.gov.br/estacoes/M";

    private static final int TEMPO_ATE_DESCONECTAR = 1000000;

    @Autowired
    private Dados dados;

    @Autowired
    private CorServico corServico;

    @PostConstruct
    public void inicializarEstacoesMeteorologicas() {
        if (listarTodas().isEmpty()) {
            List<String> urlsEstacoes = List.of(URL_ESTACOES_MANUAIS, URL_ESTACOES_AUTOMATICAS);
            for (String url : urlsEstacoes) {
               dados.iniciarTransacao();
               try {
                   popularBancoDeDadosComEstacoesMeteorologicas(url);
                   dados.confirmarTransacao();
               } catch (Exception e) {
                   dados.desfazerTransacao();
                   throw new RuntimeException("Falha: não foi possível inicializar estações da URL: " + url);
               }
            }
        }
    }

    private void popularBancoDeDadosComEstacoesMeteorologicas(String urlApiEstacoes) {
        try {
            JsonNode estacoesNode = obterDadosDaEstacao(urlApiEstacoes);
            for (JsonNode estacaoNode : estacoesNode) {
                EstacaoMeteorologica estacaoMeteorologica = new EstacaoMeteorologica();
                estacaoMeteorologica.definirDados(estacaoNode, corServico);
                salvar(estacaoMeteorologica);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível carregar estações meteorológicas da URL: " + urlApiEstacoes, e);
        }
    }

    public void salvar(EstacaoMeteorologica estacao) {
        if (validarEstacaoMeteorologica(estacao)) {
            dados.salvar(estacao);
        } else {
            throw new IllegalArgumentException("Dados da estação meteorológica são inválidos.");
        }
    }

    public List<EstacaoMeteorologica> buscarEstacoesRelevantes(Ponto ponto) {
        try {
            List<EstacaoMeteorologica> estacoesNoRaio = buscarEstacoesDentroDoRaio(ponto, calcularRaioDeBuscaEmGraus(ponto));
            if (estacoesNoRaio == null) {
                return null;
            }
            Map<Integer, List<EstacaoMeteorologica>> quadrantes = classificarEstacoesPorQuadrante(estacoesNoRaio, ponto);

            for (int quadrante = Ponto.PRIMEIRO_QUADRANTE; quadrante <= Ponto.QUARTO_QUADRANTE; quadrante++) {
                if (quadrantes.get(quadrante).isEmpty()) {
                    EstacaoMeteorologica maisProximaNoQuadrante = buscarEstacaoMaisProximaDoQuadrante(ponto, quadrante);
                    if (maisProximaNoQuadrante != null && !estacoesNoRaio.contains(maisProximaNoQuadrante)) {
                        estacoesNoRaio.add(maisProximaNoQuadrante);
                    }
                }
            }
            return estacoesNoRaio;
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível buscar as estações meteorológicas relevantes.");
        }
    }

    private Double calcularRaioDeBuscaEmGraus(Ponto ponto) {
        EstacaoMeteorologica estacaoMaisProxima = dados.buscarEstacaoMaisProximaComDados(ponto);
        double raioDeBuscaMetros = Math.min(ponto.distanciaAte(estacaoMaisProxima.getLocalizacao()), LIMITE_RAIO_BUSCA);
        return raioDeBuscaMetros / METROS_PARA_GRAU;
    }

    private List<EstacaoMeteorologica> buscarEstacoesDentroDoRaio(Ponto ponto, Double raioEmGraus) {
        if (ponto != null && raioEmGraus != null) {
            return dados.buscarEstacoesDentroDoRaio(ponto, raioEmGraus);
        } else {
            return null;
        }
    }

    private EstacaoMeteorologica buscarEstacaoMaisProximaDoQuadrante(Ponto ponto, int quadrante) {
        String condicaoQuadrante = switch (quadrante) {
            case Ponto.PRIMEIRO_QUADRANTE -> "e.localizacao.latitude >= :lat AND e.localizacao.longitude >= :lng";
            case Ponto.SEGUNDO_QUADRANTE -> "e.localizacao.latitude >= :lat AND e.localizacao.longitude <= :lng";
            case Ponto.TERCEIRO_QUADRANTE -> "e.localizacao.latitude <= :lat AND e.localizacao.longitude <= :lng";
            case Ponto.QUARTO_QUADRANTE -> "e.localizacao.latitude <= :lat AND e.localizacao.longitude >= :lng";
            default -> throw new IllegalArgumentException("Quadrante inválido: " + quadrante);
        };

        return dados.buscarEstacaoMaisProximaDoQuadrante(ponto, condicaoQuadrante);
    }

    public List<EstacaoMeteorologica> listarTodas() {
        return dados.listarTodos(EstacaoMeteorologica.class);
    }

    private Map<Integer, List<EstacaoMeteorologica>> classificarEstacoesPorQuadrante(List<EstacaoMeteorologica> estacoes, Ponto pontoReferencia) {
        Map<Integer, List<EstacaoMeteorologica>> quadrantes = new HashMap<>();
        for (int i = Ponto.PRIMEIRO_QUADRANTE; i <= Ponto.QUARTO_QUADRANTE; i++) {
            quadrantes.put(i, new ArrayList<>());
        }
        for (EstacaoMeteorologica estacao : estacoes) {
            int quadrante = estacao.getLocalizacao().obterQuadranteEmRelacaoA(pontoReferencia);
            quadrantes.get(quadrante).add(estacao);
        }
        return quadrantes;
    }

    public JsonNode obterDadosDaEstacao(String URLString) throws IOException {
        URL url = URI.create(URLString).toURL();
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");
        conexao.setConnectTimeout(TEMPO_ATE_DESCONECTAR);
        conexao.setReadTimeout(TEMPO_ATE_DESCONECTAR);

        try (InputStream dadosJson = conexao.getInputStream()) {
            ObjectMapper conversorJson = new ObjectMapper();
            return conversorJson.readTree(dadosJson);
        } catch (IOException e) {
            return null;
        } finally {
            conexao.disconnect();
        }
    }

    private boolean validarEstacaoMeteorologica(EstacaoMeteorologica estacao) {
        if (estacao == null) {
            return false;
        }
        return estacao.getCodigoEstacao() != null && estacao.getNome() != null && estacao.getLocalizacao() != null;
    }
}