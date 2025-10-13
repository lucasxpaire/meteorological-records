package web.validator;

import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import servico.ProprietarioServico;
import web.command.ProprietarioCommand;

@Component
public class ProprietarioValidator implements Validator {

    @Autowired
    private ProprietarioServico proprietarioServico;

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
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "corId", "field.required","Falha: Não é possível cadastrar sem uma cor padrão");

        if (command.getCpf() != null && !command.getCpf().isEmpty()) {
            if (!Proprietario.validarTamanhoCpf(command.getCpf())) {
                errors.rejectValue("cpf", "cpf.tamanhoInvalido", "Falha: CPF deve conter exatamente 11 dígitos numéricos");
            } else {
                if (proprietarioServico.existeComEsseCpf(command.getCpf())) {
                    if (proprietarioServico.cpfPertenceAOutroProprietario(command.getId(), command.getCpf())) {
                        errors.rejectValue("cpf", "cpf.jaExiste", "Falha: Esse CPF já pertence a outro proprietário");
                    }
                }
            }
        }

        if (command.getTelefone() != null && !command.getTelefone().isEmpty()) {
            if (!Proprietario.validarTamanhoTelefone(command.getTelefone())) {
                errors.rejectValue("telefone", "telefone.tamanhoInvalido","Falha: Telefone deve conter 11 dígitos numéricos");
            } else {
                if (proprietarioServico.existeComEsseTelefone(command.getTelefone())) {
                    if (proprietarioServico.telefonePertenceAOutroProprietario(command.getId(), command.getTelefone())) {
                        errors.rejectValue("telefone", "telefone.jaExiste", "Falha: Esse telefone já pertence a outro proprietário");
                    }
                }
            }
        }
    }
}
