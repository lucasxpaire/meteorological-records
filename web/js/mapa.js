let mapa;

let elementosMapa = {
    propriedades: {},
    estacoesMeteorologicas: {},
    previsoesTemperatura: {}
}

const COORDENADA_CENTRAL_BRASIL = { lat: -12.173683701367969, lng: -52.03651393308807 };
const ZOOM_PADRAO = 4;

const VISIVEL = 'block';
const ESCONDIDO = 'none';

const BUSCA_POR_NOME = '3';
const BUSCA_POR_CPF = '4';

document.addEventListener('DOMContentLoaded', () => {
    iniciarMapa();
    inicializarControlesDoMapa();
    criarElementosEstacoes();
    criarElementosPropriedades();
    ajustarVisualizacaoParaPropriedades();
});

function inicializarControlesDoMapa() {
    const opcaoSelecionada = document.getElementById('opcaoSelecionada');

    opcaoSelecionada.addEventListener('change', configurarVisibilidadeCamposDeBusca);

    configurarVisibilidadeCamposDeBusca();
}

function iniciarMapa() {
    mapa = new google.maps.Map(document.getElementById("mapa"), {
        mapTypeId: google.maps.MapTypeId.TERRAIN,
        disableDefaultUI: true,
        zoomControl: true,
        center: COORDENADA_CENTRAL_BRASIL,
        zoom: ZOOM_PADRAO
    });
}

function criarElementosEstacoes() {
    estacoes.forEach(estacao => {
        const icone = criarIconeEstacao(estacao);
        const descricao = criarDescricaoEstacao(estacao);

        icone.addListener('click', () => {
            descricao.open(mapa, icone);
        });

        elementosMapa.estacoesMeteorologicas[estacao.id] = {
            icone: icone,
            descricao: descricao
        };
    });
}

function criarIconeEstacao(estacao) {
    return new google.maps.Marker({
        position: {lat: estacao.localizacao.latitude, lng: estacao.localizacao.longitude},
        map: mapa,
        title: estacao.nome,
        icon: {
            url: "https://img.icons8.com/?size=100&id=17482&format=png&color=" + estacao.cor.codigoHexadecimal.replace("#", ""),
            scaledSize: new google.maps.Size(16, 16),
            labelOrigin: new google.maps.Point(8, -4)
        },
    });
}

