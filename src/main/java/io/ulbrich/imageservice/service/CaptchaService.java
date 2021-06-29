package io.ulbrich.imageservice.service;

public interface CaptchaService {
    boolean captchaSolved(String action, String userResponse, String remoteIp);
}
