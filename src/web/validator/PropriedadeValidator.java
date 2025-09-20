package web.validator;

import dados.Dados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import web.command.PropriedadeCommand;
import web.command.ProprietarioCommand;

@Component
public class PropriedadeValidator implements Validator {

    @Autowired
    private Dados dados;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(ProprietarioCommand.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PropriedadeCommand command = (PropriedadeCommand) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nome", "field.required", "Falha: Não é possível cadastrar sem um nome");

    }

}
