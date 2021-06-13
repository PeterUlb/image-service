package io.ulbrich.imageservice.dto;

import java.util.Map;

public class ImageUploadInfoDto {
    private String url;
    private String httpMethod;
    private Map<String, String> headers;

    public ImageUploadInfoDto(String url, String httpMethod, Map<String, String> headers) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
