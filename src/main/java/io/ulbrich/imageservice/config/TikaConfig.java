package io.ulbrich.imageservice.config;

import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Objects;

@Configuration
public class TikaConfig {

    @Bean
    public Parser parser() throws TikaException, IOException, SAXException {
        return new AutoDetectParser(new org.apache.tika.config.TikaConfig(Objects.requireNonNull(getClass().getResource("/tika-config.xml"))));
    }
}
