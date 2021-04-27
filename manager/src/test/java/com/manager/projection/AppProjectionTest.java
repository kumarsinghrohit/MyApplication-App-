package com.manager.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;

import org.axonframework.queryhandling.QueryGateway;
import org.elasticsearch.ElasticsearchStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.Lists;

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
import com.coreapi.valueobject.AppId;
import com.coreapi.valueobject.AppLifeCycle;
import com.coreapi.valueobject.AppName;
import com.coreapi.valueobject.AppType;
import com.coreapi.valueobject.AppVersion;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionId;
import com.coreapi.valueobject.AppVersionLifeCycle;
import com.coreapi.valueobject.AppVersionLongDescription;
import com.coreapi.valueobject.AppVersionNumber;
import com.coreapi.valueobject.AppVersionPrice;
import com.coreapi.valueobject.AppVersionShortDescription;
import com.coreapi.valueobject.AppVersionUpdateInfo;
import com.coreapi.valueobject.AppVersionVisibility;
import com.coreapi.valueobject.DeveloperId;
import com.manager.aws.AwsStorageService;

@ExtendWith(SpringExtension.class)
@Tag("Component")
@DisplayName("Testing  AppProjection, various queries and events handling")
public class AppProjectionTest {
    @Mock
    private AppDaoService appDaoService;

    @Mock
    private QueryGateway queryGateway;

    @Mock
    private AwsStorageService awsStorageService;

    private @InjectMocks AppProjection instanceUnderTest;

    @BeforeEach
    public void tearDown() {
        verifyNoMoreInteractions(appDaoService, queryGateway, awsStorageService);
    }

