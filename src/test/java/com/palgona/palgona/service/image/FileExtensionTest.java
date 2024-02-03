package com.palgona.palgona.service.image;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FileExtensionTest {

    @ParameterizedTest
    @ValueSource(strings = {"file.jpeg", "file.jpg", "file.jfif", "file.png", "file.svg"})
    void 올바른_파일_확장자_테스트(String fileName) {
        String extension = FileExtension.from(fileName).getExtension();
        Assertions.assertThat(fileName).contains(extension);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file.qwe", "file.asd", "file.zxcv"})
    void 잘못된_파일_확장자_테스트(String fileName) {
        Assertions.assertThatThrownBy(() -> FileExtension.from(fileName))
                .isInstanceOf(IllegalArgumentException.class);
    }
}