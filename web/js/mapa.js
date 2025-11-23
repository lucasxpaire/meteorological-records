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

const COLUNAS_TABELA_ESTACOES_ASSOCIADAS = [
    'Município', 'Temperatura', 'Precipitação', 'Radiação Solar'
];

const COLUNAS_TABELA_REGISTROS = [
    'Temperatura', 'Precipitação', 'Radiação Solar'
];

const GRAUS_CELSIUS = '°C';
const PRECIPITACAO_MM = ' mm';
const RADIACAO_SOLAR_KJ_M2 = ' kJ/m²';
const TEXTO_INDISPONIVEL = 'Indisponível';

const CASAS_DECIMAIS_PADRAO = 2;
const CASAS_DECIMAIS_PRECIPITACAO = 0;

document.addEventListener('DOMContentLoaded', () => {
    iniciarMapa();
    inicializarControlesDoMapa();
    criarElementosEstacoes();
    criarElementosPropriedades();
    ajustarVisualizacaoParaPropriedades();
});

function inicializarControlesDoMapa() {
    const opcaoSelecionada = document.getElementById('opcaoSelecionada');

    if (opcaoSelecionada) {
        opcaoSelecionada.addEventListener('change', configurarVisibilidadeCamposDeBusca);
        configurarVisibilidadeCamposDeBusca();
    }
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
    const descricaoConteudo = document.createElement('div');
    descricaoConteudo.className = 'info-window-conteudo';

    const cabecalho = document.createElement('div');
    cabecalho.className = 'info-window-cabecalho';

    cabecalho.innerHTML = `
        <h3>Estação Meteorológica: ${estacao.nome}</h3>
        <p><strong>Latitude:</strong> ${estacao.localizacao.latitudeFormatada}</p>
        <p><strong>Longitude:</strong> ${estacao.localizacao.longitudeFormatada}</p>
        <p><strong>Código:</strong> ${estacao.codigoEstacao}</p>
        <p><strong>Situação:</strong> ${estacao.situacao}</p>
        <p><strong>Tipo:</strong> ${estacao.tipoEstacao}</p>
    `;

    descricaoConteudo.appendChild(cabecalho);

    const temperatura = obterTextoFormatado(estacao.localizacao.temperaturaRealMaisRecente, CASAS_DECIMAIS_PADRAO, GRAUS_CELSIUS);
    const precipitacao = obterTextoFormatado(estacao.localizacao.precipitacaoRealMaisRecente, CASAS_DECIMAIS_PRECIPITACAO, PRECIPITACAO_MM);
    const radiacao = obterTextoFormatado(estacao.localizacao.radiacaoSolarRealMaisRecente, CASAS_DECIMAIS_PADRAO, RADIACAO_SOLAR_KJ_M2);
    const linhasTabela = [
        [
            temperatura + '<br>' + formatarDataHora(estacao.localizacao.dataHoraRegistroRealMaisRecente),
            precipitacao + '<br>' + formatarDataHora(estacao.localizacao.dataHoraRegistroRealMaisRecente),
            radiacao + '<br>' + formatarDataHora(estacao.localizacao.dataHoraRegistroRealMaisRecente)
        ]
    ];

    descricaoConteudo.appendChild(criarTabela(estacao.id, COLUNAS_TABELA_REGISTROS, linhasTabela, 'Registro Meteorológico'));

    return new google.maps.InfoWindow({
        content: descricaoConteudo
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
        });

        centroide.addListener('click', (event) => {
            descricaoCentroide.setPosition(event.latLng);
            descricaoCentroide.open(mapa);
        })

        descricaoPropriedade.addListener('closeclick', () => {
            linhasTracejadas.forEach(linha => linha.setVisible(false));
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
        zIndex: 1,
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
        zIndex: 2,
        map: mapa
    });
}

