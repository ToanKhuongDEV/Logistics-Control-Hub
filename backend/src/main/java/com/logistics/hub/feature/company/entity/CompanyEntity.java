package com.logistics.hub.feature.company.entity;

import com.logistics.hub.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "companies")
@SQLRestriction("deleted = false")
@Getter
@Setter
public class CompanyEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    private String email;

    private String website;

    @Column(name = "tax_id")
    private String taxId;

    @Column(columnDefinition = "TEXT")
    private String description;
}
