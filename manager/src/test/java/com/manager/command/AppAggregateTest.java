package com.manager.command;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.coreapi.event.AppCreated;
import com.coreapi.event.AppDeleted;
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

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Tag("Aggregate")
@DisplayName("Testing")
public class AppAggregateTest {
    private FixtureConfiguration<AppAggregate> fixture;
    private Currency currency = Currency.getInstance(Locale.UK);

    @BeforeEach
    public void setUp() {
        fixture = new AggregateTestFixture<>(AppAggregate.class);
    }

    @Test
    @DisplayName("Testing CreateCommand's Successful Execution")
    public void when_CreateAppCommand_Sent_Then_Expect_AppCreated() {
        AppId appId = new AppId("id");
        AppVersionId appVersionId = new AppVersionId("versionId");
        AppName appName = new AppName("appName");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription");
        AppVersionPrice appPrice = new AppVersionPrice(currency, 15.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersionNumber appVersionNumber = new AppVersionNumber("1.0");
        LocalDate createdOn = LocalDate.now();
        DeveloperId developerId = new DeveloperId("developerId");
        fixture.givenNoPriorActivity()
                .when(new CreateAppCommand(appId, appName, AppType.APP, appVersionId, appPrice, appShortDescription,
                        appLongDescription,
                        Arrays.asList(appVersionGalleryImage), appVersionNumber, developerId, createdOn))
                .expectEvents(new AppCreated(appId, appName, AppType.APP, appVersionId, appPrice, appShortDescription,
                        appLongDescription,
                        Arrays.asList(appVersionGalleryImage), appVersionNumber, AppVersionLifeCycle.ACTIVE,
                        AppVersionVisibility.PUBLIC, developerId,
                        AppLifeCycle.ACTIVE, createdOn));

    }

    @Test
    @DisplayName("Testing UpdateAppCommand's Successful Execution")
    public void when_UpdateAppCommand_Sent_Then_Expect_AppUpdated() {
        AppId appId = new AppId("id");
        AppName appName = new AppName("appName");
        AppName appNameUpdated = new AppName("appNameUpdated");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription");
        AppVersionPrice appPrice = new AppVersionPrice(currency, 15.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersionNumber appVersionNumber = new AppVersionNumber("1.0");
        DeveloperId developerId = new DeveloperId("developerId");
        LocalDate localDate = LocalDate.now();
        fixture.given(new AppCreated(appId, appNameUpdated, AppType.APP,
                new AppVersionId("versionId"),
                appPrice, appShortDescription, appLongDescription,
                Arrays.asList(appVersionGalleryImage), appVersionNumber, AppVersionLifeCycle.ACTIVE,
                AppVersionVisibility.PUBLIC, developerId, AppLifeCycle.ACTIVE, localDate))
                .when(new UpdateAppCommand(appId, appName))
                .expectSuccessfulHandlerExecution();
    }

    @Test
    @DisplayName("Testing AddAppVersionCommand's succesful execution and AppVersionAdded's occurence.")
    public void when_AddAppVersionCommand_Then_Expect_AppVersionAdded() {
        AppId appId = new AppId("id");
        AppName appName = new AppName("appName");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription");
        AppVersionPrice appPrice = new AppVersionPrice(currency, 15.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersionUpdateInfo updateInfo = new AppVersionUpdateInfo("app Update Info Test Data");
        AppVersionNumber appVersionNumber = new AppVersionNumber("1.0");
        LocalDate createdOn = LocalDate.now();
        DeveloperId developerId = new DeveloperId("developerId");
        AppVersionId versionId = new AppVersionId("versionId");
        fixture.given(new AppCreated(appId, appName, AppType.APP, new AppVersionId("versionId1"), appPrice,
                appShortDescription, appLongDescription,
                Arrays.asList(appVersionGalleryImage), appVersionNumber, AppVersionLifeCycle.ACTIVE,
                AppVersionVisibility.PUBLIC, developerId, AppLifeCycle.ACTIVE, LocalDate.now()))
                .when(new AddAppVersionCommand(appId, versionId, appPrice, appShortDescription, appLongDescription,
                        Arrays.asList(appVersionGalleryImage), updateInfo, appVersionNumber, createdOn, developerId))
                .expectSuccessfulHandlerExecution().expectEvents(new AppVersionAdded(appId, versionId,
                        appPrice, appShortDescription, appLongDescription,
                        Arrays.asList(appVersionGalleryImage), updateInfo, appVersionNumber, AppVersionLifeCycle.ACTIVE,
                        AppVersionVisibility.PUBLIC, createdOn));
    }

    @Test
    @DisplayName("Testing UpdateAppVersionCommand's succesful execution when all fields modified.")
    public void when_UpdateAppVersionCommand_Then_Expect_successfulExecution() {
        AppId appId = new AppId("id");
        AppName appName = new AppName("appName");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription1");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription1");
        AppVersionPrice appPrice = new AppVersionPrice(currency, 151.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName1", "profileImageName1");
        AppVersionUpdateInfo updateInfo = new AppVersionUpdateInfo("app Update Info Test Data1");
        AppVersionNumber appVersionNumber = new AppVersionNumber("11.0");
        LocalDate createdOn = LocalDate.now();
        DeveloperId developerId = new DeveloperId("developerId");
        AppVersionId versionId = new AppVersionId("versionId");
        AppVersionId addedVersionId = new AppVersionId("addedVersionId");
        Map<Integer, AppVersionGalleryImage> galleryImageUrlMap = new HashMap<>();
        galleryImageUrlMap.put(0, appVersionGalleryImage);
        fixture.given(
                new AppCreated(appId, appName, AppType.APP, versionId, appPrice, appShortDescription, appLongDescription,
                        Arrays.asList(appVersionGalleryImage), appVersionNumber, AppVersionLifeCycle.ACTIVE,
                        AppVersionVisibility.PUBLIC, developerId, AppLifeCycle.ACTIVE, createdOn),
                new AppVersionAdded(appId, addedVersionId, appPrice, appShortDescription, appLongDescription,
                        Arrays.asList(new AppVersionGalleryImage("thumbnailImageName", "profileImageName")), updateInfo,
                        appVersionNumber, AppVersionLifeCycle.ACTIVE, AppVersionVisibility.PUBLIC, createdOn))
                .when(new UpdateAppVersionCommand(appId, addedVersionId, new AppVersionPrice(currency, 15.0),
                        new AppVersionShortDescription("productShortDescription"),
                        new AppVersionLongDescription("productLongDescription"),
                        Arrays.asList(appVersionGalleryImage), new AppVersionUpdateInfo("app Update Info Test Data"),
                        new AppVersionNumber("1.0"), createdOn, galleryImageUrlMap))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        new AppVersionShortDescriptionUpdated(appId, addedVersionId,
                                new AppVersionShortDescription("productShortDescription")),
                        new AppVersionLongDescriptionUpdated(appId, addedVersionId,
                                new AppVersionLongDescription("productLongDescription")),
                        new AppVersionPriceUpdated(appId, addedVersionId, new AppVersionPrice(currency, 15.0)),
                        new AppVersionUpdateInformationUpdated(appId, addedVersionId,
                                new AppVersionUpdateInfo("app Update Info Test Data")),
                        new AppVersionNumberUpdated(appId, addedVersionId, new AppVersionNumber("1.0")),
                        new AppVersionGalleryImageUpdated(appId, addedVersionId, Arrays.asList(appVersionGalleryImage)));
    }

    @Test
    @DisplayName("Testing UpdateAppVersionCommand's succesful execution when gallery image is added and no other fields modified.")
    public void when_UpdateAppVersionCommand_WithOnlyGalleryImage_Then_Expect_successfulExecution() {
        AppId appId = new AppId("id");
        AppName appName = new AppName("appName");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription1");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription1");
        AppVersionPrice appPrice = new AppVersionPrice(currency, 151.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName1", "profileImageName1");
        AppVersionUpdateInfo updateInfo = new AppVersionUpdateInfo("app Update Info Test Data1");
        AppVersionNumber appVersionNumber = new AppVersionNumber("11.0");
        LocalDate createdOn = LocalDate.now();
        DeveloperId developerId = new DeveloperId("developerId");
        AppVersionId versionId = new AppVersionId("versionId");
        AppVersionId addedVersionId = new AppVersionId("addedVersionId");
        Map<Integer, AppVersionGalleryImage> galleryImageUrlMap = new HashMap<Integer, AppVersionGalleryImage>();
        galleryImageUrlMap.put(0, appVersionGalleryImage);
        fixture.given(
                new AppCreated(appId, appName, AppType.APP, versionId, appPrice, appShortDescription, appLongDescription,
                        Arrays.asList(appVersionGalleryImage), appVersionNumber, AppVersionLifeCycle.ACTIVE,
                        AppVersionVisibility.PUBLIC, developerId, AppLifeCycle.ACTIVE, createdOn),
                new AppVersionAdded(appId, addedVersionId, appPrice, appShortDescription, appLongDescription,
                        Collections.emptyList(), updateInfo, appVersionNumber, AppVersionLifeCycle.ACTIVE,
                        AppVersionVisibility.PUBLIC, createdOn))
                .when(new UpdateAppVersionCommand(appId, addedVersionId, appPrice, appShortDescription, appLongDescription,
                        Arrays.asList(appVersionGalleryImage), updateInfo, appVersionNumber, createdOn, galleryImageUrlMap))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        new AppVersionGalleryImageUpdated(appId, addedVersionId, Arrays.asList(appVersionGalleryImage)));
    }

    @Test
    @DisplayName("Testing DeleteAppCommand's Successful Execution")
    public void when_DeleteAppCommand_Sent_Then_Expect_AppDeleted() {
        AppId appId = new AppId("id");
        AppName appName = new AppName("appName");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription");
        AppVersionPrice appPrice = new AppVersionPrice(currency, 15.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersionNumber appVersionNumber = new AppVersionNumber("1.0");

        DeveloperId developerId = new DeveloperId("developerId");
        LocalDate localDate = LocalDate.now();
        fixture.given(new AppCreated(appId, appName, AppType.APP, new AppVersionId("versionId"), appPrice,
                appShortDescription, appLongDescription,
                Arrays.asList(appVersionGalleryImage), appVersionNumber, AppVersionLifeCycle.ACTIVE,
                AppVersionVisibility.PUBLIC, developerId,
                AppLifeCycle.ACTIVE, localDate))
                .when(new DeleteAppCommand(appId, AppLifeCycle.DISCONTINUED, Collections.emptyList()))
                .expectEvents(new AppDeleted(appId, AppType.APP, AppLifeCycle.DISCONTINUED, Collections.emptyList()));
    }

    @Test
    @DisplayName("Testing DeleteAppVersionCommand's Successful Execution")
    public void when_DeleteAppVersionCommand_Sent_Then_Expect_AppVersionDeleted() {
        AppName appName = new AppName("appName");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription");
        AppVersionPrice appPrice = new AppVersionPrice(currency, 15.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersionNumber appVersionNumber = new AppVersionNumber("1.0");

        DeveloperId developerId = new DeveloperId("developerId");
        LocalDate localDate = LocalDate.now();
        fixture.given(new AppCreated(new AppId("id"), appName, AppType.APP, new AppVersionId("versionId"), appPrice,
                appShortDescription, appLongDescription,
                Arrays.asList(appVersionGalleryImage), appVersionNumber, AppVersionLifeCycle.ACTIVE,
                AppVersionVisibility.PUBLIC, developerId,
                AppLifeCycle.ACTIVE, localDate))
                .when(new DeleteAppVersionCommand(new AppId("id"), new AppVersionId("versionId"), AppVersionLifeCycle.ACTIVE, 0,
                        0))
                .expectEvents(new AppVersionDeleted(new AppId("id"), new AppVersionId("versionId"),
                        AppVersionLifeCycle.ACTIVE, 0, 0));
    }
}