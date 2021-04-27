package com.manager.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

import com.google.common.collect.Lists;

import com.coreapi.valueobject.AppVersion;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionPrice;
import com.manager.aws.AwsStorageService;
import com.manager.config.messagesource.AppMessageSourceConfig;
import com.manager.configuration.keycloak.CustomKeycloakSpringBootConfigResolver;
import com.manager.configuration.keycloak.WithCustomKeycloackAuth;
import com.manager.configuration.security.ManagerSecurityConfig;
import com.manager.projection.App;
import com.manager.projection.FindAppByIdQuery;
import com.manager.projection.FindAppByNameQuery;

@ActiveProfiles("test")
@WebMvcTest(AppManagerController.class)
@WithCustomKeycloackAuth("user")
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
@ContextConfiguration(classes = { AppManagerController.class, AppManagerExceptionHandler.class, AppMessageSourceConfig.class,
        ManagerSecurityConfig.class, CustomKeycloakSpringBootConfigResolver.class })
@DisplayName("AppManagerController:: Rest api test cases.")
@Tag("Controller")
public class AppManagerControllerTest {
    @Value("classpath:/upload-apps-original")
    private Resource sourceDirectory;

    @Value("classpath:/upload-apps-test")
    private Resource destinationDirectory;

    @Value("classpath:/upload-apps-test/test-image.png")
    private Resource resourceFile;

    @Value("classpath:/upload-apps-test/test_data.txt")
    private Resource textResourceFile;

    @MockBean
    private CommandGateway commandGateway;

    @MockBean
    private QueryGateway queryGateway;

    @MockBean
    private AwsStorageService awsStorageService;

