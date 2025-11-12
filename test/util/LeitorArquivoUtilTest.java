package util;

import dados.Dados;
import modelo.EstacaoMeteorologica;
import modelo.RegistroMeteorologico;
import org.junit.Test;
import servico.EstacaoMeteorologicaServico;

import java.time.LocalDateTime;
import java.util.List;

public class LeitorArquivoUtilTest {

    private final Dados dados = new Dados();
    private final EstacaoMeteorologicaServico estacaoMeteorologicaServico = new EstacaoMeteorologicaServico(dados);

    @Test
    public void lerRegistrosAnuaisDosCsv() {
        LocalDateTime inicio = LocalDateTime.of(2024, 2, 1, 5, 0);
        LocalDateTime fim = LocalDateTime.of(2022,2, 1, 5, 0);

        String dataHoraInicio = FormatadorUtil.formatarDataParaComparacao(inicio);
        String dataHoraFim = FormatadorUtil.formatarDataParaComparacao(fim);

        List<String> caminhosArquivos = LeitorArquivoUtil.obterCaminhosDeArquivosCsvDaEstacao("A803");
        System.out.println(caminhosArquivos);

        dados.iniciarTransacao();
        List<RegistroMeteorologico> registros = LeitorArquivoUtil.lerRegistrosAnuaisDosCsv(dataHoraInicio, dataHoraFim, dados.buscarUnicoPorCampo(EstacaoMeteorologica.class, "codigoEstacao", "A803"));
        dados.confirmarTransacao();

        for (RegistroMeteorologico registro : registros) {
            System.out.println("Registro: " + registro.getDataHora().format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_COMPARACAO_CSV) + " - Temperatura: " + registro.getTemperaturaReal());
        }
    }
}