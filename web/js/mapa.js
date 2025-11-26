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
    'Município', 'Temperatura', 'Precipitação', 'Radiação solar'
];

const COLUNAS_TABELA_REGISTROS = [
    'Temperatura', 'Precipitação', 'Radiação solar'
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
        <p><strong>Estação meteorológica:</strong> ${estacao.nome}</p>
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

    const estiloDataHora = "font-size: 0.85em; color: #7a7a7a;";
    const linhasTabela = [
        [
            temperatura + '<br>' + formatarDataHora(estacao.localizacao.dataHoraRegistroRealMaisRecente, estiloDataHora),
            precipitacao + '<br>' + formatarDataHora(estacao.localizacao.dataHoraRegistroRealMaisRecente, estiloDataHora),
            radiacao + '<br>' + formatarDataHora(estacao.localizacao.dataHoraRegistroRealMaisRecente, estiloDataHora)
        ]
    ];

    descricaoConteudo.appendChild(criarTabela(estacao.id, COLUNAS_TABELA_REGISTROS, linhasTabela, 'Registro meteorológico'));

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
        });

        centroide.addListener('click', (event) => {
            descricaoCentroide.setPosition(event.latLng);
            descricaoCentroide.open(mapa);
            linhasTracejadas.forEach(linha => linha.setVisible(true));
        });

        centroide.addListener('closeclick', () => {
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
        <p><strong>Propriedade:</strong> ${propriedade.nome}</p>
        <p><strong>Proprietário:</strong> ${propriedade.nomeProprietario}</p>
        <p><strong>CPF:</strong> ${propriedade.cpfProprietario}</p>
    `;
    descricaoConteudo.appendChild(cabecalho);

    const temperaturaCalculada = obterTextoFormatado(propriedade.centroide.temperaturaCalculadaMaisRecente, CASAS_DECIMAIS_PADRAO, GRAUS_CELSIUS);
    const precipitacaoCalculada = obterTextoFormatado(propriedade.centroide.precipitacaoCalculadaMaisRecente, CASAS_DECIMAIS_PRECIPITACAO, PRECIPITACAO_MM);
    const radiacaoCalculada = obterTextoFormatado(propriedade.centroide.radiacaoSolarCalculadaMaisRecente, CASAS_DECIMAIS_PADRAO, RADIACAO_SOLAR_KJ_M2);

    const estiloDataHora = "font-size: 0.85em; color: #7a7a7a;";
    const linhasRegistroCalculado = [
        [
            temperaturaCalculada + '<br>' + formatarDataHora(propriedade.centroide.dataHoraRegistroCalculadoMaisRecente, estiloDataHora),
            precipitacaoCalculada + '<br>' + formatarDataHora(propriedade.centroide.dataHoraRegistroCalculadoMaisRecente, estiloDataHora),
            radiacaoCalculada + '<br>' + formatarDataHora(propriedade.centroide.dataHoraRegistroCalculadoMaisRecente, estiloDataHora)
        ]
    ];

    const tabelaRegistrosEstacoes = gerarHtmlEstacoesAssociadas(propriedade, propriedade.centroide.dataHoraRegistroCalculadoMaisRecente);

    const idBalao = `balao-estacoes-${propriedade.id}`;
    const htmlBalao = `
        <div id="${idBalao}" class="balao-estacoes">
            ${tabelaRegistrosEstacoes.outerHTML}
        </div>
    `;

    const tituloTabelaCalculada = `
        <div class="cabecalho-com-lupa">
            <span>Registro meteorológico calculado</span>
            <div class="container-lupa">
                <img src="https://img.icons8.com/?size=100&id=59878&format=png&color=000000" class="icone-lupa" title="Ver estações utilizadas" onclick="alternarBalaoEstacoes('${idBalao}')" alt="Lupa"/>
                ${htmlBalao}
            </div>
        </div>
    `;

    descricaoConteudo.appendChild(criarTabela(`registroCalculado-${propriedade.id}`, COLUNAS_TABELA_REGISTROS, linhasRegistroCalculado, tituloTabelaCalculada));

    const divBotao = document.createElement('div');
    const idTemperaturaPrevista = `temperatura-prevista-propriedade-${propriedade.id}`;
    const idBotaoPrevisao = `botao-previsao-${propriedade.id}`;

    divBotao.innerHTML = `
        <p style="display: none" id="${idTemperaturaPrevista}"></p>
        <button id="${idBotaoPrevisao}" onclick="criarDescricaoRegistroPrevisto(${propriedade.centroide.id}, '${idTemperaturaPrevista}', '${idBotaoPrevisao}')" class="botao botao-tabela--visualizar">Prever registro meteorológico para ${calcularProximaDataHoraPrevisao()}</button>
    `;
    descricaoConteudo.appendChild(divBotao);

    return new google.maps.InfoWindow({
        content: descricaoConteudo
    });
}

function gerarHtmlEstacoesAssociadas(propriedade, dataHoraRegistroCalculadoMaisRecente) {
    const linhasTabela = propriedade.centroide.estacoesMeteorologicas.map(estacao => {
        const dataHoraReal = estacao.localizacao.dataHoraRegistroRealMaisRecente;

        let ehPrevista = false;
        if (dataHoraRegistroCalculadoMaisRecente && dataHoraReal !== dataHoraRegistroCalculadoMaisRecente) {
            ehPrevista = true;
        }

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

        const estiloDataHora = "font-size: 0.85em; color: #7a7a7a;";
        return [
            estacao.nome + '<br>' + formatarPesosEntreEstacoesECentroide(estacao.nome, propriedade.centroide.pesosDoPontoEntreEstacoes),
            formatarDado(textoTemperatura) + '<br>' + formatarDataHora(dataHora, estiloDataHora),
            formatarDado(textoPrecipitacao) + '<br>' + formatarDataHora(dataHora, estiloDataHora),
            formatarDado(textoRadiacao) + '<br>' + formatarDataHora(dataHora, estiloDataHora)
        ];
    });

    return criarTabela(`estacoes-associadas-${propriedade.id}`, COLUNAS_TABELA_ESTACOES_ASSOCIADAS, linhasTabela, "Registros meteorológicos das estações utilizadas no cálculo");
}

function formatarPesosEntreEstacoesECentroide(nomeEstacao, pesos) {
    if (!pesos || typeof pesos !== 'object') {
        return TEXTO_INDISPONIVEL;
    }

    const valor = pesos[nomeEstacao];
    if (typeof valor !== 'number') {
        return TEXTO_INDISPONIVEL;
    }

    return formatarPeso(valor);
}

function calcularProximaDataHoraPrevisao() {
    const data = new Date();

    data.setHours(data.getHours() + 1);
    data.setMinutes(0);

    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();

    const horas = String(data.getHours()).padStart(2, '0');
    const minutos = String(data.getMinutes()).padStart(2, '0');

    return `${dia}/${mes}/${ano} às ${horas}:${minutos}`;
}

function criarDescricaoCentroide(propriedade) {
    return new google.maps.InfoWindow({
        content: `
            <div class="info-window-cabecalho">
                <p><strong>Centróide</strong></p>
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

async function obterRegistroPrevisto(idCentroide) {
    try {
        const resposta = await fetch(`${urlPrevisao}?idCentroide=${idCentroide}`);
        if (!resposta.ok) {
            throw new Error(`Falha na requisição: ${resposta.status}`);
        }
        return await resposta.json();
    } catch (error) {
        return null;
    }
}

async function criarDescricaoRegistroPrevisto(idCentroide, idTemperaturaPrevista, idBotaoPrevisao) {
    const paragrafoTemperatura = document.getElementById(idTemperaturaPrevista);
    const botaoPrevisao = document.getElementById(idBotaoPrevisao);

    botaoPrevisao.disabled = true;
    botaoPrevisao.textContent = 'Carregando...';

    try {
        const json = await obterRegistroPrevisto(idCentroide);

        paragrafoTemperatura.style.display = VISIVEL;

        const estiloDataHora = "font-size: 0.85em; color: #7a7a7a;";
        const linhasTabela = [
            [
                obterTextoFormatado(json.temperaturaPrevista, CASAS_DECIMAIS_PADRAO, GRAUS_CELSIUS) + '<br>' + formatarDataHora(json.dataHoraPrevisao, estiloDataHora),
                obterTextoFormatado(json.precipitacaoPrevista, CASAS_DECIMAIS_PRECIPITACAO, PRECIPITACAO_MM) + '<br>' + formatarDataHora(json.dataHoraPrevisao, estiloDataHora),
                obterTextoFormatado(json.radiacaoSolarPrevista, CASAS_DECIMAIS_PADRAO, RADIACAO_SOLAR_KJ_M2) + '<br>' + formatarDataHora(json.dataHoraPrevisao, estiloDataHora)
            ]
        ];

        const tabelaPrevisao = criarTabela(`previsao-${idCentroide}`, COLUNAS_TABELA_REGISTROS, linhasTabela, "Registro meteorológico previsto");

        paragrafoTemperatura.innerHTML = '';
        paragrafoTemperatura.appendChild(tabelaPrevisao);

        botaoPrevisao.disabled = false;
        const dataHoraPrevisaoTexto = formatarDataHoraSimples(json.dataHoraPrevisao);
        botaoPrevisao.textContent = `Prever registro meteorológico para ${dataHoraPrevisaoTexto}`;
    } catch (error) {
        paragrafoTemperatura.style.display = VISIVEL;

        const linhasTabela = [
            ['Indisponível', 'Indisponível', 'Indisponível']
        ];

        const tabelaPrevisao = criarTabela(`previsao-${idCentroide}`, COLUNAS_TABELA_REGISTROS, linhasTabela, "Registro meteorológico previsto");

        paragrafoTemperatura.innerHTML = '';
        paragrafoTemperatura.appendChild(tabelaPrevisao);

        botaoPrevisao.disabled = false;
        botaoPrevisao.textContent = `Prever registro meteorológico para ${calcularProximaDataHoraPrevisao()}`;
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
        thTitulo.innerHTML = tituloTabela;
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

function formatarDataHora(dataHora, estiloDataHora) {
    if (!dataHora) {
        return '';
    }
    const dataHoraFormatada = dataHora.replace(' ', ' às ');
    return `<span style="${estiloDataHora}">(${dataHoraFormatada})</span>`;
}

function formatarDataHoraSimples(dataHora) {
    if (!dataHora) {
        return '';
    }
    return dataHora.replace(' ', ' às ');
}

function alterarBalao(elemento) {
    elemento.classList.toggle('ativo');
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

function formatarPeso(valor) {
    const numero = Number(valor);
    if (Number.isNaN(numero)) {
        return TEXTO_INDISPONIVEL;
    }
    return `${(numero * 100).toFixed(1).replace('.', ',')}%`;
}

function alternarBalaoEstacoes(idBalao) {
    const balao = document.getElementById(idBalao);
    if (!balao) {
        return;
    }

    balao.classList.toggle('ativo');
}