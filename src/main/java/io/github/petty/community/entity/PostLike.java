package io.github.petty.community.entity;

import io.github.petty.users.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes") // ✅ 테이블 명 명확히
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // ✅ JPA용 기본 생성자
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"post", "user"})
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false) // ✅ 명시적 join column
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public PostLike(Post post, Users user) {
        this.post = post;
        this.user = user;
    }
}
