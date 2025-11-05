package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormatadorUtil {

    public static final DateTimeFormatter FORMATADOR_DATA_HORA_PARA_EXIBICAO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    
    public static final DateTimeFormatter FORMATADOR_DATA_HORA_PARA_COMPARACAO_CSV = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter FORMATADOR_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static final DateTimeFormatter FORMATADOR_DATA_HORA_PARA_COMPARACAO_JSON = DateTimeFormatter.ofPattern("dd/MM/yyyy HH");

    public static final String MASCARA_CPF = "###.###.###-##";
    public static final String MASCARA_TELEFONE = "(##) #####-####";

    public static final String REGEX_QUALQUER_NAO_DIGITO_NUMERICO = "\\D";
    public static final String REGEX_APENAS_DIGITOS_NUMERICOS = "^[0-9]+$";
    public static final String REGEX_CODIGO_HEXADECIMAL_VALIDO = "^#[0-9A-Fa-f]{3}([0-9A-Fa-f]{3})?$";

    public static final int QUANTIDADE_DIGITOS_VALIDOS_CPF = 11;
    public static final int QUANTIDADE_DIGITOS_VALIDOS_TELEFONE = 11;

    public static final String STRING_VAZIA = "";
    public static final String INDISPONIVEL = "Indisponível";
    public static final char PONTO = '.';
    public static final char VIRGULA = ',';
    public static final String PONTO_E_VIRGULA = ";";
    public static final String DOIS_PONTOS = ":";
    public static final String ESPACO_EM_BRANCO = " ";

    public static String formatarTemperatura(Double temperatura) {
        return String.format("%.2f°C", temperatura).replace(PONTO, VIRGULA);
    }

    public static Double converterStringParaDouble(String valor) {
       try {
           return Double.parseDouble(valor.trim().replace(VIRGULA, PONTO));
       } catch (NumberFormatException e) {
           return null;
       }
    }

    public static String formatarPontoDecimalParaVirgula(Double valor) {
        if (valor == null) {
            return STRING_VAZIA;
        }
        return String.format("%.8f", valor).replace(PONTO, VIRGULA);
    }

    public static String formatarString(String texto, String mascaraFormatacao) {
        if (texto == null || texto.trim().isEmpty()) {
            return STRING_VAZIA;
        }
        try {
            MaskFormatter formatador = new MaskFormatter(mascaraFormatacao);
            formatador.setValueContainsLiteralCharacters(false);
            return formatador.valueToString(texto);
        } catch (ParseException e) {
            return texto;
        }
    }

    public static String formatarCpf(String cpf) {
        return formatarString(cpf, MASCARA_CPF);
    }

    public static String formatarTelefone(String telefone) {
        return formatarString(telefone, MASCARA_TELEFONE);
    }

    public static String removerFormatacaoCpf(String cpf) {
        if (cpf != null && !cpf.trim().isEmpty()) {
            return cpf.replaceAll(REGEX_QUALQUER_NAO_DIGITO_NUMERICO, STRING_VAZIA);
        }
        return STRING_VAZIA;
    }

    public static String removerFormatacaoTelefone(String telefone) {
        if (telefone != null && !telefone.trim().isEmpty()) {
            return telefone.replaceAll(REGEX_QUALQUER_NAO_DIGITO_NUMERICO, STRING_VAZIA);
        }
        return STRING_VAZIA;
    }

    public static String formatarDataParaComparacao(LocalDateTime dataHora) {
        return dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static String formatarHoraParaComparacao(LocalDateTime dataHora) {
        return dataHora.format(DateTimeFormatter.ofPattern("HHmm"));
    }

    public static String formatarParaDataBrasileira(String data) {
        String[] partes = data.split("/");
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }

    public static String converterObjetoParaJson(Object o) throws JsonProcessingException {
        ObjectMapper conversorJson = new ObjectMapper();
        conversorJson.findAndRegisterModules();
        return conversorJson.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }

    public static String padronizarSeparadorData(String data) {
        if (data.contains("-")) {
            return data.replace('-', '/').trim();
        } else {
            return data.trim();
        }
    }

    public static String padronizarSeparadorHora(String hora) {
        String horaPadronizada = hora.trim();
        if (horaPadronizada.contains("UTC")) {
            horaPadronizada = horaPadronizada.replace(" UTC", "");
        }

        if (!horaPadronizada.contains(":")) {
            horaPadronizada = horaPadronizada.substring(0, 2) + DOIS_PONTOS + horaPadronizada.substring(2, 4);
        }

        return horaPadronizada;
    }

    public static String removerSubPalavraUTC(String hora) {
        return hora.replace(" UTC", "").trim();
    }

    public static LocalDateTime formatarECombinarDataHora(String data, String hora) {
        String dataPadronizada = padronizarSeparadorData(formatarParaDataBrasileira(data));
        String horaPadronizada = padronizarSeparadorHora(hora);

        return LocalDateTime.parse(dataPadronizada + ESPACO_EM_BRANCO + horaPadronizada, FORMATADOR_DATA_HORA_PARA_COMPARACAO_CSV);
    }
}
