package io.ulbrich.imageservice;

import containers.Containers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ExtendWith(Containers.class)
@ActiveProfiles("test")
class ImageServiceApplicationIT {

    @Test
    void contextLoads() {
    }

}
