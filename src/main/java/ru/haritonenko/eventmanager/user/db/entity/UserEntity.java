package ru.haritonenko.eventmanager.user.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.haritonenko.eventmanager.event.db.entity.EventEntity;
import ru.haritonenko.eventmanager.user.api.role.UserRole;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "User login can not be blank")
    @Size(min = 4, message = "Min login size is 4")
    @Column(unique = true, nullable = false)
    private String login;

    @NotBlank(message = "User password can not be blank")
    @Size(min = 4, message = "Min password size is 4")
    @Column(length = 100, nullable = false)
    private String password;

    @Min(value = 18, message = "Min age size is 18")
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole userRole;

    @NotNull(message = "User own events can not be null")
    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE)
    private List<EventEntity> ownEvents = new ArrayList<>();

    @NotNull(message = "User booked events can not be null")
    @ManyToMany
    @JoinTable(
            name = "user_booked_events",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "event_id", referencedColumnName = "id")
    )
    private List<EventEntity> bookedEvents = new ArrayList<>();

    public void addOwnEvent(EventEntity e) {
        ownEvents.add(e);
        e.setOwner(this);
    }
}
