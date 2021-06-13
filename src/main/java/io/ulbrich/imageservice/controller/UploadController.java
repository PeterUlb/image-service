package io.ulbrich.imageservice.controller;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import io.ulbrich.imageservice.config.UploadProperties;
import io.ulbrich.imageservice.dto.ImageUploadInfoDto;
import io.ulbrich.imageservice.dto.ImageUploadRequestDto;
import io.ulbrich.imageservice.interceptor.RateLimited;
import io.ulbrich.imageservice.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/image")
public class UploadController {
    private static Logger LOG = LoggerFactory.getLogger(UploadController.class);

    private final Storage storage;
    private final UploadProperties uploadProperties;
    private final ImageService imageService;

    public UploadController(Storage storage, UploadProperties uploadProperties, ImageService imageService) {
        this.storage = storage;
        this.uploadProperties = uploadProperties;
        this.imageService = imageService;
    }

    @PostMapping("/request")
    @RateLimited(group = 1)
    public ResponseEntity<ImageUploadInfoDto> signed(@Valid @RequestBody ImageUploadRequestDto imageUploadRequestDto) {
//        String externalKey = imageService.createImageEntry(imageUploadRequestDto, UUID.fromString(jwt.getSubject()));
        String externalKey = imageService.createImageEntry(imageUploadRequestDto, UUID.randomUUID());

//        List<Acl> aclList;
//        if (imageUploadRequestDto.getPrivacy().equals(ImagePrivacy.PUBLIC)) {
//            aclList = List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
//        } else {
//            aclList = Collections.emptyList();
//        }

        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(uploadProperties.getBucket(), "images/" + externalKey))
                .setContentType(MediaType.IMAGE_PNG_VALUE)
                .setMetadata(Map.of("owner", "TEST")) //jwt.getSubject()))
//                .setAcl(aclList)
                .build();
        Map<String, String> extensionHeaders = new HashMap<>();
        extensionHeaders.put("x-goog-content-length-range", "1," + imageUploadRequestDto.getSize());
        extensionHeaders.put("Content-Type", "image/png");

        URL url =
                storage.signUrl(
                        blobInfo,
                        15,
                        TimeUnit.MINUTES,
                        Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                        Storage.SignUrlOption.withExtHeaders(extensionHeaders),
                        Storage.SignUrlOption.withV4Signature());

        return ResponseEntity.ok(new ImageUploadInfoDto(url.toExternalForm(),
                HttpMethod.PUT.name(),
                Map.of("x-goog-content-length-range", "1," + imageUploadRequestDto.getSize())
        ));
    }

    @GetMapping("/tag/{tag}")
    public Response getImagesByTag(@PathVariable String tag) {
        Set<String> signedUrls = imageService.getSignedUrlsByTag(tag);
        if (signedUrls.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(signedUrls).build();
    }
}
