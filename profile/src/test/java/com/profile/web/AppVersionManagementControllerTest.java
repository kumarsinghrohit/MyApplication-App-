package com.profile.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.google.common.collect.Lists;

import com.coreapi.valueobject.AppVersion;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionPrice;
import com.profile.configuration.WithCustomKeycloackAuth;
import com.profile.query.App;
import com.profile.query.FindAppByIdQuery;

@WebMvcTest(AppVersionManagementController.class)
@WithCustomKeycloackAuth("user")
@ContextConfiguration(classes = { AppVersionManagementController.class })
@Tag("Controller")
@DisplayName("Testing App version management related rest apis")
public class AppVersionManagementControllerTest {

    @MockBean
    private QueryGateway queryGateway;

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Testing get add App Version Page.")
    public void when_addAppVersionPage_then_return_add_new_version_page() throws Exception {
        LocalDate createdOn = LocalDate.now();
        AppVersion appVersion = mock(AppVersion.class);
        App app = new App("id", "name", "type", Lists.newArrayList(appVersion), createdOn);
        CompletableFuture<App> completableFutureApp = new CompletableFuture<>();
        completableFutureApp.complete(app);
        when(queryGateway.query(any(FindAppByIdQuery.class), any(ResponseType.class))).thenReturn(completableFutureApp);

        mockMvc.perform(get("/apps/{appId}/versions/add", "appId").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());

    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Testing get update App Version Page.")
    public void when_UpdateVersionPage_then_return_Update_app_version_page() throws Exception {
        LocalDate createdOn = LocalDate.now();
        AppVersionPrice price = new AppVersionPrice(Currency.getInstance("EUR"), 1.0);
        List<AppVersionGalleryImage> galleryImages = Arrays
                .asList(new AppVersionGalleryImage("thumbnailImageName", "profileImageName"));
        AppVersion appVersion = new AppVersion("versionId", price, "shortDescription", "longDescription", galleryImages,
                "updateInformation", "versionNumber", "lifeCycle", "visibility", createdOn);
        App app = new App("id", "name", "type", Arrays.asList(appVersion), createdOn);
        CompletableFuture<App> app1 = new CompletableFuture<>();
        app1.complete(app);
        when(queryGateway.query(any(FindAppByIdQuery.class), any(ResponseType.class))).thenReturn(app1);

        mockMvc.perform(get("/apps/{appId}/versions/{versionId}", "appId", "versionId").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk());

    }
}
