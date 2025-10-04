package web;


import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class CoordenadasMultiPartFile implements MultipartFile {

    private final byte[] conteudo;
    private final String nomeArquivo;
    private final String nomeOriginal;
    private final String tipoConteudo;

    public CoordenadasMultiPartFile(String conteudo, String nomeArquivo, String nomeOriginal, String tipoConteudo) {
        this.conteudo = conteudo.getBytes();
        this.nomeArquivo = nomeArquivo;
        this.nomeOriginal = nomeOriginal;
        this.tipoConteudo = tipoConteudo;
    }

    @Override
    public String getName() {
        return nomeArquivo;
    }

    @Override
    public String getOriginalFilename() {
        return nomeOriginal;
    }

    @Override
    public String getContentType() {
        return tipoConteudo;
    }

    @Override
    public boolean isEmpty() {
        return conteudo == null || conteudo.length == 0;
    }

    @Override
    public long getSize() {
        return conteudo.length;
    }

    @Override
    public byte[] getBytes() {
        return conteudo;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(conteudo);
    }

    @Override
    public void transferTo(File arquivo) throws IOException, IllegalStateException {
        try (FileOutputStream arquivoDestino = new FileOutputStream(arquivo)) {
            arquivoDestino.write(conteudo);
        }
    }
}
