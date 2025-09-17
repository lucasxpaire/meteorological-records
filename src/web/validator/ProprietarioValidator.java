package web.validator;

import dados.Dados;
import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
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

    public void validate(Object objeto, Errors errors) {
        ProprietarioCommand proprietario = (ProprietarioCommand) objeto;

        if (proprietario.getNome() == null || proprietario.getNome().trim().isEmpty()) {
            errors.rejectValue("nome", "campo.obrigatorio", "Falha: Não é possível cadastrar sem um nome");
        }
        if (proprietario.getCpf() == null || proprietario.getCpf().trim().isEmpty()) {
            errors.rejectValue("cpf", "campo.obrigatorio", "Falha: Não é possível cadastrar sem um cpf");
        }
        if (proprietario.getTelefone() == null || proprietario.getTelefone().trim().isEmpty()) {
            errors.rejectValue("telefone", "campo.obrigatorio", "Falha: Não é possível cadastrar sem um telefone");
        }

        if (proprietario.getCor() == null || proprietario.getCor().getId() == null) {
            errors.rejectValue("cor", "campo.obrigatorio", "Falha: Não é possível cadastrar sem uma cor padrão");
        }

        if (proprietario.getCpf() != null && !proprietario.getCpf().isEmpty()) {
            if (!proprietario.getCpf().matches("\\d{11}")) {
                errors.rejectValue("cpf", "cpf.tamanhoInvalido", "Falha: CPF deve conter exatamente 11 dígitos numéricos");
            } else if (proprietario.getId() == null && dados.existeAlgumComEsseCampo(Proprietario.class, "cpf", proprietario.getCpf())) {
                errors.rejectValue("cpf", "cpf.jaExiste", "Falha: Esse CPF já está cadastrado para outro proprietário");
            }
        }

        if (proprietario.getTelefone() != null && !proprietario.getTelefone().isEmpty()) {
            if (!proprietario.getTelefone().matches("\\d{10,11}")) {
                errors.rejectValue("telefone", "telefone.tamanhoInvalido","Falha: Telefone deve conter 10 ou 11 dígitos numéricos");
            } else if (proprietario.getId() == null && dados.existeAlgumComEsseCampo(Proprietario.class, "telefone", proprietario.getTelefone())) {
                errors.rejectValue("telefone", "telefone.jaExiste", "Falha: Esse telefone já está cadastrado para outro proprietário");
            }
        }

    }
}
