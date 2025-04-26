package io.github.petty.community.enums;

public enum PetType {
    DOG("강아지"),
    CAT("고양이"),
    RABBIT("토끼"),
    HAMSTER("햄스터"),
    PARROT("앵무새"),
    REPTILE("파충류"),
    OTHER("기타");

    private final String label;

    PetType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