function criarDescricaoPropriedade(propriedade) {
    const descricaoConteudo = document.createElement('div');
    descricaoConteudo.className = 'info-window-conteudo';

    const cabecalho = document.createElement('div');
    cabecalho.className = 'info-window-cabecalho'
    cabecalho.innerHTML = `
        <h3>Propriedade: ${propriedade.nome}</h3>
        <p><strong>Proprietário:</strong> ${propriedade.nomeProprietario}</p>
        <p><strong>CPF:</strong> ${propriedade.cpfProprietario}</p>
        <p><strong>Cor:</strong> ${propriedade.corNome}</p>
    `;

    descricaoConteudo.appendChild(cabecalho);

    const tabelaRegistrosEstacoes = gerarHtmlEstacoesAssociadas(propriedade, propriedade.centroide.dataHoraRegistroCalculadoMaisRecente);
    descricaoConteudo.appendChild(tabelaRegistrosEstacoes);

    const temperaturaCalculada = obterTextoFormatado(propriedade.centroide.temperaturaCalculadaMaisRecente, CASAS_DECIMAIS_PADRAO, GRAUS_CELSIUS);
    const precipitacaoCalculada = obterTextoFormatado(propriedade.centroide.precipitacaoCalculadaMaisRecente, CASAS_DECIMAIS_PRECIPITACAO, PRECIPITACAO_MM);
    const radiacaoCalculada = obterTextoFormatado(propriedade.centroide.radiacaoSolarCalculadaMaisRecente, CASAS_DECIMAIS_PADRAO, RADIACAO_SOLAR_KJ_M2);
    const linhasRegistroCalculado = [
        [
            temperaturaCalculada + '<br>' + formatarDataHora(propriedade.centroide.dataHoraRegistroCalculadoMaisRecente),
            precipitacaoCalculada + '<br>' + formatarDataHora(propriedade.centroide.dataHoraRegistroCalculadoMaisRecente),
            radiacaoCalculada + '<br>' + formatarDataHora(propriedade.centroide.dataHoraRegistroCalculadoMaisRecente)
        ]
    ];
    descricaoConteudo.appendChild(criarTabela(`registroCalculado-${propriedade.id}`, COLUNAS_TABELA_REGISTROS, linhasRegistroCalculado, 'Registro Meteorológico Calculado'));

    const divBotao = document.createElement('div');
    const idTemperaturaPrevista = `temperatura-prevista-propriedade-${propriedade.id}`;
    const idBotaoPrevisao = `botao-previsao-${propriedade.id}`;

    divBotao.innerHTML = `
        <p style="display: none" id="${idTemperaturaPrevista}"></p>
        <button id="${idBotaoPrevisao}" onclick="preverTemperatura(${propriedade.centroide.id}, '${idTemperaturaPrevista}', '${idBotaoPrevisao}')" class="botao botao-tabela--visualizar">Prever registro meteorológico</button>
    `;
    descricaoConteudo.appendChild(divBotao);

    return new google.maps.InfoWindow({
        content: descricaoConteudo
    });
}

function gerarHtmlEstacoesAssociadas(propriedade, dataHoraRegistroCalculadoMaisRecente) {
    const linhasTabela = propriedade.centroide.estacoesMeteorologicas.map(estacao => {
        const dataHoraReal = estacao.localizacao.dataHoraRegistroRealMaisRecente;

        const ehPrevista = dataHoraRegistroCalculadoMaisRecente && dataHoraReal !== dataHoraRegistroCalculadoMaisRecente;

        let temperaturaValor;
        let precipitacaoValor;
        let radiacaoSolarValor;
        let dataHora;
        if (ehPrevista) {
            temperaturaValor = estacao.localizacao.temperaturaPrevistaMaisRecente;
            precipitacaoValor = estacao.localizacao.precipitacaoPrevistaMaisRecente;
            radiacaoSolarValor = estacao.localizacao.radiacaoSolarPrevistaMaisRecente;
            dataHora = estacao.localizacao.dataHoraRegistroPrevistoMaisRecente;
        } else {
            temperaturaValor = estacao.localizacao.temperaturaRealMaisRecente;
            precipitacaoValor = estacao.localizacao.precipitacaoRealMaisRecente;
            radiacaoSolarValor = estacao.localizacao.radiacaoSolarRealMaisRecente;
            dataHora = estacao.localizacao.dataHoraRegistroRealMaisRecente;
        }

        const textoTemperatura = obterTextoFormatado(temperaturaValor, CASAS_DECIMAIS_PADRAO, GRAUS_CELSIUS);
        const textoPrecipitacao = obterTextoFormatado(precipitacaoValor, CASAS_DECIMAIS_PRECIPITACAO, PRECIPITACAO_MM);
        const textoRadiacao = obterTextoFormatado(radiacaoSolarValor, CASAS_DECIMAIS_PADRAO, RADIACAO_SOLAR_KJ_M2);

        const formatarDado = (valor) => {
            if (ehPrevista) {
                return `
                    <span class="valor-previsto" onclick="alterarBalao(this)">
                        ${valor}
                        <span class="balao-prevista">Prevista</span>
                    </span>
                `;
            }
            return valor;
        };

        return [
            estacao.nome,
            formatarDado(textoTemperatura) + '<br>' + formatarDataHora(dataHora),
            formatarDado(textoPrecipitacao) + '<br>' + formatarDataHora(dataHora),
            formatarDado(textoRadiacao) + '<br>' + formatarDataHora(dataHora)
        ];
    });

    return criarTabela(`estacoes-associadas-${propriedade.id}`, COLUNAS_TABELA_ESTACOES_ASSOCIADAS, linhasTabela, "Registros Meteorológicos das Estações Associadas");
}

