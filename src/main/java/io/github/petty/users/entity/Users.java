package io.github.petty.users.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String username; // email

    @Column(nullable = false, length = 60)  // BCrypt로 해시된 비밀번호는 보통 60자의 길이를 가짐
    @ToString.Exclude  // toString()에서 비밀번호가 출력되지 않도록 제외
    @JsonIgnore  // 응답에서 비밀번호가 직렬화되지 않도록 제외
    private String password;

    @Column(nullable = false, length = 50)
    private String displayName;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 20)
    private String role;


    // Oauth2 관련
    @Column(nullable = false, length = 20)
    private String provider; // oauth2 provider

    @Column(length = 100)
    private String providerId; // oauth2 provider id

    @CreationTimestamp
    private LocalDateTime createdAt;
}
