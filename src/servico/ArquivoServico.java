package servico;

import dados.Dados;
import modelo.Arquivo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ArquivoServico {

    @Autowired
    private Dados dados;

    public Arquivo salvar(MultipartFile multipartFile) throws IOException {
        Arquivo arquivo = new Arquivo();
        arquivo.setNomeOriginal(multipartFile.getOriginalFilename());
        arquivo.setTipoConteudo(multipartFile.getContentType());
        arquivo.setConteudo(multipartFile.getBytes());

        return dados.salvar(arquivo);
    }

    public Arquivo buscarPorId(Long id) {
        return dados.buscarUnicoPorCampo(Arquivo.class, "id", id);
    }

    public void deletar(Arquivo arquivo) {
        dados.deletar(arquivo);
    }
}
