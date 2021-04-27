package com.manager.command;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import com.coreapi.valueobject.*;
import com.coreapi.valueobject.AppVersion;
import org.axonframework.test.saga.FixtureConfiguration;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.coreapi.event.AppDeleted;
import com.coreapi.event.AppVersionDeleted;
import com.coreapi.valueobject.AppId;
import com.coreapi.valueobject.AppLifeCycle;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionId;
import com.coreapi.valueobject.AppVersionLifeCycle;
import com.coreapi.valueobject.AppVersionLongDescription;
import com.coreapi.valueobject.AppVersionNumber;
import com.coreapi.valueobject.AppVersionPrice;
import com.coreapi.valueobject.AppVersionShortDescription;
import com.coreapi.valueobject.AppVersionUpdateInfo;
import com.coreapi.valueobject.AppVersionVisibility;

public class AppVersionLifeCycleManagementSagaTest {

    private FixtureConfiguration testFixture;

    @BeforeEach
    public void setUp() throws Exception {
        testFixture = new SagaTestFixture<>(AppVersionLifeCycleManagementSaga.class);
        testFixture.withTransienceCheckDisabled();
    }

    @Test
    public void testAppDeleted() throws Exception {
        String appId = "id";
        testFixture.givenNoPriorActivity()
                .whenAggregate(appId)
                .publishes(new AppDeleted(new AppId(appId), AppType.APP, AppLifeCycle.DISCONTINUED, Arrays.asList(getAppVersion())))
                .expectActiveSagas(1)
                .expectDispatchedCommands(new DeleteAppVersionCommand(new AppId(appId),
                        new AppVersionId(getAppVersion().getVersionId()), AppVersionLifeCycle.DISCONTINUED, 1, 1));
    }

    @Test
    public void testAppVersionDeleted() throws Exception {
        String appId = "id";
        testFixture.givenAggregate(appId)
                .published(new AppDeleted(new AppId(appId), AppType.APP, AppLifeCycle.DISCONTINUED, Arrays.asList(getAppVersion())))
                .whenAggregate(appId).publishes(new AppVersionDeleted(new AppId(appId),
                        new AppVersionId(getAppVersion().getVersionId()), AppVersionLifeCycle.DISCONTINUED, 1, 1))
                .expectActiveSagas(0);
    }

    private AppVersion getAppVersion() {
        AppVersionId appVersionId = new AppVersionId("versionId");
        AppVersionShortDescription appShortDescription = new AppVersionShortDescription("productShortDescription");
        AppVersionLongDescription appLongDescription = new AppVersionLongDescription("productLongDescription");
        Currency currency = Currency.getInstance(Locale.UK);
        AppVersionPrice appPrice = new AppVersionPrice(currency, 15.0);
        AppVersionGalleryImage appVersionGalleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersionUpdateInfo updateInfo = new AppVersionUpdateInfo("app update info has minimum length");
        AppVersionNumber appVersionNumber = new AppVersionNumber("1.0");
        AppVersion appVersion = new AppVersion(appVersionId.getValue(),
                appPrice, appShortDescription.getValue(), appLongDescription.getValue(),
                Arrays.asList(appVersionGalleryImage), updateInfo.getValue(), appVersionNumber.getValue(),
                AppVersionLifeCycle.ACTIVE.getValue(), AppVersionVisibility.PUBLIC.getValue(), LocalDate.now());
        return appVersion;
    }
}
