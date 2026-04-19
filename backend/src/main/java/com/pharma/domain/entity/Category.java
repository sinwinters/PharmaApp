package com.pharma.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "requires_prescription", nullable = false)
    @Builder.Default
    private Boolean requiresPrescription = false;

    @Column(name = "requires_strict_control", nullable = false)
    @Builder.Default
    private Boolean requiresStrictControl = false;

    @Column(name = "requires_verification", nullable = false)
    @Builder.Default
    private Boolean requiresVerification = false;
}
