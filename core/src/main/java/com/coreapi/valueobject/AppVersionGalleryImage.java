package com.coreapi.valueobject;

import lombok.ToString;
import lombok.Value;

@Value
@ToString(includeFieldNames = true)
public final class AppVersionGalleryImage {

    private String thumbnailImageName;
    private String profileImageName;

}
