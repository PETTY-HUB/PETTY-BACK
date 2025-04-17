package io.github.petty.community.entity.post;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_review")
@Getter
@Setter
@ToString(exclude = "post")
@EqualsAndHashCode(of = "postId")
public class PostReview {

    public enum PetType {
        강아지, 고양이, 햄스터, 앵무새, 거북이, 파충류, 기타
    }

    @Id
    private Long postId;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    private String petName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetType petType;

    @Column(nullable = false)
    private String region;
}
