document.addEventListener('DOMContentLoaded', function () {
    const textarea = document.getElementById('coordenadaPorInsercaoManual');
    if (!textarea) return;

    let errorContainer = document.getElementById('coordenadas-js-errors');
    if (!errorContainer) {
        errorContainer = document.createElement('div');
        errorContainer.id = 'coordenadas-js-errors';
        errorContainer.className = 'alerta-erro-formulario';
        const springError = textarea.parentNode.querySelector('.alerta-erro-formulario');
        if (springError) {
            springError.parentNode.insertBefore(errorContainer, springError.nextSibling);
        } else {
            textarea.parentNode.appendChild(errorContainer);
        }
    }

    let debounceTimer;
    const debounce = (func, delay) => {
        return function(...args) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                func.apply(this, args);
            }, delay);
        };
    };

    const validarEFormatar = (e) => {
        const lines = e.target.value.split('\n');

        const errors = [];
        const formattedLines = [];

        lines.forEach((line, index) => {
            if (line.trim() === '') {
                formattedLines.push('');
                return;
            }

            const parts = line.split(';');
            if (parts.length !== 2 || parts[0].trim() === '' || parts[1].trim() === '') {
                errors.push(`Linha ${index + 1}: Formato inválido. Use 'XX,XXXX;XX,XXXX'.`);
                formattedLines.push(line);
                return;
            }

            let [latStr, lonStr] = parts;
            let lineHasError = false;

            const lat = parseFloat(latStr.replace(',', '.'));
            if (isNaN(lat) || lat < -90 || lat > 90) {
                errors.push(`Linha ${index + 1}: Latitude (${latStr}) fora do intervalo [-90, 90].`);
                lineHasError = true;
            }

            const lon = parseFloat(lonStr.replace(',', '.'));
            if (isNaN(lon) || lon < -180 || lon > 180) {
                errors.push(`Linha ${index + 1}: Longitude (${lonStr}) fora do intervalo [-180, 180].`);
                lineHasError = true;
            }

            if (!lineHasError) {
                formattedLines.push(`${lat.toFixed(4).replace('.', ',')};${lon.toFixed(4).replace('.', ',')}`);
            } else {
                formattedLines.push(line);
            }
        });

        if (errors.length > 0) {
            textarea.classList.add('is-invalid');
            errorContainer.innerHTML = errors.map(err => `<div>${err}</div>`).join('');
        } else {
            textarea.classList.remove('is-invalid');
            errorContainer.innerHTML = '';
            e.target.value = formattedLines.join('\n');
        }
    };

    const debouncedValidation = debounce(validarEFormatar, 500);

    textarea.addEventListener('input', (e) => {
        e.target.value = e.target.value.replace(/[^\d,;\n-]/g, '');
        debouncedValidation(e);
    });
});