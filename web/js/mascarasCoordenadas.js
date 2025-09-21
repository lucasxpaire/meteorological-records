document.addEventListener('DOMContentLoaded', function() {
    const inputCoordenadas = document.getElementById('coordenadasPorInsercaoManual');

    if (inputCoordenadas) {
        inputCoordenadas.addEventListener('input', aplicarMascaraCoordenadas);
        inputCoordenadas.addEventListener('keydown', gerenciarTeclaEnter);
    }
});

function aplicarMascaraCoordenadas(event) {
    const input = event.target;
    let valor = input.value;

    valor = valor.replace(/[^0-9,;]/g, '');

    valor = valor.replace(/,{2,}/g, ',').replace(/;{2,}/g, ';');

    let valorFormatado = '';
    const coordenadas = valor.split(';');

    coordenadas.forEach((coord, index) => {
        let digitos = coord.replace(/[^\d]/g, '');
        let coordenadaFormatada = '';

        if (digitos.length > 0) {
            coordenadaFormatada += digitos.substring(0, 2);
        }
        if (digitos.length > 2) {
            coordenadaFormatada += ',' + digitos.substring(2, 6);
        }

        valorFormatado += coordenadaFormatada;

        if (index < coordenadas.length - 1) {
            valorFormatado += ';';
        }

        input.value = valorFormatado;
    })

    function gerenciarTeclaEnter(event) {
        if (event.key === 'Enter') {
            const input = event.target;
            if (input.value.slice(-1) === ';') {
                event.preventDefault();
                input.value += '\n';
            }
        }
    }
}