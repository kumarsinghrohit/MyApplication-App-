package com.manager.command;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.coreapi.event.AppVersionDeleted;
import com.coreapi.event.AppVersionGalleryImageUpdated;
import com.coreapi.event.AppVersionLongDescriptionUpdated;
import com.coreapi.event.AppVersionNumberUpdated;
import com.coreapi.event.AppVersionPriceUpdated;
import com.coreapi.event.AppVersionShortDescriptionUpdated;
import com.coreapi.event.AppVersionUpdateInformationUpdated;
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
import com.coreapi.event.AppVersionDeleted;

@Data
@AllArgsConstructor
class AppVersion {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppVersion.class);

    @EntityId
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

    @CommandHandler
    private void handle(DeleteAppVersion deleteAppVersionCommand) {
        apply(new AppVersionDeleted(deleteAppVersionCommand.getId(), versionId,
                deleteAppVersionCommand.getVersionLifeCycle(), deleteAppVersionCommand.getCurrentAppVersionIndex(),
                deleteAppVersionCommand.getTotalNumberOfAppVersions()));
    }

    @EventSourcingHandler
    private void on(AppVersionDeleted appVersionDeleted) {
        this.versionLifeCycle = appVersionDeleted.getVersionLifeCycle();
    }

    @CommandHandler
    private void handle(UpdateAppVersion updateAppVersionCommand) {
        AppId appId = updateAppVersionCommand.getId();
        AppVersionId versionId = updateAppVersionCommand.getVersionId();
        LOGGER.info("update App version command occured for app id: {} and version id: {}",
                appId.getValue(), versionId.getValue());

        if (!Objects.equals(this.shortDescription, updateAppVersionCommand.getShortDescription())) {
            apply(new AppVersionShortDescriptionUpdated(appId, versionId, updateAppVersionCommand.getShortDescription()));
        }
        if (!Objects.equals(this.longDescription, updateAppVersionCommand.getLongDescription())) {
            apply(new AppVersionLongDescriptionUpdated(appId, versionId, updateAppVersionCommand.getLongDescription()));
        }
        if (!Objects.equals(this.price.getValue(), updateAppVersionCommand.getPrice().getValue())) {
            apply(new AppVersionPriceUpdated(appId, versionId, updateAppVersionCommand.getPrice()));
        }

        if (!Objects.equals(this.updateInformation, updateAppVersionCommand.getUpdateInformation())) {
            apply(new AppVersionUpdateInformationUpdated(appId, versionId, updateAppVersionCommand.getUpdateInformation()));
        }
        if (!Objects.equals(this.versionNumber, updateAppVersionCommand.getVersionNumber())) {
            apply(new AppVersionNumberUpdated(appId, versionId, updateAppVersionCommand.getVersionNumber()));
        }

        List<AppVersionGalleryImage> updatedGalleryImages = new LinkedList<>(this.galleryImages);
        Map<Integer, AppVersionGalleryImage> galleryImageUrlUpdateMap = updateAppVersionCommand
                .getAppVersionGalleryImageMap();
        galleryImageUrlUpdateMap.forEach((index, appVersionGalleryImage) -> {
            if (updatedGalleryImages.isEmpty() || index > updatedGalleryImages.size() - 1) {
                updatedGalleryImages.add(index, appVersionGalleryImage);
            } else {
                updatedGalleryImages.set(index, appVersionGalleryImage);
            }
        });

        apply(new AppVersionGalleryImageUpdated(appId, versionId, updatedGalleryImages));
    }

    @EventSourcingHandler
    private void on(AppVersionGalleryImageUpdated appVersionGalleryImageUpdated) {
        this.galleryImages = appVersionGalleryImageUpdated.getGalleryImages();
    }

    @EventSourcingHandler
    private void on(AppVersionNumberUpdated appVersionNumberUpdated) {
        this.versionNumber = appVersionNumberUpdated.getVersionNumber();
    }

    @EventSourcingHandler
    private void on(AppVersionUpdateInformationUpdated appVersionUpdateInformationUpdated) {
        this.updateInformation = appVersionUpdateInformationUpdated.getUpdateInformation();
    }

    @EventSourcingHandler
    private void on(AppVersionPriceUpdated appVersionPriceUpdated) {
        this.price = appVersionPriceUpdated.getPrice();
    }

    @EventSourcingHandler
    private void on(AppVersionShortDescriptionUpdated appVersionShortDescriptionUpdated) {
        this.shortDescription = appVersionShortDescriptionUpdated.getShortDescription();
    }

    @EventSourcingHandler
    private void on(AppVersionLongDescriptionUpdated appVersionLongDescriptionUpdated) {
        this.longDescription = appVersionLongDescriptionUpdated.getLongDescription();
    }
}
