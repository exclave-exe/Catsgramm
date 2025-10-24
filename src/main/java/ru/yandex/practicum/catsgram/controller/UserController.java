package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        } else if (isUserEmailPresent(user)) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        } else if (isUserEmailPresent(newUser)) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (!(newUser.getEmail() == null) || !(newUser.getUsername() == null) || !(newUser.getPassword() == null)) {
                oldUser.setEmail(newUser.getEmail());
                oldUser.setUsername(newUser.getUsername());
                oldUser.setPassword(newUser.getPassword());
            }
            return oldUser;
        }

        throw new NotFoundException("User с id = " + newUser.getId() + " не найден");
    }

    private boolean isUserEmailPresent(User user) {
        return users.values()
                .stream()
                .anyMatch(currentUser -> Objects.equals(currentUser.getEmail(), user.getEmail()));
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
