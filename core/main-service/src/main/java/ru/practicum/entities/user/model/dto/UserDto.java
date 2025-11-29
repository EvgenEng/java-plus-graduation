package ru.practicum.entities.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.exception.ExceptionMessages;
import ru.practicum.exception.OptionalParams;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @NotBlank(message = "Почтовый адрес не может быть пустым")
    @Email(message = "Некорректный формат почтового адреса")
    @Size(min = OptionalParams.MinUserEmailSize, message = ExceptionMessages.UserEmailMinLenghtError)
    @Size(max = OptionalParams.MaxUserEmailSize, message = ExceptionMessages.UserEmailMaxLenghtError)
    private String email;

    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = OptionalParams.MinUserNameSize, message = ExceptionMessages.UserNameMinLenghtError)
    @Size(max = OptionalParams.MaxUserNameSize, message = ExceptionMessages.UserNameMaxLenghtError)
    private String name;
}