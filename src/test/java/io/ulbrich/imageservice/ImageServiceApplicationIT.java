package io.ulbrich.imageservice;

import containers.MockContainerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(MockContainerExtension.class)
@ActiveProfiles("test")
class ImageServiceApplicationIT {

    @Test
    void contextLoads() {
        assertThat(this).isNotNull();
    }

}
