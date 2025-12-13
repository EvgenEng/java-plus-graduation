package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.UserDto;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        return userRepository.findUsers(ids, from, size).stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public UserDto create(UserDto user) {
        Optional<User> userByEmail = userRepository.findByEmail(user.getEmail());
        if (userByEmail.isPresent()) {
            throw new ru.practicum.exception.ConditionsNotMetException(
                    "Пользователь с таким email уже существует");
        }
        return UserMapper.toUserDto(
                userRepository.save(UserMapper.toUser(user))
        );
    }

    public void delete(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new ru.practicum.exception.NotFoundException(
                        "Пользователь с id=" + userId + " не найден")
        );
        userRepository.deleteById(userId);
    }
}
