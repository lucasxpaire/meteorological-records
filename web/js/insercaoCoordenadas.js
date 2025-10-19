const areaDeTexto = document.getElementById('pontos');
const inputNomeArquivo = document.getElementById('nomeArquivoPontos');

document.addEventListener('DOMContentLoaded', () => {
    const seletorDeArquivoEscondido = document.getElementById('seletorDeArquivo');
    const linkSelecionarArquivo = document.getElementById('linkSelecionarArquivo');

    linkSelecionarArquivo.addEventListener('click', (event) => {
        event.preventDefault();
        seletorDeArquivoEscondido.click();
    });

    seletorDeArquivoEscondido.addEventListener('change', (event) => {
        if (event.target.files && event.target.files.length > 0) {
            const arquivo = event.target.files[0];
            processarArquivo(arquivo);
        }
    });

    areaDeTexto.addEventListener('dragover', (event) => {
        event.preventDefault();
        areaDeTexto.style.borderColor = '#17A2B8';
    });

    areaDeTexto.addEventListener('dragleave', () => {
        areaDeTexto.style.borderColor = '#CED4DA';
    });

    areaDeTexto.addEventListener('drop', (event) => {
        event.preventDefault();
        areaDeTexto.style.borderColor = '#CED4DA';

        const arquivoCoordenadas = event.dataTransfer.files.item(0);
        processarArquivo(arquivoCoordenadas);
    });

    areaDeTexto.addEventListener('input', () => {
        inputNomeArquivo.value = "";
    });
});

function processarArquivo(arquivo) {
    if (arquivo.type === "text/plain" || arquivo.name.endsWith(".csv") || arquivo.name.endsWith(".txt")) {
        const leitor = new FileReader();
        leitor.onload = () => {
            areaDeTexto.value = leitor.result;
            inputNomeArquivo.value = arquivo.name;
        };

        leitor.readAsText(arquivo);
    } else {
        alert("Falha: Solte apenas arquivos .csv ou .txt.");
    }
}