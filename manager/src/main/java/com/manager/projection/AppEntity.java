package com.manager.projection;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(value = AccessLevel.PACKAGE)
class AppEntity {

    private String id;
    private String name;
    private String type;
    private List<AppVersionEntity> appVersions;
    private String developerId;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate createdOn;
    private String lifeCycle;

    @JsonCreator
    AppEntity(@JsonProperty("id") String id, @JsonProperty("name") String name,
            @JsonProperty("type") String type, @JsonProperty("appVersions") List<AppVersionEntity> appVersions,
            @JsonProperty("developerId") String developerId, @JsonProperty("lifeCycle") String lifeCycle,
            @JsonProperty("createdOn") LocalDate createdOn) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.appVersions = appVersions;
        this.developerId = developerId;
        this.lifeCycle = lifeCycle;
        this.createdOn = createdOn;
    }
}
