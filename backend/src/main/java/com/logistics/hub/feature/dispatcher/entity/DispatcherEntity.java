package com.logistics.hub.feature.dispatcher.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dispatchers")
@Getter
@Setter
@NoArgsConstructor
public class DispatcherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String role;

    public DispatcherEntity(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }
}
