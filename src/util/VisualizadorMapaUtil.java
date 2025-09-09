package util;

import modelo.*;
import servico.EstacaoMeteorologicaServico;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VisualizadorMapaUtil {

    private static final String NOME_ARQUIVO_TEMPLATE = "template_propriedades.html";
    public static final String NENHUMA_TEMPERATURA = "Nenhuma Temperatura";
    public static final String STRING_VAZIA = "";
    public static final String GRAUS_CELSIUS = "°C";

    public static void visualizarPropriedadesNoMapa(Collection<Propriedade> propriedades, EstacaoMeteorologicaServico estacaoServico) {
        if (propriedades == null || propriedades.isEmpty()) {
            EscritorUtil.exibirMensagemSeparada("Nenhuma propriedade para visualizar.");
            return;
        }
        try {
            String templateHtml = lerArquivoTemplate();
            List<EstacaoMeteorologica> todasEstacoes = estacaoServico.listarTodos();

            String coordenadasParaLimites = gerarCoordenadasParaLimitesVisuais(propriedades, List.of());
            String marcadoresEstacoes = gerarMarcadoresParaEstacoes(todasEstacoes);
            String elementosPropriedade  = gerarComponentesHtmlParaPropriedades(propriedades, estacaoServico);

            String htmlFinal = templateHtml
                    .replace("{{LIMITES_VISUALIZACAO}}", coordenadasParaLimites )
                    .replace("{{ESTACOES_METEOROLOGICAS}}", marcadoresEstacoes)
                    .replace("{{PROPRIEDADE}}", elementosPropriedade )
                    .replace("{{PONTOS_PREVISAO}}", "");

            abrirHtmlNoNavegador(htmlFinal);
        } catch (Exception e) {
            EscritorUtil.escreverEmNovaLinha("Falha ao gerar o mapa: " + e.getMessage());
        }
    }

    public static void visualizarPrevisoesNoMapa(List<Temperatura> previsoes) {
        if (previsoes == null || previsoes.isEmpty()) {
            EscritorUtil.exibirMensagemSeparada("Nenhuma previsão para visualizar.");
            return;
        }
        try {
            String templateHtml = lerArquivoTemplate();

            String coordenadasParaLimites = gerarCoordenadasParaLimitesVisuais(List.of(), previsoes);
            String marcadoresPrevisoes = gerarMarcadoresParaPontosDePrevisao(previsoes);

            String htmlFinal = templateHtml
                    .replace("{{LIMITES_VISUALIZACAO}}", coordenadasParaLimites)
                    .replace("{{PONTOS_PREVISAO}}", marcadoresPrevisoes)
                    .replace("{{ESTACOES_METEOROLOGICAS}}", "")
                    .replace("{{PROPRIEDADE}}", "");

            abrirHtmlNoNavegador(htmlFinal);
        } catch (Exception e) {
            EscritorUtil.escreverEmNovaLinha("Falha ao gerar o mapa de previsões: " + e.getMessage());
        }
    }

    private static String gerarComponentesHtmlParaPropriedades(Collection<Propriedade> propriedades, EstacaoMeteorologicaServico estacaoServico) {
        return propriedades.stream()
                .map(propriedade -> gerarComponentesHtmlParaPropriedadeUnica(propriedade, estacaoServico))
                .collect(Collectors.joining("\n"));
    }

    private static String gerarComponentesHtmlParaPropriedadeUnica(Propriedade propriedade, EstacaoMeteorologicaServico estacaoServico) {
        String poligono = gerarPoligonoParaPropriedade(propriedade);
        String marcador = gerarMarcadorParaCentroide(propriedade);
        EstacaoMeteorologica estacaoMaisProxima = estacaoServico.buscarEstacaoMaisProximaComDados(propriedade.getCentroide());
        String circulo = gerarCirculoDeRelevancia(propriedade, estacaoMaisProxima);

        return String.join("\n", poligono, marcador, circulo);
    }

    private static String gerarPoligonoParaPropriedade(Propriedade propriedade) {
        String descricaoPropriedade = gerarDescricaoHtmlParaPropriedade(propriedade);
        String vertices = gerarVerticesDoPoligono(propriedade.getPoligono());
        String cor = propriedade.getProprietario().getCor().getCodigoHexadecimal();
        String linhasTracejadas = gerarLinhasTracejadasParaPropriedade(propriedade);
        long id = propriedade.getId();

        return String.format(Locale.US, """
            {
                const infoWindow_propriedade_%d = new google.maps.InfoWindow({
                    content: '%s'
                });
                const poligono_propriedade_%d = new google.maps.Polygon({
                    paths: [
                        %s
                    ],
                    strokeColor: '%s',
                    strokeOpacity: 0.8,
                    strokeWeight: 2,
                    fillColor: '%s',
                    fillOpacity: 0.15
                });
                poligono_propriedade_%d.setMap(map);
                poligono_propriedade_%d.addListener('click', (event) => {
                    infoWindow_propriedade_%d.setPosition(event.latLng);
                    infoWindow_propriedade_%d.open(map);
                });
                %s
            }
            """, id, descricaoPropriedade, id, vertices, cor, cor, id, id, id, id, linhasTracejadas);
    }

    private static String gerarMarcadorParaCentroide(Propriedade propriedade) {
        Ponto centroide = propriedade.getCentroide();
        String latitudeFormatada = FormatadorUtil.formatarCoordenadaParaExibicao(centroide.getLatitude());
        String longitudeFormatada = FormatadorUtil.formatarCoordenadaParaExibicao(centroide.getLongitude());
        String descricao = String.format("<h4>Centroide</h4>Latitude: %s<br>Longitude: %s", latitudeFormatada, longitudeFormatada);
        long id = propriedade.getId();

        return String.format(Locale.US, """
            {
                const marcador_centroide_%d = new google.maps.Marker({
                    position: {lat: %.8f, lng: %.8f},
                    map: map,
                    title: 'Centroide',
                    icon: {
                        path: google.maps.SymbolPath.CIRCLE,
                        scale: 3,
                        fillColor: '#FF0000',
                        fillOpacity: 0.3,
                        strokeColor: '#FF0000',
                        strokeWeight: 2
                    }
                });
                const infoWindow_centroide_%d = new google.maps.InfoWindow({
                    content: '%s'
                });
                marcador_centroide_%d.addListener('click', () => infoWindow_centroide_%d.open(map, marcador_centroide_%d));
            }
            """, id, centroide.getLatitude(), centroide.getLongitude(), id, descricao, id, id, id);
    }

    private static String gerarCirculoDeRelevancia(Propriedade propriedade, EstacaoMeteorologica estacaoMaisProxima) {
        Ponto centroide = propriedade.getCentroide();
        double distancia = centroide.distanciaAte(estacaoMaisProxima.getLocalizacao());

        return String.format(Locale.US, """
            const raio_relevancia_%d = new google.maps.Circle({
                strokeColor: '#0000FF',
                strokeOpacity: 0.5,
                strokeWeight: 2,
                fillColor: '#0000FF',
                fillOpacity: 0.08,
                map: map,
                center: {lat: %.8f, lng: %.8f},
                radius: %.2f,
                clickable: false
            });
            """, propriedade.getId(), centroide.getLatitude(), centroide.getLongitude(), distancia);
    }

    private static String gerarMarcadoresParaEstacoes(List<EstacaoMeteorologica> estacoes) {
        return estacoes.stream()
                .map(VisualizadorMapaUtil::gerarMarcadorParaEstacaoUnica)
                .collect(Collectors.joining("\n"));
    }

    private static String gerarMarcadorParaEstacaoUnica(EstacaoMeteorologica estacao) {
        Ponto localizacao = estacao.getLocalizacao();
        String corHexadecimal = estacao.getCor().getCodigoHexadecimal().replace("#", "");
        String urlIconeEstacao = "https://img.icons8.com/?size=100&id=17482&format=png&color=" + corHexadecimal;
        String conteudoDescricao = gerarDescricaoHtmlParaEstacao(estacao);

        return String.format(Locale.US, """
            {
                window.marcador_estacao_%s = new google.maps.Marker({
                    position: {lat: %.8f, lng: %.8f},
                    map: map,
                    title: '%s',
                    icon: {
                        url: '%s',
                        scaledSize: new google.maps.Size(16, 16)
                    }
                });
                window.infoWindow_estacao_%s = new google.maps.InfoWindow({
                    content: "%s"
                });
                window.marcador_estacao_%s.addListener('click', () => window.infoWindow_estacao_%s.open(map, window.marcador_estacao_%s));
            }
            """,
                estacao.getCodigoEstacao(), localizacao.getLatitude(), localizacao.getLongitude(), estacao.getNome(), urlIconeEstacao,
                estacao.getCodigoEstacao(), conteudoDescricao, estacao.getCodigoEstacao(), estacao.getCodigoEstacao(), estacao.getCodigoEstacao());
    }

    private static String gerarMarcadoresParaPontosDePrevisao(List<Temperatura> previsoes) {
        String urlIconePrevisao = "https://img.icons8.com/?size=100&id=15345&format=png&color=000000";
        return previsoes.stream()
                .map(previsao -> {
                    Ponto ponto = previsao.getPonto();
                    long id = previsao.getId();
                    String descricaoPrevisao = gerarDescricaoHtmlParaPrevisao(previsao);
                    return String.format(Locale.US, """
                {
                    const marcador_previsao_%d = new google.maps.Marker({
                        position: {lat: %.8f, lng: %.8f},
                        map: map,
                        title: 'Previsão #%d',
                        icon: {
                            url: '%s',
                            scaledSize: new google.maps.Size(32, 32)
                        }
                    });
                    const infoWindow_previsao_%d = new google.maps.InfoWindow({
                        content: '%s'
                    });
                    marcador_previsao_%d.addListener('click', () => infoWindow_previsao_%d.open(map, marcador_previsao_%d));
                }
                """, id, ponto.getLatitude(), ponto.getLongitude(), id, urlIconePrevisao, id, descricaoPrevisao, id, id, id);
                })
                .collect(Collectors.joining("\n"));
    }

    private static String gerarDescricaoHtmlParaPropriedade(Propriedade propriedade) {
        String descricoesEstacoes = propriedade.getCentroide().getEstacoesMeteorologicas().stream()
                .map(estacao -> {
                    String temperaturaFormatada = formatarTemperaturaParaInfoWindow(estacao.getLocalizacao().getHistoricoTemperaturas(), true);
                    String dataFormatada = obterDataFormatada(estacao.getLocalizacao().getHistoricoTemperaturas());
                    return String.format("<li><a href='#' onclick=\"abrirEstacao('%s');return false;\">%s</a>: %s %s</li>", estacao.getCodigoEstacao(), estacao.getNome(), temperaturaFormatada, dataFormatada);
                })
                .collect(Collectors.joining("", "<ul style='margin: 0; padding-left: 18px;'>", "</ul>"));

        String temperatura = formatarTemperaturaParaInfoWindow(propriedade.getCentroide().getHistoricoTemperaturas(), true);
        String dataHora = obterDataFormatada(propriedade.getCentroide().getHistoricoTemperaturas());

        String html = String.format(
                "<div style='font-family: Arial, sans-serif; line-height: 1.5;'>" +
                        "<h4>Propriedade: %s</h4>" +
                        "Proprietário: %s<br>" +
                        "CPF: %s<br>" +
                        "Estações Associadas: %s" +
                        "Temperatura Estimada: %s %s" +
                        "</div>",
                propriedade.getNome(), propriedade.getProprietario().getNome(), propriedade.getProprietario().getCpf(),
                descricoesEstacoes, temperatura, dataHora
        );
        return html.replace("'", "\\'");
    }

    private static String gerarDescricaoHtmlParaEstacao(EstacaoMeteorologica estacao) {
        String temperaturaFormatada = formatarTemperaturaParaInfoWindow(estacao.getLocalizacao().getHistoricoTemperaturas(), true);
        String dataFormatada = obterDataFormatada(estacao.getLocalizacao().getHistoricoTemperaturas());
        String latitudeFormatada = FormatadorUtil.formatarCoordenadaParaExibicao(estacao.getLocalizacao().getLatitude());
        String longitudeFormatada = FormatadorUtil.formatarCoordenadaParaExibicao(estacao.getLocalizacao().getLongitude());

        return String.format(
                "<div style='font-family: Arial, sans-serif; line-height: 1.5;'>" +
                        "<h4>Estação: %s</h4>" +
                        "Código: %s<br>" +
                        "Latitude: %s<br>" +
                        "Longitude: %s<br>" +
                        "Situação: %s<br>" +
                        "Tipo Estação: %s<br>" +
                        "Temperatura: %s %s" +
                        "</div>",
                estacao.getNome(), estacao.getCodigoEstacao(), latitudeFormatada, longitudeFormatada,
                estacao.getSituacao(), estacao.getTipoEstacao(), temperaturaFormatada, dataFormatada
        ).replace("\"", "\\\"");
    }

    private static String gerarDescricaoHtmlParaPrevisao(Temperatura previsao) {
        String temperaturaExibida;
        String dataFormatada = previsao.getDataHora().format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO);
        String latitudeFormatada = FormatadorUtil.formatarCoordenadaParaExibicao(previsao.getPonto().getLatitude());
        String longitudeFormatada = FormatadorUtil.formatarCoordenadaParaExibicao(previsao.getPonto().getLongitude());

        if (previsao.getTemperaturaReal() == null) {
            temperaturaExibida = formatarTemperaturaParaInfoWindow(List.of(previsao), false) + " (Prevista)";
        } else {
            temperaturaExibida = formatarTemperaturaParaInfoWindow(List.of(previsao), true);
        }

        return String.format(
                "<div style='font-family: Arial, sans-serif; line-height: 1.5;'>" +
                        "<h4>Previsão (%s)</h4>" +
                        "Latitude: %s<br>" +
                        "Longitude: %s<br>" +
                        "<b>Temperatura:</b> %s" +
                        "</div>",
                dataFormatada, latitudeFormatada, longitudeFormatada, temperaturaExibida
        ).replace("'", "\\'");
    }

    private static String gerarCoordenadasParaLimitesVisuais(Collection<Propriedade> propriedades, Collection<Temperatura> previsoes) {
        Stream<Ponto> pontosDePoligonos = propriedades.stream()
                .flatMap(propriedade -> propriedade.getPoligono().getPontos().stream());

        Stream<Ponto> pontosDePrevisoes = previsoes.stream()
                .map(Temperatura::getPonto);

        return Stream.concat(pontosDePoligonos, pontosDePrevisoes)
                .map(ponto -> String.format(Locale.US, "new google.maps.LatLng(%.8f,%.8f)", ponto.getLatitude(), ponto.getLongitude()))
                .collect(Collectors.joining(",\n"));
    }

    private static String gerarVerticesDoPoligono(Poligono poligono) {
        return poligono.getPontos().stream()
                .map(ponto -> String.format(Locale.US, "{lat: %.8f, lng: %.8f}", ponto.getLatitude(), ponto.getLongitude()))
                .collect(Collectors.joining(",\n"));
    }

    private static Temperatura obterTemperaturaMaisRecente(List<Temperatura> historico) {
        return historico.stream()
                .max(Comparator.comparing(Temperatura::getDataHora))
                .orElse(null);
    }

    private static String formatarTemperaturaParaInfoWindow(List<Temperatura> temperaturas, boolean temperaturaReal) {
        if (temperaturas == null || temperaturas.isEmpty()) {
            return NENHUMA_TEMPERATURA;
        }
        Temperatura temperatura = obterTemperaturaMaisRecente(temperaturas);
        if (temperatura == null) {
            return NENHUMA_TEMPERATURA;
        }

        Double valor;
        if (temperaturaReal) {
            valor = temperatura.getTemperaturaReal();
        } else {
            valor = temperatura.getTemperaturaPrevista();
        }

        if (valor == null) {
            return NENHUMA_TEMPERATURA;
        }

        return String.format("<h4 style='display:inline;'>%s°C</h4>", FormatadorUtil.formatarPontoDecimalParaVirgula(valor));
    }

    private static String formatarTemperaturaParaRotuloDoMapa(List<Temperatura> temperaturas, boolean temperaturaReal) {
        if (temperaturas == null || temperaturas.isEmpty()) {
            return NENHUMA_TEMPERATURA;
        }
        Temperatura temperatura = obterTemperaturaMaisRecente(temperaturas);
        if (temperatura == null) {
            return NENHUMA_TEMPERATURA;
        }

        Double valor;
        if (temperaturaReal) {
            valor = temperatura.getTemperaturaReal();
        } else {
            valor = temperatura.getTemperaturaPrevista();
        }

        if (valor == null) {
            return NENHUMA_TEMPERATURA;
        }

        return FormatadorUtil.formatarPontoDecimalParaVirgula(valor) + GRAUS_CELSIUS;
    }

    private static String obterDataFormatada(List<Temperatura> temperaturas) {
        if (temperaturas == null || temperaturas.isEmpty()) {
            return STRING_VAZIA;
        }
        Temperatura temperatura = obterTemperaturaMaisRecente(temperaturas);
        if (temperatura == null || temperatura.getDataHora() == null) {
            return STRING_VAZIA;
        }

        return "(" + temperatura.getDataHora().format(FormatadorUtil.FORMATADOR_DATA_HORA_PARA_EXIBICAO) + ")";
    }

    private static String lerArquivoTemplate() throws Exception {
        Path caminhoArquivo = Paths.get(NOME_ARQUIVO_TEMPLATE);
        if (!Files.exists(caminhoArquivo)) {
            throw new FileNotFoundException("Arquivo " + NOME_ARQUIVO_TEMPLATE + " não encontrado.");
        }
        return Files.readString(caminhoArquivo);
    }

    private static void abrirHtmlNoNavegador(String conteudoHtml) {
        try {
            Path caminhoArquivoTeste = Paths.get("resources/mapa_gerado.html");
            Files.write(caminhoArquivoTeste, conteudoHtml.getBytes());

            Path arquivoTemporario = Files.createTempFile("mapa_visualizacao_", ".html");
            Files.write(arquivoTemporario, conteudoHtml.getBytes());
            arquivoTemporario.toFile().deleteOnExit();

            new ProcessBuilder("cmd", "/c", "start", "msedge", arquivoTemporario.toUri().toString()).start();
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível abrir o mapa no navegador.", e);
        }
    }

    private static String gerarLinhasTracejadasParaPropriedade(Propriedade propriedade) {
        long idPropriedade = propriedade.getId();
        return propriedade.getCentroide().getEstacoesMeteorologicas().stream()
                .map(estacao -> {
                    String idEstacao = estacao.getCodigoEstacao();
                    Ponto centroide = propriedade.getCentroide();
                    Ponto localEstacao = estacao.getLocalizacao();
                    String temperaturaTexto = formatarTemperaturaParaRotuloDoMapa(localEstacao.getHistoricoTemperaturas(), true);

                    return String.format(Locale.US, """
                    let linha_tracejada_%d_%s = null;
                    let marcador_rotulo_temperatura_%d_%s = null;
                    poligono_propriedade_%d.addListener('mouseover', function() {
                        linha_tracejada_%d_%s = new google.maps.Polyline({
                            path: [
                                {lat: %.8f, lng: %.8f},
                                {lat: %.8f, lng: %.8f}
                            ],
                            map: map,
                            strokeOpacity: 0,
                            icons: [{
                                icon: {
                                    path: 'M 0,-1 0,1',
                                    strokeOpacity: 1,
                                    scale: 2
                                },
                                offset: '0',
                                repeat: '10px'
                            }]
                        });
                        marcador_rotulo_temperatura_%d_%s = new google.maps.Marker({
                            position: {lat: %.8f, lng: %.8f},
                            map: map,
                            label: {
                                text: '%s',
                                color: '#FFFF',
                                fontSize: '13px',
                                fontWeight: 'bold'
                            },
                            icon: {
                                path: google.maps.SymbolPath.CIRCLE,
                                scale: 0
                            }
                        });
                    });
                    poligono_propriedade_%d.addListener('mouseout', function() {
                        if (linha_tracejada_%d_%s) {
                            linha_tracejada_%d_%s.setMap(null);
                        }
                        if (marcador_rotulo_temperatura_%d_%s) {
                            marcador_rotulo_temperatura_%d_%s.setMap(null);
                        }
                    });
                """, idPropriedade, idEstacao, idPropriedade, idEstacao, idPropriedade, idPropriedade, idEstacao,
                            centroide.getLatitude(), centroide.getLongitude(), localEstacao.getLatitude(), localEstacao.getLongitude(),
                            idPropriedade, idEstacao, localEstacao.getLatitude(), localEstacao.getLongitude(), temperaturaTexto, idPropriedade,
                            idPropriedade, idEstacao, idPropriedade, idEstacao, idPropriedade, idEstacao, idPropriedade, idEstacao);
                })
                .collect(Collectors.joining("\n"));
    }
}