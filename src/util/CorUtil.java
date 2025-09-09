package util;

import modelo.Cor;

import java.util.List;
import java.util.Map;

public class CorUtil {

    public static final String CAMINHO_ARQUIVO_CORES = "/cores.properties";

    public static final Map<String, String> ESTADO_PARA_COR = Map.ofEntries(
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

    public static void exibirCoresDisponiveis(List<Cor> coresDisponiveis) {
        if (coresDisponiveis.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma cor disponível");
        }

        EscritorUtil.escreverEmNovaLinha("Cores disponíveis:");
        for (int i = 0; i < coresDisponiveis.size(); i++) {
            EscritorUtil.escreverEmNovaLinha((i + 1) + ". " + coresDisponiveis.get(i).getNome());
        }
    }

    public static Cor obterCorPorEscolha(List<Cor> coresDisponiveis, int escolha) {
        if (coresDisponiveis.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma cor disponível");
        }

        if (escolha < 1 || escolha > coresDisponiveis.size()) {
            throw new IllegalArgumentException("Escolha inválida. Deve ser entre 1 e " + coresDisponiveis.size());
        }

        return coresDisponiveis.get(escolha - 1);
    }
}
