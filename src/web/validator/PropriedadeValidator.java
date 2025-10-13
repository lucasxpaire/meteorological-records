package web.validator;

import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import servico.PropriedadeServico;
import servico.ProprietarioServico;
import web.command.PropriedadeCommand;

@Component
public class PropriedadeValidator implements Validator {

    @Autowired
    private PropriedadeServico propriedadeServico;

    @Autowired
    private ProprietarioServico proprietarioServico;

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
            if (!Proprietario.validarTamanhoCpf(command.getCpfProprietario())) {
                errors.rejectValue("cpfProprietario", "cpf.tamanhoInvalido", "Falha: CPF deve conter exatamente 11 dígitos numéricos");
            } else {
                if (!proprietarioServico.existeComEsseCpf(command.getCpfProprietario())) {
                    errors.rejectValue("cpfProprietario", "cpf.naoExiste", "Falha: Nenhum proprietário encontrado com esse CPF.");
                }
            }
        }

        if (!errors.hasErrors()) {
            try {
                propriedadeServico.validarPoligono(command);
            } catch (IllegalArgumentException e) {
                errors.rejectValue("tipoEntradaPoligono", "poligono.invalido", e.getMessage());
            }
        }
    }
}
