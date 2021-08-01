package io.ulbrich.imageservice.controller;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import io.ulbrich.imageservice.config.properties.ServiceProperties;
import io.ulbrich.imageservice.dto.ImageUploadInfoDto;
import io.ulbrich.imageservice.dto.ImageUploadRequestDto;
import io.ulbrich.imageservice.exception.UnsupportedImageException;
import io.ulbrich.imageservice.interceptor.CaptchaProtected;
import io.ulbrich.imageservice.interceptor.RateLimited;
import io.ulbrich.imageservice.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/image")
public class UploadController {
    private final Storage storage;
    private final ServiceProperties serviceProperties;
    private final ImageService imageService;

    public UploadController(Storage storage, ServiceProperties serviceProperties, ImageService imageService) {
        this.storage = storage;
        this.serviceProperties = serviceProperties;
        this.imageService = imageService;
    }

    @PostMapping("/request")
    @RateLimited(group = 1)
    @CaptchaProtected(action = "SIGN_UPLOAD")
    public ResponseEntity<ImageUploadInfoDto> signed(@Valid @RequestBody ImageUploadRequestDto imageUploadRequestDto) {
        if (!imageService.isSupportedContentType(imageUploadRequestDto.mimeType())) {
            throw new UnsupportedImageException(imageUploadRequestDto.mimeType());
        }

        String externalKey = imageService.createImageEntry(imageUploadRequestDto, UUID.randomUUID());

        var blobInfo = BlobInfo.newBuilder(BlobId.of(serviceProperties.getUpload().getBucket(), "images/" + externalKey))
                .setContentType(MediaType.IMAGE_PNG_VALUE)
                .setMetadata(Map.of("owner", "TEST")) //jwt.getSubject()))
                .build();
        Map<String, String> extensionHeaders = new HashMap<>();
        extensionHeaders.put("x-goog-content-length-range", "1," + imageUploadRequestDto.size());
        extensionHeaders.put("Content-Type", imageUploadRequestDto.mimeType().toLowerCase(Locale.ROOT));

        var url =
                storage.signUrl(
                        blobInfo,
                        15,
                        TimeUnit.MINUTES,
                        Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                        Storage.SignUrlOption.withExtHeaders(extensionHeaders),
                        Storage.SignUrlOption.withV4Signature());

        return ResponseEntity.ok(new ImageUploadInfoDto(url.toExternalForm(),
                HttpMethod.PUT.name(),
                Map.of("x-goog-content-length-range", "1," + imageUploadRequestDto.size())
        ));
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<Set<String>> getImagesByTag(@PathVariable String tag) {
        Set<String> signedUrls = imageService.getSignedUrlsByTag(tag);
        if (signedUrls.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(signedUrls);
    }
}
