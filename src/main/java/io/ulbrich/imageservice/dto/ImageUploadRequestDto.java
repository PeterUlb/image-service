package io.ulbrich.imageservice.dto;

import io.ulbrich.imageservice.model.ImagePrivacy;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public record ImageUploadRequestDto(
        @NotBlank
        @Size(max = 255)
        String title,
        @NotNull
        String description,
        @NotBlank
        @Size(max = 255)
        String mimeType,
        @NotBlank
        @Size(max = 255)
        String fileName,
        @Max(10485760)
        @NotNull
        Long size,
        @NotNull
        ImagePrivacy privacy,
        @Size(max = 20)
        List<String> tags) {

}
