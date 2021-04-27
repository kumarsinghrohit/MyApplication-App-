package com.profile.projection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(value = AccessLevel.PACKAGE)
class AppNameEntity {

    private String name;

    @JsonCreator
    AppNameEntity(@JsonProperty("name") String name) {
        this.name = name;
    }

}
