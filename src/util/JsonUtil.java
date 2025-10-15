package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class JsonUtil {

    public static final String URL_ESTACOES_AUTOMATICAS = "https://apitempo.inmet.gov.br/estacoes/T";
    public static final String URL_ESTACOES_MANUAIS = "https://apitempo.inmet.gov.br/estacoes/M";

    public static final String URL_TEMPERATURAS = "https://megatecnologia.com.br/silas.json?chave=A5AC5-6AA87-69587-2B9B2-A7BE1-C99F9-21&estacao=";
    public static final String URL_TEMPERATURAS2 = "http://192.168.1.2:8081/controle/silas.json?chave=A5AC5-6AA87-69587-2B9B2-A7BE1-C99F9-21&estacao=";

    public static JsonNode obterDadosDoJson(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");
        conexao.setConnectTimeout(1000000);
        conexao.setReadTimeout(1000000);

        try (InputStream dadosJson = conexao.getInputStream()) {
            ObjectMapper conversorJson = new ObjectMapper();
            return conversorJson.readTree(dadosJson);
        } finally {
            conexao.disconnect();
        }
    }

    public static String converterObjetoParaJson(Object o) throws JsonProcessingException {
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        return conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }
}
