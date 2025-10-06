    document.addEventListener('DOMContentLoaded', function () {
    const textarea = document.getElementById('coordenadaPorInsercaoManual');

    if (!textarea) {
        return;
    }

    const aplicarMascaraDeVirgula = () => {
        const cursorPosition = textarea.selectionStart;
        const valorOriginal = textarea.value;

        const valorModificado = valorOriginal.replace(/\./g, ',');

        if (valorOriginal !== valorModificado) {
            textarea.value = valorModificado;
            textarea.setSelectionRange(cursorPosition, cursorPosition);
        }
    };

    textarea.addEventListener('input', aplicarMascaraDeVirgula);
});