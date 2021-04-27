package com.profile.projection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.axonframework.queryhandling.QueryHandler;
import org.elasticsearch.ElasticsearchStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.coreapi.valueobject.AppVersionPrice;
import com.coreapi.valueobject.AppVersion;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.profile.query.App;
import com.profile.query.FindAppByIdQuery;
import com.profile.query.FindAppsByDeveloperIdAndTypeQuery;

/**
 * The projection class responsible for handling the query-side of {@link App}.
 * Contains event handlers that update the read model and query handler that
 * provide access to the stored {@code App}s
 */
@Component
public class AppProjection {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppProjection.class);

    private final AppDaoService appDaoService;

    AppProjection(AppDaoService appDaoService) {
        this.appDaoService = appDaoService;
    }

    /**
     * The query handler responsible to fetch a list of {@link App}s that was
     * developed by the currently logged in user.
     *
     * @param findAppsByDeveloperIdAndTypeQuery {@link FindAppsByDeveloperIdAndTypeQuery}
     * @return a list of {@code App} representing all the apps created by the
     *         currently logged in user.
     * @throws IOException
     */
    @QueryHandler
    List<App> findByDeveloperIdAndType(FindAppsByDeveloperIdAndTypeQuery findAppsByDeveloperIdAndTypeQuery) throws IOException {
        LOGGER.info("fetching all apps...");
        List<App> apps = new ArrayList<>();
        try {
            for (AppEntity appEntity : appDaoService.findByDeveloperIdAndType(findAppsByDeveloperIdAndTypeQuery.getDeveloperId(),
                    findAppsByDeveloperIdAndTypeQuery.getType(), findAppsByDeveloperIdAndTypeQuery.getLifeCyle())) {
                apps.add(convertToApp(appEntity));
            }

        } catch (ElasticsearchStatusException elasticsearchStatusException) {
            apps = Collections.emptyList();
        }
        return apps;
    }

    /**
     * The query handler that returns the {@link App} by a specific ID.
     *
     * @param getAppByIdQuery {@link FindAppByIdQuery}
     * @return a {@link App} representing the app requested.
     * @throws IOException
     */
    @QueryHandler
    App findById(FindAppByIdQuery getAppByIdQuery) throws IOException {
        AppEntity appEntity = appDaoService.findById(getAppByIdQuery.getAppId());
        return convertToApp(appEntity);
    }

    private App convertToApp(AppEntity appEntity) {
        List<AppVersion> appVersions = appEntity.getAppVersions().stream().map(this::convertToAppVersion)
                .collect(Collectors.toList());
        return new App(appEntity.getId(), appEntity.getName(), appEntity.getType(), appVersions, appEntity.getCreatedOn());
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
}