    @MockBean
    private AppContentUploadService appContentUploadService;

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Update name of an App.")
    public void whenUpdateApp_ThenReturnSuccessResultTest() throws Exception {
        List<App> listOfApps = new ArrayList<>();
        listOfApps.add(mockApp());
        CompletableFuture<List<App>> apps = new CompletableFuture<>();
        apps.complete(listOfApps);
        when(queryGateway.query(any(FindAppByNameQuery.class), any(ResponseType.class))).thenReturn(apps);
        mockMvc.perform(put("/apps/{appId}", "appId").header("origin", "*").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "appName").param("appType", "App").param("developerId", "developerId")).andExpect(status().isOk());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Update invalid app name then InvalidAppDataException")
    public void updateAppName_thenInvalidAppDataExceptionTest() throws Exception {
        List<App> listOfApps = new ArrayList<>();
        listOfApps.add(mockApp());
        CompletableFuture<List<App>> apps = new CompletableFuture<>();
        apps.complete(listOfApps);
        when(queryGateway.query(any(FindAppByNameQuery.class), any(ResponseType.class)))
                .thenReturn(apps);
        String contentAsString = mockMvc
                .perform(put("/apps/{appId}", "appId").header("origin", "*").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "app").param("appType", "App").param("developerId", "developerId"))
                .andReturn().getResponse().getContentAsString();
        assertThat(contentAsString)
                .isEqualTo("The name should contain a minimum of 4 characters and a maximum of 35 characters.<br/>");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Update duplicate app name then InvalidAppDataException")
    public void updateApp_With_Duplicate_Name_thenInvalidAppDataExceptionTest() throws Exception {
        List<App> listOfApps = new ArrayList<>();
        listOfApps.add(mockApp());
        CompletableFuture<List<App>> apps = new CompletableFuture<>();
        apps.complete(listOfApps);
        when(queryGateway.query(any(FindAppByNameQuery.class), any(ResponseType.class)))
                .thenReturn(apps);
        String contentAsString = mockMvc
                .perform(put("/apps/{appId}", "appId").header("origin", "*").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "name").param("appType", "App").param("developerId", "developerId"))
                .andReturn().getResponse().getContentAsString();
        assertThat(contentAsString)
                .isEqualTo("name already exists. Please provide a unique name.<br/>");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Update invalid app name and except IOException")
    public void when_updateAppName_for_nonExistentApp_thenIOException() throws Exception {
        List<App> listOfApps = new ArrayList<>();
        listOfApps.add(mockApp());
        CompletableFuture<List<App>> apps = new CompletableFuture<>();
        apps.complete(listOfApps);
        when(queryGateway.query(any(FindAppByNameQuery.class), any(ResponseType.class)))
                .thenReturn(apps);
        String contentAsString = mockMvc
                .perform(put("/apps/{appId}", "appId").header("origin", "*").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "app").param("appType", "App").param("developerId", "developerId"))
                .andReturn().getResponse().getContentAsString();
        assertThat(contentAsString)
                .isEqualTo("The name should contain a minimum of 4 characters and a maximum of 35 characters.<br/>");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("when creating app then new app added")
    public void whenCreateApp_thenNewAppAdded() throws Exception {
        copyFiles();
        File file = new File(resourceFile.getFile().getPath());
        FileInputStream fileInputStream = new FileInputStream(file);
        MockMultipartFile multipart = new MockMultipartFile(
                "file", file.getName(), "multipart/form-data",
                fileInputStream);
        MockMultipartFile textMultipartfile = new MockMultipartFile("app.txt", "test_data.txt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, textResourceFile.getInputStream());
        CompletableFuture<List<App>> apps = new CompletableFuture<>();
        apps.complete(Arrays.asList(mockApp()));
        when(queryGateway.query(any(FindAppByNameQuery.class), any(ResponseType.class))).thenReturn(apps);
        MockHttpServletResponse response = mockMvc.perform(multipart("/apps").file("binary", textMultipartfile.getBytes())
                .file(multipart).file(textMultipartfile).header("origin", "*")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "appName").param("price", "11.0").param("shortDescription", "shortDescription")
                .param("longDescription", "longDescription123").param("version", "version").param("appType", "App")
                .param("developerId", "developerId")).andExpect(status().isOk()).andReturn().getResponse();
        assertThat(response.getContentAsString()).isEqualTo("You successfully added app appName to your app.");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("create app with duplicate app name then InvalidAppDataException")
    public void createApp_DuplicateAppName_thenInvalidAppDataExceptionTest() throws Exception {
        List<App> listOfApps = new ArrayList<>();
        listOfApps.add(mockApp());
        CompletableFuture<List<App>> apps = new CompletableFuture<>();
        apps.complete(listOfApps);
        when(queryGateway.query(any(FindAppByNameQuery.class), any(ResponseType.class)))
                .thenReturn(apps);
        String contentAsString = mockMvc
                .perform(post("/apps").header("origin", "*").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "name").param("price", "11.0").param("shortDescription", "shortDescription")
                        .param("longDescription", "longDescription123").param("version", "version").param("appType", "App")
                        .param("developerId", "developerId"))
                .andReturn().getResponse().getContentAsString();
        assertThat(contentAsString).isEqualTo("name already exists. Please provide a unique name.<br/>");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("create app with invalid data then InvalidAppDataException")
    public void createApp_With_InvalidData_thenInvalidAppDataExceptionTest() throws Exception {
        List<App> listOfApps = new ArrayList<>();
        listOfApps.add(mockApp());
        CompletableFuture<List<App>> apps = new CompletableFuture<>();
        apps.complete(listOfApps);
        when(queryGateway.query(any(FindAppByNameQuery.class), any(ResponseType.class)))
                .thenReturn(apps);
        String contentAsString = mockMvc
                .perform(post("/apps").header("origin", "*").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "app").param("price", "invalidprice").param("shortDescription", "short")
                        .param("longDescription", "longDescription").param("version", "version").param("appType", "a")
                        .param("developerId", ""))
                .andReturn().getResponse().getContentAsString();
        assertNotNull(contentAsString);
        assertTrue(contentAsString.contains("The name should contain a minimum of 4"));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("delete app or library.")
    public void when_DeleteApp_then_App_DeletedSuccessfully() throws Exception {
        CompletableFuture<App> apps = new CompletableFuture<>();
        apps.complete(mockApp());
        when(queryGateway.query(any(FindAppByIdQuery.class), any(ResponseType.class))).thenReturn(apps);
        MockHttpServletResponse response = mockMvc.perform(delete("/apps/{appId}", "appId").header("origin", "*"))
                .andExpect(status().isOk()).andReturn().getResponse();
        assertThat(response.getContentAsString()).isEqualTo("You have successfully deleted name type.");
    }

    private void copyFiles() throws IOException {
        File sourceFolder = new File(sourceDirectory.getFile().getPath());
        File destinationFolder = new File(destinationDirectory.getFile().getPath());
        FileSystemUtils.copyRecursively(sourceFolder, destinationFolder);
    }

    private App mockApp() {
        LocalDate createdOn = LocalDate.now();
        AppVersionPrice appPrice = new AppVersionPrice(Currency.getInstance("EUR"), 11.0);
        AppVersionGalleryImage galleryImage = new AppVersionGalleryImage("thumbnailImageName", "profileImageName");
        AppVersion appVersion = new AppVersion("versionId", appPrice, "shortDescription",
                "longDescription", Lists.newArrayList(galleryImage), "updateInformation", "versionNumber", "lifeCycle",
                "visibility", createdOn);
        return new App("id", "name", "type", Lists.newArrayList(appVersion), "developerId", "Active", createdOn);
    }
}
