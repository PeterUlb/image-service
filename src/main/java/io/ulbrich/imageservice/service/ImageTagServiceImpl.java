package io.ulbrich.imageservice.service;

import io.ulbrich.imageservice.model.ImageTag;
import io.ulbrich.imageservice.repository.ImageTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Optional;

@Service
public class ImageTagServiceImpl implements ImageTagService {
    private final ImageTagRepository imageTagRepository;

    public ImageTagServiceImpl(ImageTagRepository imageTagRepository) {
        this.imageTagRepository = imageTagRepository;
    }

    /**
     * Tries to add the tag to the database. Ignores duplicate exceptions.
     * Note: Since parallel requests might try to insert the same tag, and postgres throws `ERROR: current transaction is aborted, commands ignored until end of transaction block`
     * on duplicate errors, each insert is handed of to a unique transaction
     *
     * @param tag The tag for the image
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void insertTag(String tag) {
        ImageTag imageTag = new ImageTag();
        imageTag.setTag(tag);
        try {
            imageTagRepository.saveAndFlush(imageTag);
        } catch (PersistenceException ignored) {
        }
    }

    @Override
    public Optional<ImageTag> find(String tag) {
        return imageTagRepository.findByTag(tag);
    }

    @Override
    public List<ImageTag> find(List<String> tags) {
        return imageTagRepository.findAllByTagIn(tags);
    }
}
