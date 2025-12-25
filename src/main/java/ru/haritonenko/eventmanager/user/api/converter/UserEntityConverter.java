package ru.haritonenko.eventmanager.user.api.converter;

import org.springframework.stereotype.Component;
import ru.haritonenko.eventmanager.user.service.domain.User;
import ru.haritonenko.eventmanager.user.db.entity.UserEntity;

@Component
public class UserEntityConverter {

    public User toDomain(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getLogin(),
                userEntity.getAge(),
                userEntity.getUserRole()
        );
    }
}
