package io.ulbrich.imageservice.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String externalKey;

    @Column(nullable = false)
    private UUID accountId;

    @Column
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private String filename;

    @Column
    private String extension;

    @Column
    private String mimeType;

    @Column
    private Long size;

    @Column
    private Long width;

    @Column
    private Long height;

    @Column
    @Enumerated(EnumType.STRING)
    private ImagePrivacy privacy;

    @ManyToMany
    @JoinTable(
            name = "image_image_tag",
            joinColumns = @JoinColumn(name = "image_id"),
            inverseJoinColumns = @JoinColumn(name = "image_tag_id"))
    private Set<ImageTag> tags;

    @Column
    @Enumerated(EnumType.STRING)
    private ImageStatus imageStatus;

    @Column(nullable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, insertable = false)
    private LocalDateTime updatedAt;

    public static Image withInitialState(String externalKey, UUID accountId, String title, String description, String fileName, Long size, ImagePrivacy privacy, Set<ImageTag> tags) {
        Image image = new Image();
        image.setExternalKey(externalKey);
        image.setAccountId(accountId);
        image.setTitle(title);
        image.setDescription(description);
        image.setFilename(fileName);
        image.setSize(size);
        image.setImageStatus(ImageStatus.REQUESTED);
        image.setTags(tags);
        image.setPrivacy(privacy);
        return image;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(String externalKey) {
        this.externalKey = externalKey;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String fileName) {
        this.filename = fileName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public ImagePrivacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(ImagePrivacy privacy) {
        this.privacy = privacy;
    }

    public Set<ImageTag> getTags() {
        return tags;
    }

    public void setTags(Set<ImageTag> tags) {
        this.tags = tags;
    }

    public ImageStatus getImageStatus() {
        return imageStatus;
    }

    public void setImageStatus(ImageStatus imageStatus) {
        this.imageStatus = imageStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
