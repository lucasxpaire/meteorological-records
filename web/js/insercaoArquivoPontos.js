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
});

function processarArquivo(arquivo) {
    if (arquivo.name.endsWith(".csv") || arquivo.name.endsWith(".txt")) {
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