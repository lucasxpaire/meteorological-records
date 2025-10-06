let map;
const elementosMapa = {
    propriedades: {},
    estacoes: {}
};
let bounds;
let propriedades;
let propriedadeMaisRecente;
let estacoes;

const CENTRO_PADRAO_MAPA_BRASIL = { lat: -12.173683701367969, lng: -52.03651393308807 };
const CENTRO_PADRAO_LOCAL = { lat: -29.6842, lng: -53.8069 };

const CRITERIOS_DE_BUSCA = {
    TODAS: 'todas',
    RECENTE: 'recente',
    NOME: 'nome',
    CPF: 'cpf'
};

function initMap() {
    map = new google.maps.Map(document.getElementById("map"), {
        mapTypeId: google.maps.MapTypeId.TERRAIN,
        disableDefaultUI: true,
        zoomControl: true,
        center: CENTRO_PADRAO_MAPA_BRASIL,
        zoom: 4
    });

    bounds = new google.maps.LatLngBounds();

    estacoes = JSON.parse(document.getElementById('dados-estacoes').textContent);
    propriedades = JSON.parse(document.getElementById('dados-propriedades').textContent);
    propriedadeMaisRecente = JSON.parse(document.getElementById('dados-propriedade-recente').textContent);

    criarElementosEstacoes(estacoes);
    criarElementosPropriedades(propriedades);

    configurarControlesMenu();

    atualizarVisualizacao();
}
document.addEventListener('DOMContentLoaded', initMap);

function filtrarPropriedades(criterio, valor) {
    const valorBusca = valor ? valor.toLowerCase().trim() : '';

    switch (criterio) {
        case CRITERIOS_DE_BUSCA.TODAS:
            return propriedades;
        case CRITERIOS_DE_BUSCA.NOME:
            if (!valorBusca) {
                return [];
            }
            return propriedades.filter(prop => prop.nome.toLowerCase().includes(valorBusca));
        case CRITERIOS_DE_BUSCA.CPF:
            if (!valorBusca) return [];
            return propriedades.filter(prop => prop.cpfProprietario.includes(valorBusca));
        case CRITERIOS_DE_BUSCA.RECENTE:
            return propriedadeMaisRecente ? [propriedadeMaisRecente] : [];
        default:
            return [];
    }
}

function criarElementosEstacoes(estacoes) {
    estacoes.forEach(estacao => {
        const marcador = new google.maps.Marker({
            position: {
                lat: estacao.localizacao.latitude,
                lng: estacao.localizacao.longitude
            },
            map: map,
            title: estacao.nome,
            icon: {
                url: "https://img.icons8.com/?size=100&id=17482&format=png&color=" + estacao.cor.codigoHexadecimal.replace("#", ""),
                scaledSize: new google.maps.Size(16, 16),
                labelOrigin: new google.maps.Point(8, -4)
            },
        });

        const infoWindow = new google.maps.InfoWindow({
            content: `
                <div class="info-window-conteudo">
                    <h3>Estação Meteorológica: ${estacao.nome}</h3>
                    <p><strong>Latitude: ${estacao.localizacao.latitudeFormatada}</strong></p>
                    <p><strong>Longitude: ${estacao.localizacao.longitudeFormatada}</strong></p>
                    <p><strong>Código: ${estacao.codigoEstacao}</strong></p>
                    <p><strong>Situação: ${estacao.situacao}</strong></p>
                    <p><strong>Tipo: ${estacao.tipoEstacao}</strong></p>
                    <p><strong>Temperatura: ${estacao.localizacao.temperaturaRecente}</strong></p>
                </div>
            `
        });

        marcador.addListener('mouseover', () => {
            infoWindow.open(map, marcador);
        });

        marcador.addListener('mouseout', () => {
            infoWindow.close();
        });

        elementosMapa.estacoes[estacao.id] = {
            marcador: marcador,
            infoWindow: infoWindow,
            localizacao: estacao.localizacao
        };
    });
}