    @Test
    @DisplayName("Testing event handler when an app is created.")
    public void whenAppCreated_thenRunHandler() throws IOException {
        Currency currency = Currency.getInstance(Locale.UK);
        AppId appId = new AppId("id");
        AppName appName = new AppName("appName");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription");
        AppVersionPrice appPrice = new AppVersionPrice(currency, 15.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersionNumber appVersionNumber = new AppVersionNumber("1.0");
        DeveloperId developerId = new DeveloperId("developerId");
        AppCreated appCreated = new AppCreated(appId, appName, AppType.APP,
                new AppVersionId("versionId"),
                appPrice, appShortDescription, appLongDescription,
                Arrays.asList(appVersionGalleryImage), appVersionNumber, AppVersionLifeCycle.ACTIVE,
                AppVersionVisibility.PUBLIC, developerId, AppLifeCycle.ACTIVE, LocalDate.now());
        instanceUnderTest.on(appCreated);

        verify(appDaoService).createApp(any(AppEntity.class));
    }

    @Test
    @DisplayName("Testing the event handler when a app name is updated")
    public void when_AppUpdated_Then_RunHandler() throws Exception {
        AppId appId = new AppId("appId");
        AppName appName = new AppName("appName");
        AppUpdated appUpdated = new AppUpdated(appId, appName);
        instanceUnderTest.on(appUpdated);
        verify(appDaoService, times(1)).updateApp(appId.getValue(), appName.getValue());
    }

    @Test
    @DisplayName("Testing the event handler when a app lifecycle is discontinued.")
    public void when_AppDeleted_Then_RunHandler() throws Exception {
        Currency currency = Currency.getInstance(Locale.UK);
        AppId appId = new AppId("appId");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription");
        AppVersionPrice appPrice = new AppVersionPrice(currency, 15.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersionUpdateInfo updateInfo = new AppVersionUpdateInfo("app update info has minimum length");
        AppVersionNumber appVersionNumber = new AppVersionNumber("1.0");
        AppVersion version = new AppVersion("versionId", appPrice, appShortDescription.getValue(), appLongDescription.getValue(),
                java.util.Arrays.asList(appVersionGalleryImage), updateInfo.getValue(), appVersionNumber.getValue(),
                AppVersionLifeCycle.ACTIVE.getValue(),
                AppVersionVisibility.PUBLIC.getValue(), LocalDate.now());
        AppDeleted appDeleted = new AppDeleted(appId, AppType.APP, AppLifeCycle.ACTIVE, Arrays.asList(version));
        instanceUnderTest.on(appDeleted);
        verify(appDaoService, times(1)).deleteApp(appId.getValue(), appDeleted.getLifeCycle().getValue());
    }

    @Test
    @DisplayName("Testing the event handler when a app version lifecycle is discontinued.")
    public void when_AppVersionDeleted_Then_RunHandler() throws Exception {
        AppId appId = new AppId("appId");
        AppVersionId appVersionId = new AppVersionId("versionId");
        AppVersionDeleted appVersionDeleted = new AppVersionDeleted(appId, appVersionId,
                AppVersionLifeCycle.ACTIVE, 1, 1);
        instanceUnderTest.on(appVersionDeleted);
        verify(appDaoService, times(1)).deleteAppVersion(appId.getValue(), appVersionId.getValue(),
                appVersionDeleted.getVersionLifeCycle().getValue());
    }

    @Test
    @DisplayName("Testing the event handler when a version to added to an app.")
    public void when_AppVersionAdded_Then_Handle_Event_Succesfully() throws IOException {
        AppId appId = new AppId("id");
        AppVersionId versionId = new AppVersionId("versionId");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription");
        AppVersionPrice appPrice = new AppVersionPrice(Currency.getInstance(Locale.UK), 15.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersionUpdateInfo updateInfo = new AppVersionUpdateInfo("app Update Info Test Data");
        AppVersionNumber appVersionNumber = new AppVersionNumber("1.0");
        LocalDate createdOn = LocalDate.now();
        AppVersionAdded appVersionAdded = new AppVersionAdded(appId, versionId, appPrice, appShortDescription,
                appLongDescription,
                Arrays.asList(appVersionGalleryImage), updateInfo, appVersionNumber, AppVersionLifeCycle.ACTIVE,
                AppVersionVisibility.PUBLIC, createdOn);
        instanceUnderTest.on(appVersionAdded);
        verify(appDaoService).addVersion(eq("id"), any(AppVersionEntity.class));
    }

    @Test
    @DisplayName("Testing the event handler when short description of an app's version is updated.")
    public void when_AppVersionShortDescriptionUpdated_Then_Handle_Event_Succesfully() throws IOException {
        AppId appId = new AppId("id");
        AppVersionId versionId = new AppVersionId("versionId");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("ShortDescription");
        AppVersionShortDescriptionUpdated appVersionShortDescriptionUpdated = new AppVersionShortDescriptionUpdated(
                appId, versionId, appShortDescription);
        instanceUnderTest.on(appVersionShortDescriptionUpdated);
        verify(appDaoService).updateVersionShortDescription("id", "versionId", "ShortDescription");
    }

    @Test
    @DisplayName("Testing the event handler when Long description of an app's version is updated.")
    public void when_AppVersionLongDescriptionUpdated_Then_Handle_Event_Succesfully() throws IOException {
        AppId appId = new AppId("id");
        AppVersionId versionId = new AppVersionId("versionId");
        AppVersionLongDescription longDescription = new AppVersionLongDescription("LongDescription1");
        AppVersionLongDescriptionUpdated appVersionLongDescriptionUpdated = new AppVersionLongDescriptionUpdated(
                appId, versionId, longDescription);
        instanceUnderTest.on(appVersionLongDescriptionUpdated);
        verify(appDaoService).updateVersionLongDescription("id", "versionId", "LongDescription1");
    }

    @Test
    @DisplayName("Testing the event handler when price of an app's version is updated.")
    public void when_AppVersionPriceUpdated_Then_Handle_Event_Succesfully() throws IOException {
        AppId appId = new AppId("id");
        AppVersionId versionId = new AppVersionId("versionId");
        AppVersionPrice price = new AppVersionPrice(Currency.getInstance("EUR"), 1.0);
        AppVersionPriceUpdated appVersionPriceUpdated = new AppVersionPriceUpdated(
                appId, versionId, price);
        instanceUnderTest.on(appVersionPriceUpdated);
        verify(appDaoService).updateVersionPrice("id", "versionId", 1.0);
    }

    @Test
    @DisplayName("Testing the event handler when updateInfo of an app's version is updated.")
    public void when_AppVersionUpdateInformationUpdated_Then_Handle_Event_Succesfully() throws IOException {
        AppId appId = new AppId("id");
        AppVersionId versionId = new AppVersionId("versionId");
        AppVersionUpdateInfo appUpdateInfo = new AppVersionUpdateInfo("app Update Info Test Data");
        AppVersionUpdateInformationUpdated appVersionUpdateInformationUpdated = new AppVersionUpdateInformationUpdated(
                appId, versionId, appUpdateInfo);
        instanceUnderTest.on(appVersionUpdateInformationUpdated);
        verify(appDaoService).updateVersionUpdateInformation("id", "versionId", "app Update Info Test Data");
    }

    @Test
    @DisplayName("Testing the event handler when version number of an app's version is updated.")
    public void when_AppVersionNumberUpdated_Then_Handle_Event_Succesfully() throws IOException {
        AppId appId = new AppId("id");
        AppVersionId versionId = new AppVersionId("versionId");
        AppVersionNumber appVersionNumber = new AppVersionNumber("1.0");
        AppVersionNumberUpdated appVersionNumberUpdated = new AppVersionNumberUpdated(
                appId, versionId, appVersionNumber);
        instanceUnderTest.on(appVersionNumberUpdated);
        verify(appDaoService).updateVersionNumber("id", "versionId", "1.0");
    }

    @Test
    @DisplayName("Testing the event handler when version number of an app's version is updated.")
    public void when_AppVersionGalleryImageUpdated_Then_Handle_Event_Succesfully() throws IOException {
        AppId appId = new AppId("id");
        AppVersionId versionId = new AppVersionId("versionId");
        AppVersionGalleryImage galleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersionGalleryImageUpdated appVersionGalleryImageUpdated = new AppVersionGalleryImageUpdated(
                appId, versionId, Arrays.asList(galleryImage));
        instanceUnderTest.on(appVersionGalleryImageUpdated);
        verify(appDaoService).updateVersionGalleryImages(eq("id"), eq("versionId"), any());
    }

    @Test
    @DisplayName("Testing the query handler when all versions of an app name is rquested.")
    public void when_findversionNumbersById_Then_aapVersionsNumber() throws Exception {
        AppVersionNumberMap appVersionNumberMap = new AppVersionNumberMap(Collections.singletonMap("1.0", "versionId"));
        GetAppVersionsNumberByIdQuery getAppVersionsNumberByIdQuery = new GetAppVersionsNumberByIdQuery("appId");
        when(appDaoService.findversionNumbersById("appId")).thenReturn(appVersionNumberMap);
        assertThat(instanceUnderTest.findversionNumbersById(getAppVersionsNumberByIdQuery).getAppVersionsNumberMap().keySet()
                .iterator().next()).isEqualTo("1.0");
        verify(appDaoService).findversionNumbersById("appId");
    }

    @Test
    @DisplayName("Testing find app by given app id")
    public void when_findAppById_then_Return_app() throws IOException {
        LocalDate createdOn = LocalDate.now();
        AppPriceEntity appPriceEntity = new AppPriceEntity(Currency.getInstance("EUR"), 11);
        AppVersionGalleryImageEntity galleryImage = new AppVersionGalleryImageEntity("thumbnailImageName", "profileImageName");
        AppVersionEntity appVersionEntity = new AppVersionEntity("versionId", appPriceEntity, "shortDescription",
                "longDescription", Lists.newArrayList(galleryImage), "updateInformation", "versionNumber", "lifeCycle",
                "visibility", createdOn);
        AppEntity appEntity = new AppEntity("id", "name", "type", Lists.newArrayList(appVersionEntity), "developerId",
                AppLifeCycle.ACTIVE.getValue(), createdOn);
        when(appDaoService.findById("appId")).thenReturn(appEntity);
        instanceUnderTest.findAppById(new FindAppByIdQuery("appId"));

        verify(appDaoService).findById("appId");
    }

    @Test
    @DisplayName("Testing find app by given app name")
    public void when_findAppByName_then_Return_app() throws IOException {
        LocalDate createdOn = LocalDate.now();
        AppPriceEntity appPriceEntity = new AppPriceEntity(Currency.getInstance("EUR"), 11);
        AppVersionGalleryImageEntity galleryImage = new AppVersionGalleryImageEntity("thumbnailImageName", "profileImageName");
        AppVersionEntity appVersionEntity = new AppVersionEntity("versionId", appPriceEntity, "shortDescription",
                "longDescription", Lists.newArrayList(galleryImage), "updateInformation", "versionNumber", "lifeCycle",
                "visibility", createdOn);
        AppEntity appEntity = new AppEntity("id", "appName", "type", Lists.newArrayList(appVersionEntity), "developerId",
                AppLifeCycle.ACTIVE.getValue(), createdOn);
        when(appDaoService.findAppByName("appName")).thenReturn(Arrays.asList(appEntity));
        assertThat(instanceUnderTest.findAppByName(new FindAppByNameQuery("appName"))).isNotEmpty();

        verify(appDaoService).findAppByName("appName");
    }

    @Test
    @DisplayName("Testing find app by given app name when index not exists")
    public void when_findAppByName__Index_Not_Exists_then_ElasticsearchStatusException() throws IOException {
        when(appDaoService.findAppByName("appName")).thenThrow(ElasticsearchStatusException.class);
        assertThat(instanceUnderTest.findAppByName(new FindAppByNameQuery("appName"))).isEmpty();

        verify(appDaoService).findAppByName("appName");
    }
}