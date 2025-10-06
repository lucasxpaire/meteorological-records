let map;
const elementosMapa = {
    estacoes: {},
    marcadorPrevisao: null
};
let estacoes;

function initMap() {
    map = new google.maps.Map(document.getElementById("map"), {
        mapTypeId: google.maps.MapTypeId.TERRAIN,
        disableDefaultUI: true,
        zoomControl: true,
        center: { lat: -29.6841, lng: -53.8011 },
        zoom: 8
    });

    estacoes = JSON.parse(document.getElementById('dados-estacoes').textContent);
    criarMarcadoresEstacoes(estacoes);
    exibirMarcadorPrevisao();
}
document.addEventListener('DOMContentLoaded', function() {
    initMap();

    const formPrevisao = document.getElementById('form-previsao');
    if (formPrevisao) {
        formPrevisao.addEventListener('submit', function() {
            const inputLatitude = document.getElementById('latitude');
            const inputLongitude = document.getElementById('longitude');

            if (inputLatitude) {
                inputLatitude.value = inputLatitude.value.replace(',', '.');
            }
            if (inputLongitude) {
                inputLongitude.value = inputLongitude.value.replace(',', '.');
            }
        });
    }

    const aplicarMascaraVirgula = (inputElement) => {
        if (!inputElement) return;

        const onInput = () => {
            const valorOriginal = inputElement.value;
            let valorModificado = valorOriginal;

            valorModificado = valorModificado.replace(/\./g, ',');

            const primeiraVirgula = valorModificado.indexOf(',');
            if (primeiraVirgula !== -1) {
                let parteInteira = valorModificado.substring(0, primeiraVirgula + 1);
                let parteDecimal = valorModificado.substring(primeiraVirgula + 1).replace(/,/g, '');
                valorModificado = parteInteira + parteDecimal;
            }

            if (valorOriginal !== valorModificado) {
                const cursorPosition = inputElement.selectionStart;
                inputElement.value = valorModificado;
                inputElement.setSelectionRange(cursorPosition, cursorPosition);
            }
        };
        inputElement.addEventListener('input', onInput);
    };

    const campoData = document.getElementById('data');
    if (campoData) {
        const hoje = new Date();
        const ano = hoje.getFullYear();
        const mes = String(hoje.getMonth() + 1).padStart(2, '0');
        const dia = String(hoje.getDate()).padStart(2, '0');

        campoData.min = `${ano}-${mes}-${dia}`;
    }

    aplicarMascaraVirgula(document.getElementById('latitude'));
    aplicarMascaraVirgula(document.getElementById('longitude'));
});

function criarMarcadoresEstacoes(estacoes) {
    const bounds = new google.maps.LatLngBounds();

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
            }
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
                </div>`
        });

        marcador.addListener('click', () => {
            infoWindow.open(map, marcador);
        });

        elementosMapa.estacoes[estacao.id] = marcador;
        bounds.extend(marcador.getPosition());
    });

    if (estacoes.length > 0) {
        map.fitBounds(bounds);
    }
}

function exibirMarcadorPrevisao() {
    const dadosPrevisaoEl = document.getElementById('dados-previsao');
    if (!dadosPrevisaoEl || !dadosPrevisaoEl.textContent) {
        return;
    }

    const previsao = JSON.parse(dadosPrevisaoEl.textContent);
    if (!previsao || previsao.latitude == null || previsao.longitude == null) {
        return;
    }

    if (elementosMapa.marcadorPrevisao) {
        elementosMapa.marcadorPrevisao.setMap(null);
    }

    const marcador = new google.maps.Marker({
        position: {
            lat: previsao.latitude,
            lng: previsao.longitude,
        },
        map: map,
        title: `Previsão`,
        icon: {
            url: 'https://img.icons8.com/?size=100&id=15345&format=png',
            scaledSize: new google.maps.Size(16, 16)
        }
    });

    const infoWindow = new google.maps.InfoWindow({
        content: `
            <div class="info-window-conteudo">
                <h3>Previsão de Temperatura</h3>
                <p><strong>Latitude: ${previsao.latitudeFormatada}</strong></p>
                <p><strong>Longitude: ${previsao.longitudeFormatada}</strong></p>
                <p><strong>Temperatura: ${previsao.temperaturaFormatada}</strong></p>
            </div>
        `
    });

    marcador.addListener('click', () => {
        infoWindow.open(map, marcador);
    });

    elementosMapa.marcadorPrevisao = marcador;
    map.setCenter(marcador.getPosition());
    map.setZoom(12);
    infoWindow.open(map, marcador);
}