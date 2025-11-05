package util;

import junit.framework.TestCase;

public class FormatadorUtilTest extends TestCase {

    public void testar_formatacao_data_hora() {
        String data1 = "01-03-2025";
        String hora1 = "0000 UTC";

        String data2 = "01/01/2025";
        String hora2 = "00:00";

        System.out.println(FormatadorUtil.formatarECombinarDataHora(data1, hora1));
        System.out.println(FormatadorUtil.formatarECombinarDataHora(data2, hora2));
    }

}