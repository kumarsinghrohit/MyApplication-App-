package com.manager.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.util.NestedServletException;

import com.manager.config.messagesource.AppMessageSourceConfig;
import com.manager.configuration.keycloak.CustomKeycloakSpringBootConfigResolver;
import com.manager.configuration.keycloak.WithCustomKeycloackAuth;
import com.manager.configuration.security.ManagerSecurityConfig;
import com.manager.projection.AppVersionNumberMap;
import com.manager.projection.GetAppVersionsNumberByIdQuery;

@ActiveProfiles("test")
@WebMvcTest(AppVersionManagerController.class)
@WithCustomKeycloackAuth("user")
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
@ContextConfiguration(classes = { AppVersionManagerController.class, AppMessageSourceConfig.class,
        ManagerSecurityConfig.class, CustomKeycloakSpringBootConfigResolver.class })
@Tag("Controller")
@DisplayName("Testing App version management related rest apis")
public class AppVersionManagerControllerTest {

    @Value("classpath:/upload-apps-original")
    private Resource sourceDirectory;
    @Value("classpath:/upload-apps-test")
    private Resource destinationDirectory;
    @Value("classpath:/upload-apps-test/test-image.png")
    private Resource resourceFile;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AppContentUploadService appContentUploadService;
    @MockBean
    private CommandGateway commandGateway;
    @MockBean
    private QueryGateway queryGateway;

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Testing adding an app version to an existing app.")
    public void whenAddAppVesrion_thenVersionAdded() throws Exception {
        copyFiles();
        File file = new File(resourceFile.getFile().getPath());
        FileInputStream fileInputStream = new FileInputStream(file);
        MockMultipartFile multipart = new MockMultipartFile(
                "file", file.getName(), "multipart/form-data",
                fileInputStream);
        AppVersionNumberMap appVersionNumberMap = new AppVersionNumberMap(Collections.singletonMap("1.0", "versionId"));
        CompletableFuture<AppVersionNumberMap> versionNumbers = new CompletableFuture<>();
        versionNumbers.complete(appVersionNumberMap);
        when(queryGateway.query(any(GetAppVersionsNumberByIdQuery.class), any(ResponseType.class)))
                .thenReturn(versionNumbers);
        MockHttpServletResponse response = mockMvc.perform(
                multipart("/apps/{appId}/versions", "appId").file(multipart).header("origin", "*")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "appName").param("price", "11.0").param("shortDescription", "shortDescription")
                        .param("longDescription", "longDescription123").param("version", "version")
                        .param("whatsNewInVersion", "whatsNewInVersion")
                        .param("developerId", "developerId"))
                .andExpect(status().isOk()).andReturn().getResponse();
        assertThat(response.getContentAsString()).isEqualTo("You successfully added version version to appName.");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Testing adding an app version with invalid data to an existing app.")
    public void whenAddAppVesrion_WithInvalid_Data_then_Exception() throws Exception {
        AppVersionNumberMap appVersionNumberMap = new AppVersionNumberMap(Collections.singletonMap("1.0", "versionId"));
        CompletableFuture<AppVersionNumberMap> versionNumbers = new CompletableFuture<>();
        versionNumbers.complete(appVersionNumberMap);
        when(queryGateway.query(any(GetAppVersionsNumberByIdQuery.class), any(ResponseType.class)))
                .thenReturn(versionNumbers);
        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(post("/apps/{appId}/versions", "appId").header("origin", "*")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "appName").param("price", "invalidPrice").param("shortDescription", "short")
                        .param("longDescription", "longDescription").param("version", "1.0")
                        .param("whatsNewInVersion", "whatsNewInVersion")
                        .param("developerId", "")));
        assertThat(exception.getCause()).isInstanceOf(InvalidAppDataException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Testing updating an app version to an of existing app.")
    public void when_UpdateAppNersion_thenAppVersionUpdated() throws Exception {
        copyFiles();
        File file = new File(resourceFile.getFile().getPath());
        FileInputStream fileInputStream = new FileInputStream(file);
        MockMultipartFile multipart = new MockMultipartFile(
                "file", file.getName(), "multipart/form-data",
                fileInputStream);
        AppVersionNumberMap appVersionNumberMap = new AppVersionNumberMap(Collections.singletonMap("1.0", "versionId"));
        CompletableFuture<AppVersionNumberMap> versionNumbers = new CompletableFuture<>();
        versionNumbers.complete(appVersionNumberMap);
        when(queryGateway.query(any(GetAppVersionsNumberByIdQuery.class), any(ResponseType.class)))
                .thenReturn(versionNumbers);
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/apps/{appId}/versions/{versionId}",
                "appId", "versionId");
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });
        MockHttpServletResponse response = mockMvc.perform(
                builder.file(multipart).header("origin", "*")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "name").param("price", "1.0").param("shortDescription", "shortDescription")
                        .param("longDescription", "longDescription1").param("version", "version")
                        .param("whatsNewInVersion", "whatsNewInVersion")
                        .param("developerId", "developerId"))
                .andExpect(status().isOk()).andReturn().getResponse();
        assertThat(response.getContentAsString()).isEqualTo("You successfully updated version version of name.");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Testing updating an app version with invalid data to an existing app.")
    public void whenUpdateApp_WithInvalid_Data_thenNewAppAdded() throws Exception {
        AppVersionNumberMap appVersionNumberMap = new AppVersionNumberMap(Collections.singletonMap("1.0", "versionId"));
        CompletableFuture<AppVersionNumberMap> versionNumbers = new CompletableFuture<>();
        versionNumbers.complete(appVersionNumberMap);
        when(queryGateway.query(any(GetAppVersionsNumberByIdQuery.class), any(ResponseType.class)))
                .thenReturn(versionNumbers);
        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(put("/apps/{appId}/versions/{versionId}", "appId", "versionId1").header("origin", "*")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "appName").param("price", "invalidPrice").param("shortDescription", "shortDescription")
                        .param("longDescription", "longDescription").param("version", "1.0")
                        .param("whatsNewInVersion", "whatsNewInVersion")
                        .param("developerId", "")));
        assertThat(exception.getCause()).isInstanceOf(InvalidAppDataException.class);
    }

    private void copyFiles() throws IOException {
        File sourceFolder = new File(sourceDirectory.getFile().getPath());
        File destinationFolder = new File(destinationDirectory.getFile().getPath());
        FileSystemUtils.copyRecursively(sourceFolder, destinationFolder);
    }
}
