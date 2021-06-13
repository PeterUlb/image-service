package io.ulbrich.imageservice.controller;

import io.ulbrich.imageservice.client.CountryClient;
import io.ulbrich.imageservice.model.Country;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class CountryDemoControllerTest {

    private final CountryClient countryClient = Mockito.mock(CountryClient.class);
    private final CountryDemoController countryDemoController = new CountryDemoController(countryClient);

    @Test
    void name() {
        List<Country> exp = Lists.list(new Country("GermanyTest", "DE", "BerlinTest", Lists.emptyList()));
        given(countryClient.getByName("Germany")).willReturn(exp);

        //when
        List<Country> countries = countryDemoController.name("Germany");

        then(countryClient).should().getByName("Germany");

        assertThat(countries).isEqualTo(exp);
    }
}