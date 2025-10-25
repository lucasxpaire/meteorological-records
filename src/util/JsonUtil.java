package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    public static String converterObjetoParaJson(Object o) throws JsonProcessingException {
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        return conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }
}
