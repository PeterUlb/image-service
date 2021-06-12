package io.ulbrich.imageservice.client;

import io.ulbrich.imageservice.model.Country;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "country-api", url = "${srv.country-api.url}")
@RequestMapping(value = "/v2")
public interface CountryClient {
    @RequestMapping(method = RequestMethod.GET, value = "/name/{name}")
    List<Country> getByName(@PathVariable String name);
}
