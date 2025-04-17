package io.github.petty.community.form;

public record PostShowoffForm(
        String title,
        String content,
        String petType
) {
    public static PostShowoffForm empty() {
        return new PostShowoffForm("", "", "");
    }
}

