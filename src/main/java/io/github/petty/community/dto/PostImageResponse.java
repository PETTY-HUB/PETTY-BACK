package io.github.petty.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostImageResponse {
    private Long id;
    private String imageUrl;
    private Integer ordering;
}