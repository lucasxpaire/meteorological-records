package web.controller;

import modelo.Arquivo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import servico.ArquivoServico;

@Controller
public class ArquivoController {

    @Autowired
    private ArquivoServico arquivoServico;

    @GetMapping("/arquivo/baixar/{idArquivo}")
    public ResponseEntity<byte[]> baixarArquivo(@PathVariable Long idArquivo) {
        Arquivo arquivo = arquivoServico.buscarPorId(idArquivo);

        if (arquivo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders cabecalho = new HttpHeaders();
        cabecalho.setContentType(MediaType.parseMediaType(arquivo.getTipoConteudo()));
        cabecalho.setContentDispositionFormData("attachment", arquivo.getNomeOriginal());
        cabecalho.setContentLength(arquivo.getConteudo().length);

        return new ResponseEntity<>(arquivo.getConteudo(), cabecalho, HttpStatus.OK);
    }
}
