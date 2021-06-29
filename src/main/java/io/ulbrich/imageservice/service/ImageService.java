package io.ulbrich.imageservice.service;

import io.ulbrich.imageservice.dto.ImageUploadRequestDto;
import io.ulbrich.imageservice.exception.UnsupportedImageException;

import java.util.Set;
import java.util.UUID;

public interface ImageService {
    String createImageEntry(ImageUploadRequestDto imageUploadRequestDto, UUID accountId);

    void processImageAfterUpload(String imageKey) throws UnsupportedImageException;

    Set<String> getSignedUrlsByTag(String tag);
}
