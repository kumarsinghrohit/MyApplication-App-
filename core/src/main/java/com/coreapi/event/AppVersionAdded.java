package com.coreapi.event;

import java.time.LocalDate;
import java.util.List;

import org.axonframework.serialization.Revision;

import lombok.ToString;
import lombok.Value;
import com.coreapi.valueobject.AppId;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionId;
import com.coreapi.valueobject.AppVersionLifeCycle;
import com.coreapi.valueobject.AppVersionLongDescription;
import com.coreapi.valueobject.AppVersionNumber;
import com.coreapi.valueobject.AppVersionPrice;
import com.coreapi.valueobject.AppVersionShortDescription;
import com.coreapi.valueobject.AppVersionUpdateInfo;
import com.coreapi.valueobject.AppVersionVisibility;

/**
 * An Axon event which will be published over event bus when an new version of
 * app/library is added.
 *
 */
@Value
@ToString(includeFieldNames = true)
@Revision("1.0")
public final class AppVersionAdded {

    private AppId id;
    private AppVersionId versionId;
    private AppVersionPrice price;
    private AppVersionShortDescription shortDescription;
    private AppVersionLongDescription longDescription;
    private List<AppVersionGalleryImage> galleryImages;
    private AppVersionUpdateInfo updateInformation;
    private AppVersionNumber versionNumber;
    private AppVersionLifeCycle versionLifeCycle;
    private AppVersionVisibility visibility;
    private LocalDate createdOn;
}