function criarDescricaoCentroide(propriedade) {
    return new google.maps.InfoWindow({
        content: `
            <div class="info-window-cabecalho">
                <h3>Centróide</h3>
                <p><strong>Latitude:</strong> ${propriedade.centroide.latitudeFormatada}</p>
                <p><strong>Longitude:</strong> ${propriedade.centroide.longitudeFormatada}</p>
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

function formatarValor(valor, quantidadeCasasDecimais) {
    if (valor === null || valor === undefined) {
        return null;
    }
    return valor.toLocaleString('pt-BR', {
        minimumFractionDigits: quantidadeCasasDecimais,
        maximumFractionDigits: quantidadeCasasDecimais
    });
}

function obterTextoFormatado(valor, casasDecimais, unidade) {
    const valorFormatado = formatarValor(valor, casasDecimais);
    if (valorFormatado === null) {
        return TEXTO_INDISPONIVEL;
    }
    return valorFormatado + unidade;
}

async function preverTemperatura(idCentroide, idTemperaturaPrevista, idBotaoPrevisao) {
    const paragrafoTemperatura = document.getElementById(idTemperaturaPrevista);
    const botaoPrevisao = document.getElementById(idBotaoPrevisao);

    botaoPrevisao.disabled = true;
    botaoPrevisao.innerHTML = 'Carregando...';

    try {
        const resposta = await fetch(`${urlPrevisao}?idCentroide=${idCentroide}`);
        if (!resposta.ok) {
            throw new Error(`Falha na requisição: ${resposta.status}`);
        }
        const json = await resposta.json();

        paragrafoTemperatura.style.display = VISIVEL;

        const linhasTabela = [
            [
                obterTextoFormatado(json.temperaturaPrevista, CASAS_DECIMAIS_PADRAO, GRAUS_CELSIUS) + '<br>' + formatarDataHora(json.dataHoraPrevisao),
                obterTextoFormatado(json.precipitacaoPrevista, CASAS_DECIMAIS_PRECIPITACAO, PRECIPITACAO_MM) + '<br>' + formatarDataHora(json.dataHoraPrevisao),
                obterTextoFormatado(json.radiacaoSolarPrevista, CASAS_DECIMAIS_PADRAO, RADIACAO_SOLAR_KJ_M2) + '<br>' + formatarDataHora(json.dataHoraPrevisao)
            ]
        ];

        const tabelaPrevisao = criarTabela(`previsao-${idCentroide}`, COLUNAS_TABELA_REGISTROS, linhasTabela, "Registro Meteorológico Previsto");

        paragrafoTemperatura.innerHTML = '';
        paragrafoTemperatura.appendChild(tabelaPrevisao);
    } catch (error) {
        paragrafoTemperatura.style.display = VISIVEL;

        const linhasTabela = [
            ['Indisponível', 'Indisponível', 'Indisponível']
        ];

        const tabelaPrevisao = criarTabela(`previsao-${idCentroide}`, COLUNAS_TABELA_REGISTROS, linhasTabela, "Registro Meteorológico Previsto");

        paragrafoTemperatura.innerHTML = '';
        paragrafoTemperatura.appendChild(tabelaPrevisao);
    } finally {
        botaoPrevisao.disabled = false;
        botaoPrevisao.innerHTML = 'Prever registro meteorológico';
    }
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
        });

        google.maps.event.addListenerOnce(mapa, 'idle', () => {
            mapa.fitBounds(limites);
        });
    } else {
        mapa.setCenter(COORDENADA_CENTRAL_BRASIL);
        mapa.setZoom(ZOOM_PADRAO);
    }
}

function criarTabela(idTabela, dadosColunas, dadosLinhas, tituloTabela) {
    const tabela = document.createElement("table");

    tabela.id = idTabela;

    const thead = document.createElement("thead");

    if (tituloTabela) {
        const trTitulo = document.createElement("tr");
        const thTitulo = document.createElement("th");
        thTitulo.textContent = tituloTabela;
        thTitulo.colSpan = dadosColunas.length;
        trTitulo.appendChild(thTitulo);
        thead.appendChild(trTitulo);
    }

    const trHead = document.createElement("tr");
    dadosColunas.forEach(coluna => {
        const th = document.createElement("th");
        th.textContent = coluna;
        trHead.appendChild(th);
    });
    thead.appendChild(trHead);
    tabela.appendChild(thead);

    const tbody = document.createElement("tbody");

    dadosLinhas.forEach(linha => {
        const tr = document.createElement("tr");
        linha.forEach(valor => {
            const td = document.createElement("td");
            td.innerHTML = valor;
            tr.appendChild(td);
        });

        tbody.appendChild(tr);
    });

    tabela.appendChild(tbody);

    return tabela;
}

function formatarDataHora(dataHora) {
    if (!dataHora) {
        return '';
    }
    const dataHoraFormatada = dataHora.replace(' ', ' às ');
    return `<span style="font-size: 0.85em; color: #7a7a7a;">(${dataHoraFormatada})</span>`;
}

function alterarBalao(elemento) {
    elemento.classList.toggle('ativo');
}