function criarElementosPropriedades(propriedades) {
    propriedades.forEach(propriedade => {
        const vertices = propriedade.poligono.pontos.map(ponto => ({ lat: ponto.latitude, lng: ponto.longitude }));

        const poligono = new google.maps.Polygon({
            paths: vertices,
            strokeColor: propriedade.corCodigoHexadecimal,
            strokeOpacity: 0.8,
            strokeWeight: 2,
            fillColor: propriedade.corCodigoHexadecimal,
            fillOpacity: 0.25
        });

        const centroide = new google.maps.Circle({
            center: { lat: propriedade.centroide.latitude, lng: propriedade.centroide.longitude},
            strokeColor: propriedade.corCodigoHexadecimal,
            strokeOpacity: 0.8,
            strokeWeight: 2,
            fillColor: propriedade.corCodigoHexadecimal,
            fillOpacity: 0.15,
            radius: 200
        })

        const raioDeRelevancia = new google.maps.Circle({
            center: { lat: propriedade.centroide.latitude, lng: propriedade.centroide.longitude },
            radius: propriedade.centroide.raioRelevanciaEmMetros,
            strokeColor: propriedade.corCodigoHexadecimal,
            strokeOpacity: 0.5,
            strokeWeight: 1,
            fillOpacity: 0.2,
            map: map,
            visible: false
        });

        const descricaoPropriedade = `
            <div class="info-window-conteudo">
                <h3>Propriedade: ${propriedade.nome}</h3>
                <p><strong>Proprietário: ${propriedade.nomeProprietario}</strong></p>
                <p><strong>CPF: ${propriedade.cpfProprietario}</strong></p>
                <p><strong>Cor: ${propriedade.corNome}</strong></p>
            </div>
        `;

        const infoWindowDescricaoPropriedade = new google.maps.InfoWindow({
            content: descricaoPropriedade
        });

        let listaEstacoesHtml = '';
        if (propriedade.centroide.estacoesMeteorologicas && propriedade.centroide.estacoesMeteorologicas.length > 0) {
            const tituloLista = '<p><strong>Estações Associadas:</strong></p>';

            const itensLista = propriedade.centroide.estacoesMeteorologicas.map(estacao => `<li><p><strong> ${estacao.nome} (${estacao.codigoEstacao}): ${estacao.localizacao.temperaturaRecente}</strong></p></li>`).join('');

            const listaUl = `<ul class="info-window-lista-estacoes">${itensLista}</ul>`;

            listaEstacoesHtml = tituloLista + listaUl;
        }

        const descricaoCentroide = `
            <div class="info-window-conteudo">
                <h3>Centróide</h3>
                <p><strong>Latitude: ${propriedade.centroide.latitudeFormatada}</strong></p>
                <p><strong>Longitude: ${propriedade.centroide.longitudeFormatada}</strong></p>
                <p><strong>Temperatura: ${propriedade.centroide.temperaturaRecente}</strong></p>
                ${listaEstacoesHtml}
            </div>
        `;

        const infoWindowDescricaoCentroide = new google.maps.InfoWindow({
            content: descricaoCentroide
        });

        centroide.addListener('mouseover', event => {
            infoWindowDescricaoCentroide.setPosition(event.latLng);
            infoWindowDescricaoCentroide.open(map);
        })

        centroide.addListener('mouseout', function() {
            infoWindowDescricaoCentroide.close();
        });

        const linhasPontilhadas  = [];
        if (propriedade.centroide.estacoesMeteorologicas) {
            propriedade.centroide.estacoesMeteorologicas.forEach(estacao => {
                const linha = new google.maps.Polyline({
                    path: [
                        { lat: propriedade.centroide.latitude, lng: propriedade.centroide.longitude},
                        { lat: estacao.localizacao.latitude, lng: estacao.localizacao.longitude }
                    ],
                    geodesic: true,
                    strokeColor: '#000000',
                    strokeOpacity: 0,
                    strokeWeight: 2,
                    icons: [{
                        icon: {
                            path: 'M 0,-1 0,1',
                            strokeOpacity: 1,
                            scale: 4
                        },
                        offset: '0',
                        repeat: '20px'
                    }],
                    map: map,
                    visible: false
                });
                linhasPontilhadas.push(linha);
            });
        }

        poligono.addListener('click', event => {
            infoWindowDescricaoPropriedade.setPosition(event.latLng);
            infoWindowDescricaoPropriedade.open(map);
        });

        poligono.addListener('mouseover', () => {
            raioDeRelevancia.setVisible(true);
            linhasPontilhadas.forEach(linha => linha.setVisible(true));

            if (propriedade.centroide.estacoesMeteorologicas) {
                propriedade.centroide.estacoesMeteorologicas.forEach(estacaoAssociada => {
                    const estacaoElemento = elementosMapa.estacoes[estacaoAssociada.id];
                    if (estacaoElemento) {
                        estacaoElemento.marcador.setLabel({
                            text: estacaoAssociada.localizacao.temperaturaRecenteParaLabel,
                            color: '#ffffff',
                            fontSize: '12px',
                            fontWeight: 'bold',
                        });
                    }
                });
            }
        })

        poligono.addListener('mouseout', () => {
            raioDeRelevancia.setVisible(false);

            linhasPontilhadas.forEach(linha => linha.setVisible(false));

            if (propriedade.centroide.estacoesMeteorologicas) {
                propriedade.centroide.estacoesMeteorologicas.forEach(estacaoAssociada => {
                    const estacaoElemento = elementosMapa.estacoes[estacaoAssociada.id];
                    if (estacaoElemento) {
                        estacaoElemento.marcador.setLabel(null);
                    }
                });
            }
        });

        poligono.setMap(map);
        centroide.setMap(map);

        elementosMapa.propriedades[propriedade.id] = {
            poligono: poligono,
            centroide: centroide,
            raioDeRelevancia: raioDeRelevancia,
            linhasPontilhadas: linhasPontilhadas,
            infoWindow: infoWindowDescricaoPropriedade,
            infoWindowDescricaoCentroide,
            vertices: vertices
        };
    });
}

