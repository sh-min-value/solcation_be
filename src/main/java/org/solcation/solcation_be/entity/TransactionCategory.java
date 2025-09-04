package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "transaction_category_tb")
public class TransactionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="tc_pk")
    private Long tcPk;

    @Column(name="tc_name")
    private String tcName;

    @Column(name="tc_icon")
    private String tcIcon; //파일 경로

}
