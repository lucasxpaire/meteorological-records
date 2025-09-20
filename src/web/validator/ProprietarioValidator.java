package web.validator;

import dados.Dados;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import web.command.ProprietarioCommand;

@Component
public class ProprietarioValidator implements Validator {

    @Autowired
    private Dados dados;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(ProprietarioCommand.class);
    }

    @Override
    public void validate(Object o, Errors errors) {

        ProprietarioCommand command = (ProprietarioCommand) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nome", "field.required", "Falha: Não é possível cadastrar sem um nome");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cpf", "field.required", "Falha: Não é possível cadastrar sem um cpf");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "telefone", "field.required", "Falha: Não é possível cadastrar sem um telefone");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cor", "field.required","Falha: Não é possível cadastrar sem uma cor padrão");

        if (command.getCpf() != null && !command.getCpf().isEmpty()) {
            if (!command.getCpf().matches("\\d{11}")) {
                errors.rejectValue("cpf", "cpf.tamanhoInvalido", "Falha: CPF deve conter exatamente 11 dígitos numéricos");
            } else {
                Proprietario proprietarioExistente = dados.buscarUnicoPorCampo(Proprietario.class, "cpf", command.getCpf());
                if (proprietarioExistente != null && !proprietarioExistente.getId().equals(command.getId())) {
                    errors.rejectValue("cpf", "cpf.jaExiste", "Falha: Esse CPF já pertence a outro proprietário");
                }
            }
        }

        if (command.getTelefone() != null && !command.getTelefone().isEmpty()) {
            if (!command.getTelefone().matches("\\d{10,11}")) {
                errors.rejectValue("telefone", "telefone.tamanhoInvalido","Falha: Telefone deve conter 10 ou 11 dígitos numéricos");
            } else {
                Proprietario proprietarioExistente = dados.buscarUnicoPorCampo(Proprietario.class, "telefone", command.getTelefone());
                if (proprietarioExistente != null && !proprietarioExistente.getId().equals(command.getId())) {
                    errors.rejectValue("telefone", "telefone.jaExiste", "Falaha: Esse telefone já pertence a outro proprietário");
                }
            }
        }

    }
}
