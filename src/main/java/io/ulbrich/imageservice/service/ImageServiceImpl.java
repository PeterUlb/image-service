package io.ulbrich.imageservice.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import io.ulbrich.imageservice.config.properties.ServiceProperties;
import io.ulbrich.imageservice.dto.ImageUploadRequestDto;
import io.ulbrich.imageservice.exception.UnsupportedImageException;
import io.ulbrich.imageservice.model.Image;
import io.ulbrich.imageservice.model.ImageStatus;
import io.ulbrich.imageservice.model.ImageTag;
import io.ulbrich.imageservice.repository.ImageRepository;
import io.ulbrich.imageservice.util.TikaUtil;
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
import java.nio.channels.Channels;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ImageServiceImpl implements ImageService {
    private static final Logger LOG = LoggerFactory.getLogger(ImageServiceImpl.class);

    private final ImageRepository imageRepository;
    private final ImageTagService imageTagService;
    private final TikaUtil tikaUtil;
    private final Storage storage;
    private final ServiceProperties serviceProperties;

    public ImageServiceImpl(ImageRepository imageRepository, ImageTagService imageTagService, TikaUtil tikaUtil, Storage storage, ServiceProperties serviceProperties) {
        this.imageRepository = imageRepository;
        this.imageTagService = imageTagService;
        this.tikaUtil = tikaUtil;
        this.storage = storage;
        this.serviceProperties = serviceProperties;
    }

    @Override
    @Transactional
    public String createImageEntry(ImageUploadRequestDto imageUploadRequestDto, UUID accountId) {
        Set<ImageTag> tags;
        // First try to insert the missing tags
        if (imageUploadRequestDto.tags() != null && !imageUploadRequestDto.tags().isEmpty()) {
            insertMissingImageTags(imageUploadRequestDto.tags());
            // Now get all ids for the relevant tags, at this point all must exist
            tags = imageUploadRequestDto.tags().stream().map(s -> imageTagService.find(s).orElseThrow()).collect(Collectors.toSet());
        } else {
            tags = null;
        }

        var image = Image.withInitialState(accountId, imageUploadRequestDto, tags);
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
        var blob = storage.get(serviceProperties.getUpload().getBucket(), imageKey);
        if (blob == null) {
            LOG.error("Could not find storage object for {}", imageKey);
            return;
        }

        var image = imageRepository.findByExternalKey(externalId).orElse(null);
        if (image == null) {
            LOG.error("Could not find db entry for object in message {}", imageKey);
            return;
        }

        try (var inputStream = Channels.newInputStream(blob.reader())) {
            metadata = tikaUtil.extractMetadata(inputStream);
        } catch (IOException | TikaException | SAXException | NoSuchFieldError e) {
            LOG.error("Error while reading object: " + e.getMessage());
            image.setImageStatus(ImageStatus.REJECTED);
            return;
        } catch (Exception e) {
            // E.g. PDF Parser likes to throw NoSuchFieldException, basically any parser could throw an exception
            // for some reason (which isn't a TikaException), so in this case, we also reject
            LOG.error("Unexpected exception while reading object: {}", e.getMessage());
            image.setImageStatus(ImageStatus.REJECTED);
            return;
        }

        var mimeType = tikaUtil.getContentType(metadata).orElseThrow(); // TODO
        LOG.debug(mimeType);

        if (!isSupportedContentType(mimeType) || !image.getMimeType().equals(mimeType)) {
            LOG.warn("Reject image of type {}, expected {}", mimeType, image.getMimeType());
            image.setImageStatus(ImageStatus.REJECTED);
            image.setWidth(null);
            image.setHeight(null);
            image.setExtension(null);
            image.setSize(blob.getSize());
            storage.delete(serviceProperties.getUpload().getBucket(), imageKey);
            return;
        }

        long height = tikaUtil.getHeight(metadata);
        long width = tikaUtil.getWidth(metadata);
        LOG.debug("Height: {}", height);
        LOG.debug("Width: {}", width);

        var allTypes = MimeTypes.getDefaultMimeTypes();
        String mimeTypeExtension;
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
        // Set the real size. The requested one is actually the maximum allowed and might differ
        image.setSize(blob.getSize());
    }

    @Override
    @Transactional
    public Set<String> getSignedUrlsByTag(String tag) {
        var imageTag = imageTagService.find(tag).orElse(null);
        if (imageTag == null) {
            return Collections.emptySet();
        }

        Set<Image> images = imageTag.getImages();
        Set<String> signedUrls = new HashSet<>(images.size());
        for (Image image : images) {
            if (image.getImageStatus() != ImageStatus.VERIFIED) {
                continue;
            }

            var blobInfo = BlobInfo.newBuilder(BlobId.of(serviceProperties.getUpload().getBucket(), "images/" + image.getExternalKey())).build();
            var url = storage.signUrl(blobInfo, 10, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
            signedUrls.add(url.toExternalForm());
        }

        return signedUrls;
    }

    @Override
    public boolean isSupportedContentType(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png", "image/jpeg", "image/gif" -> true;
            default -> false;
        };
    }

    private void insertMissingImageTags(List<String> tags) {
        Set<String> existingTags = imageTagService.find(tags).stream().map(ImageTag::getTag).collect(Collectors.toSet());

        var inserted = 0;
        for (String tag : tags) {
            if (!existingTags.contains(tag)) {
                imageTagService.insertTag(tag);
                inserted++;
            }
        }

        LOG.debug("Inserted {} missing tags", inserted);
    }
}
