let map;
const elementosMapa = {
    propriedades: {},
    estacoes: {}
};
let bounds;
let propriedades;
let propriedadeMaisRecente;

function initMap() {
    map = new google.maps.Map(document.getElementById("map"), {
        mapTypeId: google.maps.MapTypeId.TERRAIN,
        disableDefaultUI: true,
        zoomControl: true
    });

    bounds = new google.maps.LatLngBounds();

    const estacoes = JSON.parse(document.getElementById('dados-estacoes').textContent);
    propriedades = JSON.parse(document.getElementById('dados-propriedades').textContent);
    propriedadeMaisRecente = JSON.parse(document.getElementById('dados-propriedade-recente').textContent);

    criarMarcadoresEstacoes(estacoes);
    criarElementosPropriedades(propriedades);

    if (!bounds.isEmpty()) {
        map.fitBounds(bounds);
    }

    configurarControlesMenu();

}
document.addEventListener('DOMContentLoaded', initMap);

function criarMarcadoresEstacoes(estacoes) {
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
                scaledSize: new google.maps.Size(16, 16)
            }
        });

        const conteudoInfoWindow = `
            <div class="info-window-conteudo">
                <h3>Estação Meteorológica: ${estacao.nome}</h3>
                <p><strong>Código: ${estacao.codigoEstacao}</strong></p>
                <p><strong>Situação: ${estacao.situacao}</strong></p>
                <p><strong>Tipo: ${estacao.tipoEstacao}</strong></p>
                <p><strong>Temperatura: ${estacao.localizacao.temperaturaRecente}</strong></p>
            </div>
        `;

        const infoWindow = new google.maps.InfoWindow({
            content: conteudoInfoWindow
        });

        marcador.addListener('click', () => {
            infoWindow.open(map, marcador);
        });

        bounds.extend(marcador.getPosition());

        elementosMapa.estacoes[estacao.id] = {
            marcador: marcador,
            infoWindow: infoWindow
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

        const conteudoInfoWindow = `
            <div class="info-window-conteudo">
                <h3>Propriedade: ${propriedade.nome}</h3>
                <p><strong>Proprietário: ${propriedade.nomeProprietario}</strong></p>
            </div>
        `;

        const infoWindow = new google.maps.InfoWindow({
            content: conteudoInfoWindow
        });

        poligono.addListener('click', event => {
            infoWindow.setPosition(event.latLng);
            infoWindow.open(map);
        })

        poligono.setMap(map);

        vertices.forEach(vertice => bounds.extend(vertice));

        elementosMapa.propriedades[propriedade.id] = {
            poligono: poligono,
            infoWindow: infoWindow
        };
    });
}

function configurarControlesMenu() {
    document.getElementById('select-propriedade').addEventListener('change', atualizarVisualizacao);
    document.getElementById('checkbox-estacoes').addEventListener('change', atualizarVisualizacao);
    document.getElementById('botao-buscar').addEventListener('click', atualizarVisualizacao);

    const campoBusca = document.getElementById("input-busca");
    const selectTipoBusca = document.getElementById("select-propriedade");

    selectTipoBusca.addEventListener('change', () => {
        campoBusca.value = ''

        if (selectTipoBusca.value === 'cpf') {
            VMasker(campoBusca).maskPattern("999.999.999-99");
        } else {
            VMasker(campoBusca).unMask();
        }

    })

    atualizarVisualizacao();
}

function atualizarVisualizacao() {
    const opcaoVisualizarPropriedades = document.getElementById('select-propriedade').value;
    const inputBusca = document.getElementById('input-busca');
    const grupoBusca = document.getElementById('grupo-busca');
    const mostrarEstacoes = document.getElementById('checkbox-estacoes').checked;

    if (opcaoVisualizarPropriedades === 'nome' || opcaoVisualizarPropriedades === 'cpf') {
        grupoBusca.style.display = 'block';
    } else {
        grupoBusca.style.display = 'none';
    }
    
    for (const id in elementosMapa.propriedades) {
        elementosMapa.propriedades[id].poligono.setMap(null);
    }

    switch (opcaoVisualizarPropriedades) {
        case 'todas':
            for (const id in elementosMapa.propriedades) {
                elementosMapa.propriedades[id].poligono.setMap(map);
            }
            break;
        case 'nome':
            const nomeBusca = inputBusca.value.toLowerCase();
            if (nomeBusca) {
                propriedades.forEach(propriedade => {
                    if (propriedade.nome.toLowerCase().includes(nomeBusca)) {
                        elementosMapa.propriedades[propriedade.id].poligono.setMap(map);
                    }
                });
            }
            break;
        case 'cpf':
            const cpfBusca = inputBusca.value.replace(/\D/g, '');
            if (cpfBusca) {
                propriedades.forEach(propriedade => {
                    if (propriedade.cpfProprietario.includes(cpfBusca)) {
                        elementosMapa.propriedades[propriedade.id].poligono.setMap(map);
                    }
                })
            }
            break;
        case 'recente':
            elementosMapa.propriedades[propriedadeMaisRecente.id].poligono.setMap(map);
            break;
        default:
            for (const id in elementosMapa.propriedades) {
                elementosMapa.propriedades[id].poligono.setMap(map);
            }
            break;
    }

    for (const id in elementosMapa.estacoes) {
        elementosMapa.estacoes[id].marcador.setMap(mostrarEstacoes ? map : null);
    }
}