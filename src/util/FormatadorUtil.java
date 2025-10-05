package util;

import modelo.Ponto;

import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static util.LeitorArquivoUtil.CABECALHO_POLIGONOS_CSV;
import static util.LeitorArquivoUtil.DUAS_COLUNAS;

public class FormatadorUtil {

    public static final DateTimeFormatter FORMATADOR_DATAHORA_PARA_EXIBICAO_MAPA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    public static final DateTimeFormatter FORMATADOR_DATA_PADRAO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final DateTimeFormatter FORMATADOR_DATA_HORA_PARA_EXIBICAO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    
    public static final DateTimeFormatter FORMATADOR_DATA_HORA_PARA_COMPARACAO_CSV = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static final DateTimeFormatter FORMATADOR_DATA_HORA_PARA_COMPARACAO_JSON = DateTimeFormatter.ofPattern("dd/MM/yyyy HH");

    public static String formatarTemperatura(Double temperatura) {
        return String.format("%.2f°C", temperatura).replace('.', ',');
    }

    public static Double StringParaDouble(String valor) {
        return Double.parseDouble(valor.trim().replace(",", "."));
    }

    public static Ponto StringParaPonto(String linha, int numeroLinha) {
        String[] partes = linha.split(";");
        if (partes.length != DUAS_COLUNAS) {
            throw new IllegalArgumentException("Linha " + numeroLinha + " do arquivo. Só deve existir duas colunas no formato: " + CABECALHO_POLIGONOS_CSV);
        }
        Ponto ponto = new Ponto();
        ponto.setLatitude(FormatadorUtil.StringParaDouble(partes[0]));
        ponto.setLongitude(FormatadorUtil.StringParaDouble(partes[1]));
        return ponto;
    }

    public static String formatarPontoDecimalParaVirgula(Double valor) {
        if (valor == null) {
            return " ";
        }
        return String.format("%.8f", valor).replace('.', ',');
    }

    public static String formatarString(String texto, String mascara) {
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }
        try {
            MaskFormatter mf = new MaskFormatter(mascara);
            mf.setValueContainsLiteralCharacters(false);
            return mf.valueToString(texto);
        } catch (ParseException ex) {
            return texto;
        }
    }

    public static String formatarCpf(String cpf) {
        return formatarString(cpf, "###.###.###-##");
    }

    public static String formatarTelefone(String telefone) {
        if (telefone != null && telefone.length() == 11) {
            return formatarString(telefone, "(##) #####-####");
        }

        return formatarString(telefone, "(##) ####-####");
    }

    public static String removerFormatacaoCpf(String cpf) {
        if (cpf != null && !cpf.trim().isEmpty()) {
            return cpf.replaceAll("\\D", "");
        }
        return " ";
    }

    public static String removerFormatacaoTelefone(String telefone) {
        if (telefone != null && !telefone.trim().isEmpty()) {
            return telefone.replaceAll("\\D", "");
        }
        return " ";
    }

    public static String formatarDataParaComparacao(LocalDateTime dataHora) {
        return dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static String formatarHoraParaComparacao(LocalDateTime dataHora) {
        return dataHora.format(DateTimeFormatter.ofPattern("HHmm"));
    }

    public static String reordenarStringDataParaFormatacaoBrasileira(String data) {
        String[] partes = data.split("/");
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }

    public static String removerSubPalavraUTC(String hora) {
        return hora.replace(" UTC", "").trim();
    }

    static String formatarCoordenadaParaExibicao(Double valor) {
        if (valor == null) {
            return "";
        }
        return String.format(Locale.US, "%.8f", valor).replace('.', ',');
    }
}
