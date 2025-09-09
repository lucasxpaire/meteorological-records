package util;

import modelo.Poligono;
import modelo.Ponto;
import modelo.Proprietario;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class LeitorUtil {

    private static final int DUAS_COLUNAS = 2;

    private static final Set<String> RESPOSTAS_SALVAR = Set.of("salvar", "s");
    private static final Set<String> RESPOSTAS_VOLTAR = Set.of("voltar", "v");

    private static final Scanner scanner = new Scanner(System.in);

    public static double lerDouble(String mensagem) {
        while (true) {
            try {
                EscritorUtil.escreverNaMesmaLinha(mensagem);
                return FormatadorUtil.StringParaDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                EscritorUtil.exibirMensagemSeparada("Falha: Digite uma opção válida.");
            }
        }
    }

    public static Ponto lerPonto() {
        while (true) {
            double latitude = lerDouble("Digite a latitude: ");

            if (Ponto.validarLatitude(latitude))  {
                double longitude = lerDouble("Digite a longitude: ");

                if (Ponto.validarLongitude(longitude)) {
                    return new Ponto(latitude, longitude);
                } else {
                    EscritorUtil.exibirMensagemSeparada("Falha: longitude inválida. Digite um valor válido entre o intervalo -180 a 180.");
                }
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: latitude inválida. Digite um valor válido entre o intervalo -90 a 90.");
            }
        }

    }

    public static LocalDate lerData(String mensagem) {
        while (true) {
            try {
                String entrada = lerString(mensagem);
                LocalDate dataSelecionada = LocalDate.parse(entrada, FormatadorUtil.FORMATADOR_DATA_PADRAO);
                if (dataSelecionada.isBefore(LocalDate.now())) {
                    EscritorUtil.exibirMensagemSeparada("Falha: A data para previsão deve ser igual ou posterior ao momento atual.");
                } else {
                    return dataSelecionada;
                }
            } catch (DateTimeParseException e) {
                EscritorUtil.exibirMensagemSeparada("Falha: Formato de data inválido. Use o formato dd/MM/yyyy.");
            }
        }
    }

    public static String lerString(String informacaoPedida) {
        EscritorUtil.escreverNaMesmaLinha(informacaoPedida);
        return scanner.nextLine();
    }

    public static int lerInteiroComIntervalo(String informacaoPedida, int minimo, int maximo) {
        while (true) {
            try {
                String entrada = lerString(informacaoPedida);
                int opcao = Integer.parseInt(entrada);
                if (opcao >= minimo && opcao <= maximo) {
                    return opcao;
                } else {
                    EscritorUtil.exibirMensagemSeparada("Falha: Digite um número entre " + minimo + " e " + maximo + ".");
                }
            } catch (NumberFormatException e) {
                EscritorUtil.exibirMensagemSeparada("Falha: Digite uma opção válida.");
            }
        }
    }

    public static String lerConfirmacao(String mensagem) {
        while (true) {
            String resposta = LeitorUtil.lerString(mensagem);
            if (resposta.equalsIgnoreCase("s") || resposta.equalsIgnoreCase("n")) {
                return resposta;
            }
            EscritorUtil.exibirMensagemSeparada("Falha: Digite apenas 's' ou 'n'.");
        }
    }

    public static String lerCpf(String mensagem) {
        while (true) {
            String entradaUsuario = lerString(mensagem);
            String cpfSemFormatacao = FormatadorUtil.removerFormatacaoCpf(entradaUsuario);

            if (!Proprietario.validarTamanhoCpf(cpfSemFormatacao)) {
                EscritorUtil.exibirMensagemSeparada("Falha: CPF não possui 11 digitos. Tente novamente.");
                continue;
            }
            return cpfSemFormatacao;
        }
    }

    public static String lerNome(String mensagem) {
        while (true) {
            String nome = lerString(mensagem);
            if (nome != null && !nome.trim().isEmpty()) {
                return nome;
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Nome inválido. Tente novamente.");
            }
        }
    }

    public static String lerTelefone(String mensagem) {
        while (true) {
            String entradaUsuario = lerString(mensagem);
            String telefoneSemFormatacao = FormatadorUtil.removerFormatacaoTelefone(entradaUsuario);
            if (Proprietario.validarTamanhoTelefone(telefoneSemFormatacao)) {
                return telefoneSemFormatacao;
            } else {
                EscritorUtil.exibirMensagemSeparada("Falha: Formato de telefone inválido.");
            }
        }
    }

    public static List<Ponto> lerListaManual() {
        List<Ponto> listaPontos = new ArrayList<>();
        EscritorUtil.escreverEmNovaLinha("Digite as coordenadas separadas por espaço (lat long), \"salvar\" ou \"voltar\":");

        while (true) {
            String entrada = LeitorUtil.lerString("V" + (listaPontos.size() + 1) + ": ").toLowerCase();

            if (RESPOSTAS_VOLTAR.contains(entrada)) {
                return null;
            }

            if (RESPOSTAS_SALVAR.contains(entrada)) {
                if (listaPontos.size() < Poligono.QUANTIDADE_MINIMA_DE_PONTOS) {
                    EscritorUtil.escreverEmNovaLinha("Falha: Um polígono precisa de pelo menos 3 vértices.");
                    continue;
                }
                return listaPontos;
            }

            String[] partes = entrada.split(" ");
            if (partes.length != DUAS_COLUNAS) {
                EscritorUtil.escreverEmNovaLinha("Falha: Digite dois números separados por espaço (lat long).");
                continue;
            }

            try {
                Double latitude = FormatadorUtil.StringParaDouble(partes[0].trim());
                Double longitude = FormatadorUtil.StringParaDouble(partes[1].trim());

                if (Ponto.validarLatitude(latitude))  {
                    if (Ponto.validarLongitude(longitude)) {
                        Ponto ponto = new Ponto();
                        ponto.setLatitude(latitude);
                        ponto.setLongitude(longitude);
                        listaPontos.add(ponto);
                    } else {
                        EscritorUtil.exibirMensagemSeparada("Falha: longitude inválida. Digite um valor válido entre o intervalo -180 a 180.");
                    }
                } else {
                    EscritorUtil.exibirMensagemSeparada("Falha: latitude inválida. Digite um valor válido entre o intervalo -90 a 90.");
                }
            } catch (NumberFormatException e) {
                EscritorUtil.escreverEmNovaLinha("Falha: Digite números válidos para latitude e longitude.");
            } catch (IllegalArgumentException e) {
                EscritorUtil.escreverEmNovaLinha("Falha: " + e.getMessage());
            }
        }
    }

}