function configurarControlesMenu() {
    document.getElementById('select-propriedade').addEventListener('change', atualizarVisualizacao);
    document.getElementById('botao-buscar').addEventListener('click', atualizarVisualizacao);

    const checkboxEstacoes = document.getElementById('checkbox-estacoes');
    const checkboxCentroides = document.getElementById('checkbox-centroides');
    const botaoAjustarVisualizacao  = document.getElementById('botao-ajustar-zoom');

    checkboxEstacoes.addEventListener('change', () => {
        const visivel = checkboxEstacoes.checked;
        for (const id in elementosMapa.estacoes) {
            elementosMapa.estacoes[id].marcador.setVisible(visivel);
        }
        atualizarVisualizacao();
    });

    checkboxCentroides.addEventListener('mouseover', () => {
        const visivel = checkboxCentroides.checked;
        for (const id in elementosMapa.propriedades) {
            elementosMapa.propriedades[id].centroide.setVisible(visivel);
        }
    });

    botaoAjustarVisualizacao .addEventListener('click', () => {
        const opcaoVisualizarPropriedades = document.getElementById('select-propriedade').value;
        const inputBusca = document.getElementById('input-busca').value;

        const propriedadesVisiveis = filtrarPropriedades(opcaoVisualizarPropriedades, inputBusca);

        const novosBounds = new google.maps.LatLngBounds();

        if (propriedadesVisiveis.length > 0) {
            propriedadesVisiveis.forEach(prop => {
                const elemento = elementosMapa.propriedades[prop.id];
                if (elemento && elemento.vertices) {
                    elemento.vertices.forEach(vertice => novosBounds.extend(vertice));
                }
            });
        } else {
            for (const id in elementosMapa.estacoes) {
                const estacao = elementosMapa.estacoes[id];
                novosBounds.extend({ lat: estacao.localizacao.latitude, lng: estacao.localizacao.longitude });
            }
        }

        if (!novosBounds.isEmpty()) {
            map.fitBounds(novosBounds);
        } else {
            map.setCenter(CENTRO_PADRAO_LOCAL);
            map.setZoom(12);
        }
    });

    const campoBusca = document.getElementById("input-busca");
    const selectTipoBusca = document.getElementById("select-propriedade");

    selectTipoBusca.addEventListener('change', () => {
        campoBusca.value = '';

        if (selectTipoBusca.value === CRITERIOS_DE_BUSCA.CPF) {
            VMasker(campoBusca).maskPattern("999.999.999-99");
        } else {
            VMasker(campoBusca).unMask();
        }
    });
}

function atualizarVisualizacao() {
    const opcaoVisualizarPropriedades = document.getElementById('select-propriedade').value;
    const inputBusca = document.getElementById('input-busca');
    const grupoBusca = document.getElementById('grupo-busca');
    const mostrarEstacoes = document.getElementById('checkbox-estacoes').checked;
    const avisoFalha = document.getElementById('busca-alerta-falha');
    const mostrarCentroides = document.getElementById('checkbox-centroides').checked;

    avisoFalha.style.display = 'none';

    if (opcaoVisualizarPropriedades === CRITERIOS_DE_BUSCA.NOME || opcaoVisualizarPropriedades === CRITERIOS_DE_BUSCA.CPF) {
        grupoBusca.style.display = 'block';
    } else {
        grupoBusca.style.display = 'none';
    }

    const propriedadesVisiveis = filtrarPropriedades(opcaoVisualizarPropriedades, inputBusca.value);
    const buscaAtivaComTermo = (opcaoVisualizarPropriedades === CRITERIOS_DE_BUSCA.NOME || opcaoVisualizarPropriedades === CRITERIOS_DE_BUSCA.CPF) && inputBusca.value;
    if (buscaAtivaComTermo && propriedadesVisiveis.length === 0) {
        avisoFalha.style.display = 'block';
    }

    for (const id in elementosMapa.propriedades) {
        elementosMapa.propriedades[id].poligono.setMap(null);
        elementosMapa.propriedades[id].centroide.setMap(null);
    }

    propriedadesVisiveis.forEach(prop => {
        if (elementosMapa.propriedades[prop.id]) {
            elementosMapa.propriedades[prop.id].poligono.setMap(map);
            if (mostrarCentroides) {
                elementosMapa.propriedades[prop.id].centroide.setMap(map);
            }
        }
    });

    const novosBounds = new google.maps.LatLngBounds();

    if (propriedadesVisiveis.length > 0) {
        propriedadesVisiveis.forEach(prop => {
            const elemento = elementosMapa.propriedades[prop.id];
            if (elemento && elemento.vertices) {
                elemento.vertices.forEach(vertice => novosBounds.extend(vertice));
            }
        });
    } else if (mostrarEstacoes) {
        for (const id in elementosMapa.estacoes) {
            const estacao = elementosMapa.estacoes[id];
            novosBounds.extend({lat: estacao.localizacao.latitude, lng: estacao.localizacao.longitude});
        }
    }

    if (!novosBounds.isEmpty()) {
        map.fitBounds(novosBounds);
    }
}