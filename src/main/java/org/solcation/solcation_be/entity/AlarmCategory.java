package org.solcation.solcation_be.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alarm_category_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlarmCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ac_pk")
    private Long acPk;

    @Column(name = "ac_name", nullable = false, length = 50)
    private String acName;

    @Column(name = "ac_dest", nullable = false, length = 50)
    private String acDest;
}
