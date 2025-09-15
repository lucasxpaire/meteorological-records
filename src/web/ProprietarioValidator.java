package web;

import dados.Dados;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ProprietarioValidator implements Validator {

    @Autowired
    private Dados dados;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(ProprietarioCommand.class);
    }

    public void validate(Object o, Errors errors) {
        ProprietarioCommand command = (ProprietarioCommand) o;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nome", "field.required", "Não é possível cadastrar uma pessoa com nome nulo");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cpf", "field.required", "Não é possível cadastrar uma pessoa com cpf nulo");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "telefone", "field.required", "Não é possível cadastrar uma pessoa com telefone nulo");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cor", "field.required", "Não é possível cadastrar uma pessoa com cor nula");
        if (command.getId() == null && command.getCpf() != null && !command.getCpf().isEmpty() && dados.existeAlgumComEsseCampo(Proprietario.class, "cpf", command.getCpf())) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cpf", "field.required", "Esse CPF já foi cadastrado para outro Proprietário");
        }
    }
}
