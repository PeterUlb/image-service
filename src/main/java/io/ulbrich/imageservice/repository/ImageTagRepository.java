package io.ulbrich.imageservice.repository;

import io.ulbrich.imageservice.model.ImageTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageTagRepository extends JpaRepository<ImageTag, Long> {
    Optional<ImageTag> findByTag(String tag);

    List<ImageTag> findAllByTagIn(List<String> tags);
}
