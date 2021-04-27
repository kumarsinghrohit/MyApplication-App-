package com.manager.projection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import com.coreapi.valueobject.AppLifeCycle;
import com.coreapi.valueobject.AppVersionLifeCycle;

@ExtendWith(SpringExtension.class)
@Tag("Service")
@DisplayName("Testing AppDaoService")
public class AppDaoServiceTest {

    @Mock
    private AppRepository appRepository;
    @Mock
    private AppVersionEntity appVersionEntity;
    @Mock
    private ObjectMapper objectMapper;

    private @InjectMocks AppDaoService instanceUnderTest;

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(appRepository, appVersionEntity, objectMapper);
    }

    @Test
    @DisplayName("Testing create app.")
    public void when_createApp_Then_App_Created() throws IOException {
        AppEntity appEntity = mock(AppEntity.class);
        instanceUnderTest.createApp(appEntity);
        verify(appRepository).save(appEntity);
    }

    @Test
    @DisplayName("Testing update app.")
    public void when_UpdateApp_Then_App_Updated() throws IOException {
        instanceUnderTest.updateApp("id", "name");
        verify(appRepository).updateApp("id", "name");
    }

    @Test
    @DisplayName("Testing adding an app version to an existing app.")
    public void when_AddAppVersion_Then_AppVersion_Added() throws IOException {
        instanceUnderTest.addVersion("id", appVersionEntity);
        verify(appRepository).addVersion("id", appVersionEntity);
    }

    @Test
    @DisplayName("Testing finding apps by name.")
    public void when_findAppByName_Then_App_Returned() throws IOException {
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHit[] hit = { new SearchHit(1, "appId", null, null) };
        SearchHits hits = new SearchHits(hit, null, 0);
        LocalDate createdOn = LocalDate.now();
        AppPriceEntity appPriceEntity = new AppPriceEntity(Currency.getInstance("EUR"), 11);
        AppVersionGalleryImageEntity galleryImage = new AppVersionGalleryImageEntity("thumbnailImageName", "profileImageName");
        AppVersionEntity appVersionEntity = new AppVersionEntity("versionId", appPriceEntity, "shortDescription",
                "longDescription", Lists.newArrayList(galleryImage), "updateInformation", "versionNumber", "lifeCycle",
                "visibility", createdOn);
        AppEntity appEntity = new AppEntity("id", "name", "type", Lists.newArrayList(appVersionEntity), "developerId",
                AppLifeCycle.ACTIVE.getValue(), createdOn);

        when(appRepository.findAppByName("name")).thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(hits);
        when(objectMapper.convertValue(null, AppEntity.class)).thenReturn(appEntity);

        instanceUnderTest.findAppByName("name");
        verify(appRepository).findAppByName("name");
        verify(objectMapper).convertValue(null, AppEntity.class);
        verify(searchResponse).getHits();
    }

    @Test
    @DisplayName("Testing finding all versions of an existing app.")
    public void when_findversionNumbersById_Then_return_version_numbers() throws IOException {
        LocalDate createdOn = LocalDate.now();
        AppPriceEntity appPriceEntity = new AppPriceEntity(Currency.getInstance("EUR"), 11);
        AppVersionGalleryImageEntity galleryImage = new AppVersionGalleryImageEntity("thumbnailImageName", "profileImageName");
        AppVersionEntity appVersionEntity = new AppVersionEntity("versionId", appPriceEntity, "shortDescription",
                "longDescription", Lists.newArrayList(galleryImage), "updateInformation", "versionNumber",
                AppVersionLifeCycle.ACTIVE.getValue(),
                "visibility", createdOn);
        AppEntity appEntity = new AppEntity("id", "name", "type", Lists.newArrayList(appVersionEntity), "developerId",
                AppLifeCycle.ACTIVE.getValue(), createdOn);

        GetResponse getResponse = mock(GetResponse.class);
        when(appRepository.findById("id")).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(objectMapper.convertValue(any(Map.class), eq(AppEntity.class))).thenReturn(appEntity);
        when(getResponse.getId()).thenReturn("id");

        instanceUnderTest.findversionNumbersById("id");

        verify(appRepository).findById("id");
        verify(objectMapper).convertValue(any(Map.class), eq(AppEntity.class));
    }

    @Test
    @DisplayName("Testing find app by id.")
    public void when_findAppById_Then_ReturnApp() throws IOException {
        LocalDate createdOn = LocalDate.now();
        AppPriceEntity appPriceEntity = new AppPriceEntity(Currency.getInstance("EUR"), 11);
        AppVersionGalleryImageEntity galleryImage = new AppVersionGalleryImageEntity("thumbnailImageName", "profileImageName");
        AppVersionEntity appVersionEntity = new AppVersionEntity("versionId", appPriceEntity, "shortDescription",
                "longDescription", Lists.newArrayList(galleryImage), "updateInformation", "versionNumber",
                AppVersionLifeCycle.ACTIVE.getValue(),
                "visibility", createdOn);
        AppEntity appEntity = new AppEntity("id", "name", "type", Lists.newArrayList(appVersionEntity), "developerId",
                AppLifeCycle.ACTIVE.getValue(), createdOn);

        GetResponse getResponse = mock(GetResponse.class);
        when(appRepository.findById("id")).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(objectMapper.convertValue(any(Map.class), eq(AppEntity.class))).thenReturn(appEntity);
        when(getResponse.getId()).thenReturn("id");

        instanceUnderTest.findById("id");

        verify(appRepository).findById("id");
        verify(objectMapper).convertValue(any(Map.class), eq(AppEntity.class));
    }

    @Test
    @DisplayName("Testing update ShortDescription of a particular version of an existing app.")
    public void when_UpdateVersionShortDescription_Then_ShortDescription_Updated() throws IOException {
        instanceUnderTest.updateVersionShortDescription("appId", "versionId", "shortDescription");
        verify(appRepository).updateVersionShortDescription("appId", "versionId", "shortDescription");
    }

    @Test
    @DisplayName("Testing update LongDescription of a particular version of an existing app.")
    public void when_updateVersionLongDescription_Then_LongDescription_Updated() throws IOException {
        instanceUnderTest.updateVersionLongDescription("appId", "versionId", "LongDescription");
        verify(appRepository).updateVersionLongDescription("appId", "versionId", "LongDescription");
    }

    @Test
    @DisplayName("Testing update Price of a particular version of an existing app.")
    public void when_updateVersionPrice_Then_Price_Updated() throws IOException {
        instanceUnderTest.updateVersionPrice("appId", "versionId", 1.0);
        verify(appRepository).updateVersionPrice("appId", "versionId", 1.0);
    }

    @Test
    @DisplayName("Testing update GalleryImages of a particular version of an existing app.")
    public void when_updateVersionGalleryImages_Then_GalleryImages_Updated() throws IOException {
        AppVersionGalleryImageEntity appVersionGalleryImageEntity = mock(AppVersionGalleryImageEntity.class);
        instanceUnderTest.updateVersionGalleryImages("appId", "versionId", Arrays.asList(appVersionGalleryImageEntity));
        verify(appRepository).updateVersionGalleryImages("appId", "versionId", Arrays.asList(appVersionGalleryImageEntity));
    }

    @Test
    @DisplayName("Testing update UpdateInformation of a particular version of an existing app.")
    public void when_updateVersionUpdateInformation_Then_Info_Updated() throws IOException {
        instanceUnderTest.updateVersionUpdateInformation("appId", "versionId", "info");
        verify(appRepository).updateVersionUpdateInformation("appId", "versionId", "info");
    }

    @Test
    @DisplayName("Testing update Version Number of a particular version of an existing app.")
    public void when_updateVersionNumber_Then_VersionNumber_Updated() throws IOException {
        instanceUnderTest.updateVersionNumber("appId", "versionId", "1");
        verify(appRepository).updateVersionNumber("appId", "versionId", "1");
    }

    @Test
    @DisplayName("Testing delete a particular version of an existing app.")
    public void when_deleteAppVersion_Then_AppVersion_Deleted() throws IOException {
        instanceUnderTest.deleteAppVersion("appId", "versionId", "Active");
        verify(appRepository).deleteAppVersion("appId", "versionId", "Active");
    }

    @Test
    @DisplayName("Testing delete an existing app.")
    public void when_deleteApp_Then_App_Deleted() throws IOException {
        instanceUnderTest.deleteApp("appId", "Active");
        verify(appRepository).deleteApp("appId", "Active");
    }
}
