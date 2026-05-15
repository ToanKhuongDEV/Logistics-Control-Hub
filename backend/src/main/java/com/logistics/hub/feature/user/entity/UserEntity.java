package com.logistics.hub.feature.user.entity;

import com.logistics.hub.common.base.BaseEntity;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 255)
    private String email;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String role;

    @OneToMany(mappedBy = "dispatcher", fetch = FetchType.LAZY)
    private List<DepotEntity> assignedDepots = new ArrayList<>();
}
