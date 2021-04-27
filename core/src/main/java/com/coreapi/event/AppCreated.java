package com.coreapi.event;

import java.time.LocalDate;
import java.util.List;

import org.axonframework.serialization.Revision;

import lombok.Value;
import com.coreapi.valueobject.AppId;
import com.coreapi.valueobject.AppLifeCycle;
import com.coreapi.valueobject.AppName;
import com.coreapi.valueobject.AppType;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionId;
import com.coreapi.valueobject.AppVersionLifeCycle;
import com.coreapi.valueobject.AppVersionLongDescription;
import com.coreapi.valueobject.AppVersionNumber;
import com.coreapi.valueobject.AppVersionPrice;
import com.coreapi.valueobject.AppVersionShortDescription;
import com.coreapi.valueobject.AppVersionVisibility;
import com.coreapi.valueobject.DeveloperId;

/**
 * An Axon event which will be published over event bus when a app/library is
 * created.
 *
 */
@Value
@Revision("1.0")
public final class AppCreated {

    private AppId id;
    private AppName name;
    private AppType type;
    private AppVersionId versionId;
    private AppVersionPrice price;
    private AppVersionShortDescription shortDescription;
    private AppVersionLongDescription longDescription;
    private List<AppVersionGalleryImage> galleryImages;
    private AppVersionNumber versionNumber;
    private AppVersionLifeCycle versionLifeCycle;
    private AppVersionVisibility visibility;
    private DeveloperId developerId;
    private AppLifeCycle lifeCycle;
    private LocalDate createdOn;
}
