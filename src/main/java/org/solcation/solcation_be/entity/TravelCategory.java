package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "travel_plan_category_tb")
public class TravelCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="tpc_pk")
    private Long tpcPk;

    @Column(name="tpc_name")
    private String tpcName;

    @Column(name="tpc_icon")
    private String tpcIcon; //파일 경로

    @Column(name = "tpc_code", nullable = false, length = 50)
    private String tpcCode;
}
