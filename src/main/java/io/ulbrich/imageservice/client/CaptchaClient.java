package io.ulbrich.imageservice.client;

import io.ulbrich.imageservice.dto.CaptchaResponseDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "captcha-api", url = "${srv.captcha.url}")
@ConditionalOnProperty(prefix = "srv.captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
public interface CaptchaClient {
    @GetMapping("/siteverify")
    CaptchaResponseDto verify(@RequestParam(value = "secret") String secret,
                              @RequestParam(value = "response") String response,
                              @RequestParam(value = "remoteip") String remoteIp);
}
