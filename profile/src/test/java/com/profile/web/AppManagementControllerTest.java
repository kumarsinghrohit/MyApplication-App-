package com.profile.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.google.common.collect.Lists;

import com.coreapi.valueobject.AppVersion;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionPrice;
import com.profile.configuration.WithCustomKeycloackAuth;
import com.profile.query.App;
import com.profile.query.FindAppByIdQuery;
import com.profile.query.FindAppsByDeveloperIdAndTypeQuery;

@WebMvcTest(AppManagementController.class)
@WithCustomKeycloackAuth("user")
@ContextConfiguration(classes = { AppManagementController.class })
@Tag("Controller")
@DisplayName("Testing App management related rest apis")
public class AppManagementControllerTest {

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    @MockBean
    private QueryGateway queryGateway;

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Testing get developed apps for logged in user")
    public void when_getDevelopedApps_then_return_all_developed_apps() throws Exception {
        LocalDate createdOn = LocalDate.now();
        AppVersionPrice appPrice = new AppVersionPrice(Currency.getInstance("EUR"), 11.0);
        AppVersionGalleryImage galleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersion appVersion = new AppVersion("versionId", appPrice, "shortDescription",
                "longDescription", Lists.newArrayList(galleryImage), "updateInformation", "versionNumber", "lifeCycle",
                "visibility", createdOn);
        App app = new App("id", "name", "type", Lists.newArrayList(appVersion), createdOn);

        CompletableFuture<List<App>> apps = new CompletableFuture<>();
        apps.complete(Lists.newArrayList(app));
        when(queryGateway.query(any(FindAppsByDeveloperIdAndTypeQuery.class), any(ResponseType.class))).thenReturn(apps);

        MockHttpServletResponse response = mockMvc.perform(get("/").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andReturn().getResponse();
        assertThat(response.getContentType()).isEqualTo(CONTENT_TYPE);
        assertThat(response.getContentAsString()).contains("Developed Apps");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Testing get developed libraries for logged in user")
    public void when_getDeveloped_liberaries_then_return_all_developed_liberaries() throws Exception {
        LocalDate createdOn = LocalDate.now();
        AppVersionPrice appPrice = new AppVersionPrice(Currency.getInstance("EUR"), 11.0);
        AppVersionGalleryImage galleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersion appVersion = new AppVersion("versionId", appPrice, "shortDescription",
                "longDescription", Lists.newArrayList(galleryImage), "updateInformation", "versionNumber", "lifeCycle",
                "visibility", createdOn);
        App app = new App("id", "name", "type", Lists.newArrayList(appVersion), createdOn);

        CompletableFuture<List<App>> apps = new CompletableFuture<>();
        apps.complete(Lists.newArrayList(app));
        when(queryGateway.query(any(FindAppsByDeveloperIdAndTypeQuery.class), any(ResponseType.class))).thenReturn(apps);

        MockHttpServletResponse response = mockMvc.perform(get("/").param("type", "Library").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk()).andReturn().getResponse();

        assertThat(response.getContentType()).isEqualTo(CONTENT_TYPE);
        assertThat(response.getContentAsString()).contains("Developed Libraries");
    }

    @Test
    @DisplayName("Testing create apps")
    public void when_createApps_then_return_create_app_lib_page() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/apps/create").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk()).andReturn().getResponse();

        assertThat(response.getContentType()).isEqualTo(CONTENT_TYPE);
        assertThat(response.getContentAsString()).contains("Developed Apps");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Testing edit apps")
    public void when_editApps_then_return_edit_app_lib_page() throws Exception {
        LocalDate createdOn = LocalDate.now();
        AppVersionPrice appPrice = new AppVersionPrice(Currency.getInstance("EUR"), 11.0);
        AppVersionGalleryImage galleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersion appVersion = new AppVersion("versionId", appPrice, "shortDescription",
                "longDescription", Lists.newArrayList(galleryImage), "updateInformation", "versionNumber", "lifeCycle",
                "visibility", createdOn);
        App app = new App("id", "name", "type", Lists.newArrayList(appVersion), createdOn);

        CompletableFuture<App> completableFutureApp = new CompletableFuture<>();
        completableFutureApp.complete(app);
        when(queryGateway.query(any(FindAppByIdQuery.class), any(ResponseType.class))).thenReturn(completableFutureApp);
        MockHttpServletResponse response = mockMvc.perform(get("/apps/edit/{id}", "id").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk()).andReturn().getResponse();

        assertThat(response.getContentType()).isEqualTo(CONTENT_TYPE);
        assertThat(response.getContentAsString()).contains("Update Developed App");
    }
}
