package ru.haritonenko.eventmanager.user.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.haritonenko.eventmanager.user.api.role.UserRole;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @NotBlank(message = "User login can not be blank")
    @Size(min = 4, message = "Min login size is 4")
    @Column(unique = true)
    String login;
    @NotBlank(message = "User password can not be blank")
    @Size(min = 4, message = "Min password size is 4")
    @Column(length = 100)
    String password;
    @Min(18)
    int age;
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    UserRole userRole;
}
