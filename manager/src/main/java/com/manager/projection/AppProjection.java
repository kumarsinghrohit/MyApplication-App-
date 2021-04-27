package com.manager.projection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.elasticsearch.ElasticsearchStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.coreapi.event.AppCreated;
import com.coreapi.event.AppDeleted;
import com.coreapi.event.AppUpdated;
import com.coreapi.event.AppVersionAdded;
import com.coreapi.event.AppVersionDeleted;
import com.coreapi.event.AppVersionGalleryImageUpdated;
import com.coreapi.event.AppVersionLongDescriptionUpdated;
import com.coreapi.event.AppVersionNumberUpdated;
import com.coreapi.event.AppVersionPriceUpdated;
import com.coreapi.event.AppVersionShortDescriptionUpdated;
import com.coreapi.event.AppVersionUpdateInformationUpdated;
import com.coreapi.valueobject.AppVersion;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionPrice;

@Component
class AppProjection {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppProjection.class);

    private final AppDaoService appDaoService;

    AppProjection(AppDaoService appDaoService) {
        this.appDaoService = appDaoService;
    }

    @EventHandler
    void on(AppCreated appCreated) throws IOException {
        LOGGER.info("App Created Event Occured....");
        appDaoService.createApp(new AppEntity(
                appCreated.getId().getValue(),
                appCreated.getName().getValue(),
                appCreated.getType().getValue(),
                Arrays.asList(
                        convertToAppVersionEntity(appCreated)),
                appCreated.getDeveloperId().getValue(),
                appCreated.getLifeCycle().getValue(),
                appCreated.getCreatedOn()));
    }

    @EventHandler
    void on(AppUpdated appUpdated) throws IOException {
        LOGGER.info("App Updated Event Occured....");
        appDaoService.updateApp(appUpdated.getId().getValue(), appUpdated.getName().getValue());
    }

    @EventHandler
    void on(AppVersionAdded appVersionAdded) throws IOException {
        LOGGER.info("App version added Event Occured for app id: {}", appVersionAdded.getId().getValue());
        appDaoService.addVersion(appVersionAdded.getId().getValue(),
                convertToAppVersionEntity(appVersionAdded));
    }

    @EventHandler
    void on(AppVersionShortDescriptionUpdated appVersionShortDescriptionUpdated) throws IOException {
        LOGGER.info("AppVersionShortDescriptionUpdated Event Occured for app id: {} and version id : {}",
                appVersionShortDescriptionUpdated.getId().getValue(),
                appVersionShortDescriptionUpdated.getVersionId().getValue());
        appDaoService.updateVersionShortDescription(appVersionShortDescriptionUpdated.getId().getValue(),
                appVersionShortDescriptionUpdated.getVersionId().getValue(),
                appVersionShortDescriptionUpdated.getShortDescription().getValue());
    }

    @EventHandler
    void on(AppVersionLongDescriptionUpdated appVersionLongDescriptionUpdated) throws IOException {
        LOGGER.info("AppVersionLongDescriptionUpdated Event Occured for app id: {} and version id : {}",
                appVersionLongDescriptionUpdated.getId().getValue(),
                appVersionLongDescriptionUpdated.getVersionId().getValue());
        appDaoService.updateVersionLongDescription(appVersionLongDescriptionUpdated.getId().getValue(),
                appVersionLongDescriptionUpdated.getVersionId().getValue(),
                appVersionLongDescriptionUpdated.getLongDescription().getValue());
    }

    @EventHandler
    void on(AppVersionPriceUpdated appVersionPriceUpdated) throws IOException {
        LOGGER.info("AppVersionPriceUpdated Event Occured for app id: {} and version id : {}",
                appVersionPriceUpdated.getId().getValue(),
                appVersionPriceUpdated.getVersionId().getValue());
        appDaoService.updateVersionPrice(appVersionPriceUpdated.getId().getValue(),
                appVersionPriceUpdated.getVersionId().getValue(),
                appVersionPriceUpdated.getPrice().getValue());
    }

    @EventHandler
    void on(AppVersionUpdateInformationUpdated appVersionUpdateInformationUpdated) throws IOException {
        LOGGER.info("AppVersionUpdateInformationUpdated Event Occured for app id: {} and version id : {}",
                appVersionUpdateInformationUpdated.getId().getValue(),
                appVersionUpdateInformationUpdated.getVersionId().getValue());
        appDaoService.updateVersionUpdateInformation(appVersionUpdateInformationUpdated.getId().getValue(),
                appVersionUpdateInformationUpdated.getVersionId().getValue(),
                appVersionUpdateInformationUpdated.getUpdateInformation().getValue());
    }

    @EventHandler
    void on(AppVersionNumberUpdated appVersionNumberUpdated) throws IOException {
        LOGGER.info("AppVersionNumberUpdated Event Occured for app id: {} and version id : {}",
                appVersionNumberUpdated.getId().getValue(),
                appVersionNumberUpdated.getVersionId().getValue());
        appDaoService.updateVersionNumber(appVersionNumberUpdated.getId().getValue(),
                appVersionNumberUpdated.getVersionId().getValue(),
                appVersionNumberUpdated.getVersionNumber().getValue());
    }

    @EventHandler
    void on(AppVersionGalleryImageUpdated appVersionGalleryImageUpdated) throws IOException {
        LOGGER.info("AppVersionGalleryImageUpdated Event Occured for app id: {} and version id : {}",
                appVersionGalleryImageUpdated.getId().getValue(),
                appVersionGalleryImageUpdated.getVersionId().getValue());
        appDaoService.updateVersionGalleryImages(appVersionGalleryImageUpdated.getId().getValue(),
                appVersionGalleryImageUpdated.getVersionId().getValue(),
                mapToAppVersionGalleryImageEntity(appVersionGalleryImageUpdated.getGalleryImages()));
    }

    @EventHandler
    void on(AppVersionDeleted appVersionDeleted) throws IOException {
        LOGGER.info("AppVersionDeleted Event Occured for app id: {} and version id : {}",
                appVersionDeleted.getId().getValue(),
                appVersionDeleted.getVersionId().getValue());
        appDaoService.deleteAppVersion(appVersionDeleted.getId().getValue(),
                appVersionDeleted.getVersionId().getValue(),
                appVersionDeleted.getVersionLifeCycle().getValue());
    }

    @EventHandler
    void on(AppDeleted appDeleted) throws IOException {
        LOGGER.info("AppDeleted Event Occured for app id: {} ",
                appDeleted.getId().getValue());
        appDaoService.deleteApp(appDeleted.getId().getValue(),
                appDeleted.getLifeCycle().getValue());
    }

    /**
     * The query handler that returns existing version numbers for an {@link App}.
     *
     * @param getAppVersionsNumberByIdQuery {@link GetAppVersionsNumberByIdQuery}
     * @return a {@link Map<String, String>} representing all version numbers with
     *         it version Id for the given app id.
     * @throws IOException
     */
    @QueryHandler
    AppVersionNumberMap findversionNumbersById(GetAppVersionsNumberByIdQuery getAppVersionsNumberByIdQuery) throws IOException {
        return appDaoService.findversionNumbersById(getAppVersionsNumberByIdQuery.getAppId());
    }

    /**
     * The query handler that returns the {@link App} by a specific ID.
     *
     * @param findByIdQuery {@link FindAppByIdQuery}
     * @return a {@link App} representing the app requested.
     * @throws IOException
     */
    @QueryHandler
    App findAppById(FindAppByIdQuery findByIdQuery) throws IOException {
        AppEntity appEntity = appDaoService.findById(findByIdQuery.getAppId());
        return convertToApp(appEntity);
    }

    /**
     * The query handler that returns all the {@link App}s by type currently present
     * in the database.
     *
     * @param getAllAppsByTypeQuery {@link FindAppByNameQuery}
     * @return a list of {@link App} representing all the products in the catalogue.
     * @throws IOException
     */
    @QueryHandler
    List<App> findAppByName(FindAppByNameQuery getAllAppsByTypeQuery) throws IOException {
        List<App> apps = new ArrayList<>();
        try {
            for (AppEntity appEntity : appDaoService.findAppByName(getAllAppsByTypeQuery.getName())) {
                apps.add(convertToApp(appEntity));
            }

        } catch (ElasticsearchStatusException elasticsearchStatusException) {
            apps = Collections.emptyList();
        }
        return apps;
    }

    private App convertToApp(AppEntity appEntity) {
        List<AppVersion> appVersions = appEntity.getAppVersions().stream().map(this::convertToAppVersion)
                .collect(Collectors.toList());
        return new App(appEntity.getId(), appEntity.getName(), appEntity.getType(), appVersions, appEntity.getDeveloperId(),
                appEntity.getLifeCycle(), appEntity.getCreatedOn());
    }

    private AppVersion convertToAppVersion(AppVersionEntity appVersionEntity) {
        List<AppVersionGalleryImage> galleryImages = appVersionEntity.getGalleryImages().stream()
                .map(this::convertToAppVersionGalleryImage).collect(Collectors.toList());
        return new AppVersion(appVersionEntity.getVersionId(),
                new AppVersionPrice(appVersionEntity.getPrice().getCurrency(), appVersionEntity.getPrice().getValue()),
                appVersionEntity.getShortDescription(), appVersionEntity.getLongDescription(),
                galleryImages, appVersionEntity.getUpdateInformation(), appVersionEntity.getVersionNumber(),
                appVersionEntity.getVersionLifeCycle(), appVersionEntity.getVisibility(), appVersionEntity.getCreatedOn());
    }

    private AppVersionGalleryImage convertToAppVersionGalleryImage(
            AppVersionGalleryImageEntity appVersionGalleryImageEntity) {
        return new AppVersionGalleryImage(appVersionGalleryImageEntity.getThumbnailImageName(),
                appVersionGalleryImageEntity.getProfileImageName());

    }

    private AppVersionEntity convertToAppVersionEntity(AppCreated appCreated) {
        return new AppVersionEntity(appCreated.getVersionId().getValue(),
                new AppPriceEntity(appCreated.getPrice().getCurrency(), appCreated.getPrice().getValue()),
                appCreated.getShortDescription().getValue(),
                appCreated.getLongDescription().getValue(),
                mapToAppVersionGalleryImageEntity(appCreated.getGalleryImages()),
                null,
                appCreated.getVersionNumber().getValue(),
                appCreated.getVersionLifeCycle().getValue(),
                appCreated.getVisibility().getValue(),
                appCreated.getCreatedOn());
    }

    private AppVersionEntity convertToAppVersionEntity(AppVersionAdded appVersionAdded) {
        return new AppVersionEntity(appVersionAdded.getVersionId().getValue(),
                new AppPriceEntity(appVersionAdded.getPrice().getCurrency(), appVersionAdded.getPrice().getValue()),
                appVersionAdded.getShortDescription().getValue(),
                appVersionAdded.getLongDescription().getValue(),
                mapToAppVersionGalleryImageEntity(appVersionAdded.getGalleryImages()),
                appVersionAdded.getUpdateInformation().getValue(),
                appVersionAdded.getVersionNumber().getValue(),
                appVersionAdded.getVersionLifeCycle().getValue(),
                appVersionAdded.getVisibility().getValue(),
                appVersionAdded.getCreatedOn());
    }

    private List<AppVersionGalleryImageEntity> mapToAppVersionGalleryImageEntity(List<AppVersionGalleryImage> galleryImages) {
        return galleryImages.stream().map(galleryImage -> new AppVersionGalleryImageEntity(galleryImage.getProfileImageName(),
                galleryImage.getThumbnailImageName())).collect(Collectors.toList());
    }
}
