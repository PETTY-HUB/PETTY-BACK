package io.github.petty.community.form;

public record PostReviewForm(
        String title,
        String content,
        String region,
        String petName,
        String petType
) {
    public static PostReviewForm empty() {
        return new PostReviewForm("", "", "", "", "");
    }
}
