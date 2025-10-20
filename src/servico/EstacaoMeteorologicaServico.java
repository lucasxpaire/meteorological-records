package servico;

import com.fasterxml.jackson.databind.JsonNode;
import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import util.*;

import javax.annotation.PostConstruct;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstacaoMeteorologicaServico {

    private static final double METROS_PARA_GRAU = 111320.0;
    private static final int LIMITE_RAIO_BUSCA = 100000;

    @Autowired
    private Dados dados;

    @Autowired
    private CorServico corServico;

    @PostConstruct
    public void inicializarEstacoesMeteorologicas() {
        if (listarTodas().isEmpty()) {
            List<String> urlsEstacoes = List.of(JsonUtil.URL_ESTACOES_MANUAIS, JsonUtil.URL_ESTACOES_AUTOMATICAS);
            for (String url : urlsEstacoes) {
               dados.iniciarTransacao();
               try {
                   preencherBancoComEstacoesDaApi(url);
                   dados.confirmarTransacao();
               } catch (Exception e) {
                   dados.desfazerTransacao();
                   throw new RuntimeException("Falha: não foi possível inicializar estações da URL: " + url);
               }
            }
        }
    }

    private void preencherBancoComEstacoesDaApi(String urlApiEstacoes) {
        try {
            JsonNode estacoesNode = JsonUtil.obterDadosDoJson(urlApiEstacoes);
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

    public Double calcularRaioDeBuscaEmGraus(Ponto ponto) {
        EstacaoMeteorologica estacaoMaisProxima = dados.buscarEstacaoMaisProximaComDados(ponto);
        double raioDeBuscaMetros = Math.min(ponto.distanciaAte(estacaoMaisProxima.getLocalizacao()), LIMITE_RAIO_BUSCA);
        return raioDeBuscaMetros / METROS_PARA_GRAU;
    }

    public List<EstacaoMeteorologica> buscarEstacoesRelevantes(Ponto ponto) {
        try {
            List<EstacaoMeteorologica> estacoesNoRaio = buscarEstacoesDentroDoRaio(ponto, calcularRaioDeBuscaEmGraus(ponto));
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

    public List<EstacaoMeteorologica> buscarEstacoesDentroDoRaio(Ponto ponto, Double raioEmGraus) {
        return dados.buscarEstacoesDentroDoRaio(ponto, raioEmGraus);
    }

    public EstacaoMeteorologica buscarEstacaoMaisProximaDoQuadrante(Ponto ponto, int quadrante) {
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

    private boolean validarEstacaoMeteorologica(EstacaoMeteorologica estacao) {
        if (estacao == null) {
            return false;
        }
        return estacao.getCodigoEstacao() != null && estacao.getNome() != null && estacao.getLocalizacao() != null;
    }
}