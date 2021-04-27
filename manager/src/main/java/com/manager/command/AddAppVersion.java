package com.manager.command;

import java.time.LocalDate;
import java.util.List;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.ToString;
import lombok.Value;
import com.coreapi.valueobject.AppId;
import com.coreapi.valueobject.AppVersionLongDescription;
import com.coreapi.valueobject.AppVersionPrice;
import com.coreapi.valueobject.AppVersionShortDescription;
import com.coreapi.valueobject.AppVersionUpdateInfo;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionId;
import com.coreapi.valueobject.AppVersionNumber;
import com.coreapi.valueobject.DeveloperId;

/**
 * An axon command which will be published over a axon command bus when a new
 * app version of app/library is added.
 */
@Value
@ToString(includeFieldNames = true)
public final class AddAppVersion {

    @TargetAggregateIdentifier
    private AppId id;
    private AppVersionId versionId;
    private AppVersionPrice price;
    private AppVersionShortDescription shortDescription;
    private AppVersionLongDescription longDescription;
    private List<AppVersionGalleryImage> galleryImages;
    private AppVersionUpdateInfo updateInformation;
    private AppVersionNumber versionNumber;
    private LocalDate createdOn;
    private DeveloperId developerId;
}
