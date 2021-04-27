package com.manager.command;

import java.time.LocalDate;
import java.util.List;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.ToString;
import lombok.Value;
import com.coreapi.valueobject.AppId;
import com.coreapi.valueobject.AppName;
import com.coreapi.valueobject.AppType;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionId;
import com.coreapi.valueobject.AppVersionLongDescription;
import com.coreapi.valueobject.AppVersionNumber;
import com.coreapi.valueobject.AppVersionPrice;
import com.coreapi.valueobject.AppVersionShortDescription;
import com.coreapi.valueobject.DeveloperId;

/**
 * An axon command which will be published over a axon command bus when a new
 * app/lib is created.
 */
@Value
@ToString(includeFieldNames = true)
public final class CreateApp {

    @TargetAggregateIdentifier
    private AppId id;
    private AppName name;
    private AppType type;
    private AppVersionId versionId;
    private AppVersionPrice price;
    private AppVersionShortDescription shortDescription;
    private AppVersionLongDescription longDescription;
    private List<AppVersionGalleryImage> galleryImages;
    private AppVersionNumber versionNumber;
    private DeveloperId developerId;
    private LocalDate createdOn;
}
