package io.ulbrich.imageservice.repository;

import io.ulbrich.imageservice.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByExternalKey(String externalKey);
}
