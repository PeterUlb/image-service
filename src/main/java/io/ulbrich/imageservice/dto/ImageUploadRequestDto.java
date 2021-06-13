package io.ulbrich.imageservice.dto;

import io.ulbrich.imageservice.model.ImagePrivacy;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class ImageUploadRequestDto {
    @NotBlank
    @Size(max = 255)
    private String title;
    @NotNull
    private String description;
    @NotBlank
    @Size(max = 255)
    private String fileName;
    @Max(10485760)
    @NotNull
    private Long size;
    @NotNull
    private ImagePrivacy privacy;
    @Size(max = 20)
    private List<String> tags;

    public ImageUploadRequestDto() {
    }

    public ImageUploadRequestDto(@NotBlank @Size(max = 255) String title, @NotNull String description, @NotBlank @Size(max = 255) String fileName, @Max(10485760) @NotNull Long size, @NotBlank ImagePrivacy privacy, @Size(max = 20) List<String> tags) {
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.size = size;
        this.privacy = privacy;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public ImagePrivacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(ImagePrivacy privacy) {
        this.privacy = privacy;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
