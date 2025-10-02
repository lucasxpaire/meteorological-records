package web.validator;

import dados.Dados;
import modelo.Poligono;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;
import util.FormatadorUtil;
import util.PoligonoUtil;
import web.StringMultipartFile;
import web.command.PropriedadeCommand;

@Component
public class PropriedadeValidator implements Validator {

    @Autowired
    private Dados dados;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(PropriedadeCommand.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PropriedadeCommand command = (PropriedadeCommand) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nome", "field.required", "Falha: Não é possível cadastrar sem um nome.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tipoEntradaPoligono", "field.required", "Falha: Selecione um método para a definição do polígono.");

        if (command.getIdProprietario() == null && command.getCpfProprietario() != null) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cpfProprietario", "field.required", "Falha: Não é possível cadastrar sem um cpf");
            if (!errors.hasFieldErrors("cpfProprietario")) {
                String cpfSemFormatacao = FormatadorUtil.removerFormatacaoCpf(command.getCpfProprietario());
                if (!cpfSemFormatacao.matches("\\d{11}")) {
                    errors.rejectValue("cpfProprietario", "cpf.tamanhoInvalido", "Falha: CPF deve conter exatamente 11 dígitos numéricos");
                }
                if (!dados.existeAlgumComEsseCampo(Proprietario.class, "cpf", cpfSemFormatacao)) {
                    errors.rejectValue("cpfProprietario", "cpf.naoExiste", "Falha: Nenhum proprietário encontrado com esse CPF.");
                }
            }
        }

        if (!errors.hasFieldErrors("tipoEntradaPoligono")) {
            MultipartFile arquivoRecebido = null;

            if (PoligonoUtil.TIPO_MANUAL.equals(command.getTipoEntradaPoligono())) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "coordenadasPorInsercaoManual", "field.required", "Falha: As coordenadas manuais são obrigatórias");
                if (!errors.hasFieldErrors("coordenadasPorInsercaoManual")) {
                    arquivoRecebido = new StringMultipartFile(command.getCoordenadasPorInsercaoManual(), "coordenadas", "coordenadas.txt", "text/plain");
                }
            } else if (PoligonoUtil.TIPO_ARQUIVO.equals(command.getTipoEntradaPoligono())) {
                if (command.getCoordenadasPorArquivo() == null || command.getCoordenadasPorArquivo().isEmpty()) {
                    errors.rejectValue("coordenadasPorArquivo", "field.required", "Falha: O arquivo de coordenadas é obrigatório.");
                } else {
                    arquivoRecebido = command.getCoordenadasPorArquivo();
                }
            }

            if (arquivoRecebido != null && !arquivoRecebido.isEmpty()) {
                try {
                    Poligono poligono = PoligonoUtil.criarPoligonoPorArquivo(arquivoRecebido);
                    if (poligono.getPontos().size() < Poligono.QUANTIDADE_MINIMA_DE_PONTOS) {
                        errors.rejectValue("tipoEntradaPoligono", "poligono.pontosInsuficientes", "Falha: O polígono deve possuir ao menos 3 pontos.");
                    }
                    if (poligono.possuiAutoIntersecao()) {
                        errors.rejectValue("tipoEntradaPoligono", "poligono.autoIntersecao", "Falha: O polígono possui auto-interseção.");
                    }
                } catch (Exception e) {
                    errors.rejectValue("tipoEntradaPoligono", "formato.invalido", "Falha: As coordenadas estão em um formato inválido.");
                }
            }
        }
    }

}
