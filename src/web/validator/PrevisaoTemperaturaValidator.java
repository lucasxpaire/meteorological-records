package web.validator;

import modelo.Ponto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import web.command.PrevisaoTemperaturaCommand;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class PrevisaoTemperaturaValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PrevisaoTemperaturaCommand.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PrevisaoTemperaturaCommand command = (PrevisaoTemperaturaCommand) target;

        if (command.getLatitude() == null) {
            errors.rejectValue("latitude", "campo.obrigatorio", "A latitude é obrigatória.");
        } else if (!Ponto.validarLatitude(command.getLatitude())) {
            errors.rejectValue("latitude", "latitude.invalida", "Latitude inválida. O valor deve estar entre -90 e 90.");
        }

        if (command.getLongitude() == null) {
            errors.rejectValue("longitude", "campo.obrigatorio", "A longitude é obrigatória.");
        } else if (!Ponto.validarLongitude(command.getLongitude())) {
            errors.rejectValue("longitude", "longitude.invalida", "Longitude inválida. O valor deve estar entre -180 e 180.");
        }

        if (command.getData() == null) {
            errors.rejectValue("data", "campo.obrigatorio", "A data é obrigatória.");
        } else if (command.getData().isBefore(LocalDate.now())) {
            errors.rejectValue("data", "data.passado", "A data da previsão não pode ser no passado.");
        }

        if (command.getHora() == null) {
            errors.rejectValue("hora", "campo.obrigatorio", "A hora é obrigatória.");
        }

        if (command.getData() != null && command.getHora() != null) {
            if (command.getData().isEqual(LocalDate.now()) && command.getHora() < LocalTime.now().getHour()) {
                errors.rejectValue("hora", "hora.passado", "Para o dia de hoje, a hora da previsão não pode ser no passado.");
            }
        }
    }
}