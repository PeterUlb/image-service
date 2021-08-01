package io.ulbrich.imageservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import containers.MockContainerExtension;
import io.ulbrich.imageservice.dto.ImageUploadRequestDto;
import io.ulbrich.imageservice.model.ImagePrivacy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockContainerExtension.class)
@ActiveProfiles("test")
class UploadControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void name() throws Exception {
        mockMvc.perform(get("/country/{name}", "germany")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name")
                        .value("MockDeutschland"))
                .andExpect(jsonPath("$[0].capital")
                        .value("MockBerlin"));
    }

    @Test
    void testSignRequestSizeRejected() throws Exception {
        long size = 50 * 1024 * 1024;
        ImageUploadRequestDto hugeImage = new ImageUploadRequestDto("Huge Image", "A really huge image", "image/png", "test.png", size, ImagePrivacy.PRIVATE, List.of("test"));

        var objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(hugeImage);

        mockMvc.perform(post("/api/v1/image/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignRequestAccepted() throws Exception {
        long size = 3 * 1024 * 1024;
        ImageUploadRequestDto normalImage = new ImageUploadRequestDto("Normal Image", "Just a normal image", "image/png", "test.png", size, ImagePrivacy.PRIVATE, List.of("test"));

        var objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(normalImage);

        mockMvc.perform(post("/api/v1/image/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }
}