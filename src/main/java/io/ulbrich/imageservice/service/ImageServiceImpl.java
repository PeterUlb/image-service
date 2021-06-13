package io.ulbrich.imageservice.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import io.ulbrich.imageservice.config.UploadProperties;
import io.ulbrich.imageservice.dto.ImageUploadRequestDto;
import io.ulbrich.imageservice.exception.UnsupportedImageException;
import io.ulbrich.imageservice.model.Image;
import io.ulbrich.imageservice.model.ImageStatus;
import io.ulbrich.imageservice.model.ImageTag;
import io.ulbrich.imageservice.repository.ImageRepository;
import io.ulbrich.imageservice.util.TikaUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageServiceImpl implements ImageService {
    private static final Logger LOG = LoggerFactory.getLogger(ImageServiceImpl.class);

    private final ImageRepository imageRepository;
    private final ImageTagService imageTagService;
    private final TikaUtil tikaUtil;
    private final Storage storage;
    private final UploadProperties uploadProperties;

    public ImageServiceImpl(ImageRepository imageRepository, ImageTagService imageTagService, TikaUtil tikaUtil, Storage storage, UploadProperties uploadProperties) {
        this.imageRepository = imageRepository;
        this.imageTagService = imageTagService;
        this.tikaUtil = tikaUtil;
        this.storage = storage;
        this.uploadProperties = uploadProperties;
    }

    @Override
    @Transactional
    public String createImageEntry(ImageUploadRequestDto imageUploadRequestDto, UUID accountId) {
        Set<ImageTag> tags;
        // First try to insert the missing tags
        if (imageUploadRequestDto.getTags() != null && !imageUploadRequestDto.getTags().isEmpty()) {
            insertMissingImageTags(imageUploadRequestDto.getTags());
            // Now get all ids for the relevant tags, at this point all must exist
            tags = imageUploadRequestDto.getTags().stream().map(s -> imageTagService.find(s).orElseThrow()).collect(Collectors.toSet());
        } else {
            tags = null;
        }

        Image image = Image.withInitialState(generateExternalKey(),
                accountId,
                imageUploadRequestDto.getTitle(),
                imageUploadRequestDto.getDescription(),
                imageUploadRequestDto.getFileName(),
                imageUploadRequestDto.getSize(),
                imageUploadRequestDto.getPrivacy(),
                tags);
        imageRepository.save(image);
        return image.getExternalKey();
    }

    @Override
    @Transactional(noRollbackFor = UnsupportedImageException.class)
    public void processImageAfterUpload(String imageKey) throws UnsupportedImageException {
        String externalId = imageKey;
        int pos = imageKey.lastIndexOf("/");
        if (pos != -1) {
            externalId = imageKey.substring(pos + 1);
        }

        Metadata metadata;
        Blob blob = storage.get(uploadProperties.getBucket(), imageKey);
        if (blob == null) {
            return;
        }

        try (InputStream inputStream = Channels.newInputStream(blob.reader())) {
            metadata = tikaUtil.extractMetadata(inputStream);
        } catch (IOException | TikaException | SAXException e) {
            LOG.error("Error while reading object: " + e.getMessage());
            imageRepository.findByExternalKey(externalId).ifPresent(image -> image.setImageStatus(ImageStatus.REJECTED));
            return;
        }

        String mimeType = tikaUtil.getContentType(metadata).orElseThrow(); // TODO
        LOG.debug(mimeType);

        boolean supported = switch (mimeType) {
            case "image/png", "image/jpeg", "image/gif" -> true;
            default -> false;
        };

        if (!supported) {
            // TODO: DELETE, SET IMAGE STATUS, ...
            imageRepository.findByExternalKey(externalId).ifPresent(image -> image.setImageStatus(ImageStatus.REJECTED));
            return;
        }

        long height = tikaUtil.getHeight(metadata);
        long width = tikaUtil.getWidth(metadata);
        LOG.debug("Height: " + height);
        LOG.debug("Width: " + width);

        Image image = imageRepository.findByExternalKey(externalId).orElse(null);
        if (image == null) {
            LOG.error("Could not find db entry for object in message " + imageKey);
            return;
        }

        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        String mimeTypeExtension = null;
        try {
            mimeTypeExtension = allTypes.forName(mimeType).getExtension();
        } catch (MimeTypeException e) {
            mimeTypeExtension = ""; // None
        }
        image.setImageStatus(ImageStatus.VERIFIED);
        image.setWidth(width);
        image.setHeight(height);
        image.setMimeType(mimeType);
        image.setExtension(mimeTypeExtension);
    }

    @Override
    public Set<String> getSignedUrlsByTag(String tag) {
        return null;
    }

    private void insertMissingImageTags(List<String> tags) {
        Set<String> existingTags = imageTagService.find(tags).stream().map(ImageTag::getTag).collect(Collectors.toSet());

        int inserted = 0;
        for (String tag : tags) {
            if (!existingTags.contains(tag)) {
                imageTagService.insertTag(tag);
                inserted++;
            }
        }

        LOG.debug(String.format("Inserted %d missing tags", inserted));
    }

    private String generateExternalKey() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
