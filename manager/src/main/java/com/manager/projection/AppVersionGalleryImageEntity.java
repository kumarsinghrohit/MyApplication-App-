package com.manager.projection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(value = AccessLevel.PACKAGE)
class AppVersionGalleryImageEntity {

    private String thumbnailImageName;
    private String profileImageName;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    AppVersionGalleryImageEntity(@JsonProperty("thumbnailImageName") String thumbnailImageName,
            @JsonProperty("profileImageName") String profileImageName) {
        this.thumbnailImageName = thumbnailImageName;
        this.profileImageName = profileImageName;
    }

}
