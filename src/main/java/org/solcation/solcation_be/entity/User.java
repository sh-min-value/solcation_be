package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.entity.converter.UserGenderConverter;
import org.solcation.solcation_be.entity.converter.UserRoleConverter;
import org.solcation.solcation_be.entity.enums.GENDER;
import org.solcation.solcation_be.entity.enums.ROLE;
import org.solcation.solcation_be.util.security.AesGcmAttributeConverter;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "user_tb")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_pk")
    private Long userPk;

    @Column(
            name = "user_id",
            unique = true,
            nullable = false,
            length = 10
    )
    private String userId;

    @Column(
            name = "user_pw",
            nullable = false,
            length = 100
    )
    private String userPw;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(
            name = "street_addr",
            nullable = false,
            length = 100
    )
    private String streetAddr;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(
            name = "addr_detail",
            nullable = false,
            length = 100
    )
    private String addrDetail;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(
            name = "postal_code",
            nullable = false,
            length = 5
    )
    private String postalCode;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(
            name = "tel",
            nullable = false,
            length = 11,
            unique = true
    )
    private String tel;

    @Column(
            name = "user_name",
            nullable = false,
            length = 30
    )
    private String userName;

    @Column(
            name = "date_of_birth",
            nullable = false
    )
    private LocalDate dateOfBirth;

    @Column(
            name = "gender",
            nullable = false
    )
    @Convert(converter = UserGenderConverter.class)
    private GENDER gender;

    @Column(
            name = "role",
            nullable = false
    )
    @Convert(converter = UserRoleConverter.class)
    private ROLE role;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(
            name = "email",
            nullable = false,
            length = 100
    )
    private String email;

    public void setPwEncoding(String pw) {
        this.userPw = pw;
    }
}
