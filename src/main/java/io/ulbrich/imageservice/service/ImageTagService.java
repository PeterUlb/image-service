package io.ulbrich.imageservice.service;

import io.ulbrich.imageservice.model.ImageTag;

import java.util.List;
import java.util.Optional;

public interface ImageTagService {
    void insertTag(String tag);

    Optional<ImageTag> find(String tag);

    List<ImageTag> find(List<String> tags);
}
