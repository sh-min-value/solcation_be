package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "terms_category_tb")
public class TermsCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="terms_pk")
    private Long termsPk;

    @Column(name="terms_code")
    private String termsCode;
}