function criarDescricaoEstacao(estacao) {
    return new google.maps.InfoWindow({
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
}

function criarElementosPropriedades() {
    propriedades.forEach(propriedade => {
        const poligono = criarPoligono(propriedade);
        const centroide = criarCentroide(propriedade);
        const descricaoPropriedade = criarDescricaoPropriedade(propriedade);
        const descricaoCentroide = criarDescricaoCentroide(propriedade);
        const linhasTracejadas = criarLinhasTracejadasEntreCentroideEEstacoes(propriedade);

        poligono.addListener('click', (event) => {
            descricaoPropriedade.setPosition(event.latLng);
            descricaoPropriedade.open(mapa);
            linhasTracejadas.forEach(linha => linha.setVisible(true));
            propriedade.centroide.estacoesMeteorologicas.forEach(estacao => {
                const elementoEstacao = elementosMapa.estacoesMeteorologicas[estacao.id];
                elementoEstacao.icone.setLabel(criarLabelTemperaturaEstacao(estacao));
            })

        });

        centroide.addListener('click', (event) => {
            descricaoCentroide.setPosition(event.latLng);
            descricaoCentroide.open(mapa);
        })

        descricaoPropriedade.addListener('closeclick', () => {
            linhasTracejadas.forEach(linha => linha.setVisible(false));
            propriedade.centroide.estacoesMeteorologicas.forEach(estacao => {
                const elementoEstacao = elementosMapa.estacoesMeteorologicas[estacao.id];
                elementoEstacao.icone.setLabel(null);
            });
        });

        elementosMapa.propriedades[propriedade.id] = {
            poligono: poligono,
            centroide: centroide,
            descricaoPropriedade: descricaoPropriedade,
            descricaoCentroide: descricaoCentroide,
            linhasTracejadas: linhasTracejadas
        }
    });
}

function criarPoligono(propriedade) {
    return new google.maps.Polygon({
        paths: propriedade.poligono.pontos.map(ponto => ({
            lat: ponto.latitude,
            lng: ponto.longitude
        })),
        strokeColor: propriedade.corCodigoHexadecimal,
        strokeOpacity: 0.8,
        strokeWeight: 2,
        fillColor: propriedade.corCodigoHexadecimal,
        fillOpacity: 0.25,
        map: mapa
    });
}

function criarCentroide(propriedade) {
    return new google.maps.Circle({
        center: { lat: propriedade.centroide.latitude, lng: propriedade.centroide.longitude },
        strokeColor: propriedade.corCodigoHexadecimal,
        strokeOpacity: 0.8,
        strokeWeight: 2,
        fillColor: propriedade.corCodigoHexadecimal,
        fillOpacity: 0.15,
        radius: 200,
        map: mapa
    });
}

function criarDescricaoPropriedade(propriedade) {
    const idParagrafoTemperatura = `temperatura-propriedade-${propriedade.id}`;
    const idBotaoPrevisao = `botao-previsao-${propriedade.id}`;

    return new google.maps.InfoWindow({
        content: `
            <div class="info-window-conteudo">
                <h3>Propriedade: ${propriedade.nome}</h3>
                <p><strong>Proprietário: ${propriedade.nomeProprietario}</strong></p>
                <p><strong>CPF: ${propriedade.cpfProprietario}</strong></p>
                <p><strong>Cor: ${propriedade.corNome}</strong></p>
                ${gerarHtmlEstacoesAssociadas(propriedade)}
                <p id="${idParagrafoTemperatura}"><strong>Temperatura: ${propriedade.centroide.temperaturaRecente}</strong></p>
                <button id="${idBotaoPrevisao}" onclick="preverTemperatura(${propriedade.centroide.id}, '${idParagrafoTemperatura}')" class="botao botao-tabela--visualizar">Prever temperatura</button>
            </div>
        `
    });
}

function gerarHtmlEstacoesAssociadas(propriedade) {
    let html;
    const tituloLista = '<p><strong>Estações Associadas:</strong></p>';
    const itensLista = propriedade.centroide.estacoesMeteorologicas.map(estacao => `<li><p><strong> ${estacao.nome} (${estacao.codigoEstacao}): ${estacao.localizacao.temperaturaRecente}</strong></p></li>`).join('');
    const listaUl = `<ul class="info-window-lista-estacoes">${itensLista}</ul>`;
    html = tituloLista + listaUl;
    return html;
}

function criarDescricaoCentroide(propriedade) {
    return new google.maps.InfoWindow({
        content: `
            <div class="info-window-conteudo">
                <h3>Centróide</h3>
                <p><strong>Latitude: ${propriedade.centroide.latitudeFormatada}</strong></p>
                <p><strong>Longitude: ${propriedade.centroide.longitudeFormatada}</strong></p>
            </div>
        `
    });
}

function criarLinhasTracejadasEntreCentroideEEstacoes(propriedade) {
    const linhas = [];

    propriedade.centroide.estacoesMeteorologicas.forEach(estacao => {
        const linha = new google.maps.Polyline({
            path: [
                { lat: propriedade.centroide.latitude, lng: propriedade.centroide.longitude },
                { lat: estacao.localizacao.latitude, lng: estacao.localizacao.longitude }
            ],
            geodesic: true,
            strokeColor: '#000000',
            strokeOpacity: 0,
            strokeWeight: 2,
            icons: [{
                icon: { path: 'M 0,-1 0,1', strokeOpacity: 1, scale: 4 },
                offset: '0',
                repeat: '20px'
            }],
            map: mapa,
            visible: false
        });
        linhas.push(linha);
    });

    return linhas;
}

function criarLabelTemperaturaEstacao(estacao) {
    return {
        text: estacao.localizacao.temperaturaRecenteParaLabel,
        color: '#ffffff',
        fontWeight: 'bold',
        fontSize: '12px'
    }
}

function preverTemperatura(idCentroide, idParagrafoTemperatura) {
    const paragrafoTemperatura = document.getElementById(idParagrafoTemperatura);
    const textoTemperatura = paragrafoTemperatura.querySelector('strong');

    fetch(`${urlPrevisao}?idCentroide=${idCentroide}`)
        .then(resposta => {
            if (!resposta.ok) {
                throw new Error(`Falha: ${response.status}`);
            }
            return resposta.json();
        })
        .then(data => {
            textoTemperatura.innerHTML = `Temperatura: ${data.temperaturaFormatada}`;
        })
        .catch(error => {
            console.error('Falha: Não foi possível prever temperatura.', error);
            textoTemperatura.innerHTML = 'Indisponível';
        });
}

function configurarVisibilidadeCamposDeBusca() {
    const opcaoSelecionada = document.getElementById('opcaoSelecionada').value;
    const divCpfBusca  = document.getElementById('cpfBusca').parentElement;
    const divNomeBusca  = document.getElementById('nomeBusca').parentElement;

    divCpfBusca.style.display = ESCONDIDO;
    divNomeBusca.style.display = ESCONDIDO;

    if (opcaoSelecionada === BUSCA_POR_CPF) {
        divCpfBusca.style.display = VISIVEL;
        const cpfBusca= document.getElementById('cpfBusca');
        VMasker(cpfBusca).maskPattern('999.999.999-99');
    } else if (opcaoSelecionada === BUSCA_POR_NOME) {
        divNomeBusca.style.display = VISIVEL;
    }

}

function ajustarVisualizacaoParaPropriedades() {
    const limites = new google.maps.LatLngBounds();

    if (propriedades.length > 0) {
        propriedades.forEach(propriedade => {
            propriedade.poligono.pontos.forEach(ponto => {
                limites.extend(new google.maps.LatLng(ponto.latitude, ponto.longitude));
            })
        })

        google.maps.event.addListenerOnce(mapa, 'idle', () => {
            mapa.fitBounds(limites);
        });
    } else {
        mapa.setCenter(COORDENADA_CENTRAL_BRASIL);
        mapa.setZoom(ZOOM_PADRAO);
    }
}