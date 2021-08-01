package io.ulbrich.imageservice.util;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class TikaUtil {
    private final Parser parser;

    public TikaUtil(Parser parser) {
        this.parser = parser;
    }

    public Metadata extractMetadata(InputStream inputStream) throws TikaException, SAXException, IOException {
        var handler = new BodyContentHandler();
        var metadata = new Metadata();
        var context = new ParseContext();
        parser.parse(inputStream, handler, metadata, context);
        return metadata;
    }

    public long getHeight(Metadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException();
        }

        String contentType = getContentType(metadata).orElseThrow();
        switch (contentType) {
            case "image/jpeg":
                String h = metadata.get("Image Height");
                if (h == null) {
                    throw new NoSuchElementException();
                }
                return Long.parseLong(h.substring(0, h.indexOf(" ")));
            case "image/png", "image/gif":
                h = metadata.get("height");
                if (h == null) {
                    throw new NoSuchElementException();
                }
                return Long.parseLong(h);
            default:
                throw new IllegalArgumentException();
        }
    }

    public long getWidth(Metadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException();
        }

        String contentType = getContentType(metadata).orElseThrow();
        switch (contentType) {
            case "image/jpeg":
                String h = metadata.get("Image Width");
                if (h == null) {
                    throw new NoSuchElementException();
                }
                return Long.parseLong(h.substring(0, h.indexOf(" ")));
            case "image/png", "image/gif":
                h = metadata.get("width");
                if (h == null) {
                    throw new NoSuchElementException();
                }
                return Long.parseLong(h);
            default:
                throw new IllegalArgumentException();
        }
    }

    public Optional<String> getContentType(Metadata metadata) {
        String contentType = metadata.get("Content-Type");
        if (contentType != null) {
            contentType = contentType.toLowerCase(Locale.ROOT);
        }
        return Optional.ofNullable(contentType);
    }
}
