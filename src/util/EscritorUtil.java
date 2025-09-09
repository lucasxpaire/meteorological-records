package util;

public class EscritorUtil {

    public static void escreverEmNovaLinha(String mensagem) {
        System.out.println(mensagem);
    }

    public static void escreverNaMesmaLinha(String mensagem) {
        System.out.print(mensagem);
    }

    public static void pularLinha() {
        System.out.println();
    }

    public static void exibirMensagemSeparada(String mensagem) {
        System.out.println();
        System.out.println(mensagem);
        System.out.println();
    }

}
