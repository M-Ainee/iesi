package io.metadew.iesi.data.generation.configuration;

import io.metadew.iesi.data.generation.execution.GenerationComponentExecution;
import io.metadew.iesi.data.generation.execution.GenerationDataExecution;

public class Address extends GenerationComponentExecution {

    public Address(GenerationDataExecution execution) {
        super(execution);
    }

    public String cityPrefix() {
        return fetch("address.city_prefix");
    }

    public String citySuffix() {
        return fetch("address.city_suffix");
    }

    public String country() {
        return fetch("address.country");
    }

    public String countryCode() {
        return fetch("address.country_code");
    }

    public String buildingNumber() {
        return numerify(fetch("address.building_number"));
    }

    public String streetSuffix() {
        return fetch("address.street_suffix");
    }

    public String secondaryAddress() {
        return fetch("address.secondary_address");
    }

    public String postcode() {
        return fetch("address.postcode");
    }

    public String state() {
        return fetch("address.state");
    }

    public String stateAbbr() {
        return fetch("address.state_abbr");
    }

    public String timeZone() {
        return fetch("address.time_zone");
    }

    public String city() {
        return parse(fetch("address.city"));
    }

    public String streetName() {
        return parse(fetch("address.street_name"));
    }

    public String streetAddress() {
        return parse(fetch("address.street_address"));
    }

    public String defaultCountry() {
        return fetch("address.default_country");
    }
}
