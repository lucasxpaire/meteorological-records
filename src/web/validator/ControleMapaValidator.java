package web.validator;

import modelo.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import servico.PropriedadeServico;
import servico.ProprietarioServico;
import web.command.ControleMapaCommand;
import web.controller.MapaController;

@Component
public class ControleMapaValidator implements Validator {

    @Autowired
    private ProprietarioServico proprietarioServico;
    @Autowired
    private PropriedadeServico propriedadeServico;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(ControleMapaCommand.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ControleMapaCommand command = (ControleMapaCommand) o;

        if (command.getOpcaoSelecionada() != null) {
            String opcao = MapaController.OPCOES_CONTROLE_MAPA.get(command.getOpcaoSelecionada());

            if (opcao.equalsIgnoreCase(MapaController.BUSCAR_PROPRIEDADES_POR_CPF)) {
                if (command.getCpfBusca().isEmpty() || command.getCpfBusca() == null) {
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cpfBusca", "field.required", "Falha: O CPF do proprietário está vazio.");
                } else {
                    if (!Proprietario.validarTamanhoCpf(command.getCpfBusca())) {
                        errors.rejectValue("cpfBusca", "cpfBusca.tamanhoInvalido", "Falha: CPF de busca deve conter exatamente 11 dígitos numéricos.");
                    } else {
                        if (proprietarioServico.existeComEsseCpf(command.getCpfBusca())) {
                            if (!propriedadeServico.existePropriedadesNesseCpf(command.getCpfBusca())) {
                                errors.rejectValue("cpfBusca", "cpfBusca.nenhumaPropriedade", "Falha: Nenhuma propriedade cadastrada nesse CPF.");
                            }
                        } else {
                            errors.rejectValue("cpfBusca", "cpfBusca.naoExiste", "Falha: CPF não está cadastrado no sistema.");
                        }
                    }
                }
            } else if (opcao.equalsIgnoreCase(MapaController.BUSCAR_PROPRIEDADES_POR_NOME)) {
                if (command.getNomeBusca().isEmpty() || command.getNomeBusca() == null) {
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nomeBusca", "field.required", "Falha: O nome da propriedade para buscar está vazia.");
                } else {
                    if (!propriedadeServico.existePropriedadesNesseNome(command.getNomeBusca())) {
                        errors.rejectValue("nomeBusca", "nomeBusca.nenhumaPropriedade", "Falha: Nenhuma propriedade cadastrada com esse nome.");
                    }
                }
            }
        }

    }
}