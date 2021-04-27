package com.profile.web;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.coreapi.valueobject.AppVersion;
import com.profile.query.App;
import com.profile.query.FindAppByIdQuery;

/**
 * Controller responsible for serving pages related to management of app
 * versions. Returns Thymeleaf templates of the pages.
 */
@Controller
@PreAuthorize("hasRole('user')")
@RequestMapping("/apps/{appId}/versions")
public class AppVersionManagementController {
    private static final String APPS_PREFIX = "apps/";
    public static final String APPS_VERSION_PREFIX = APPS_PREFIX + "versions/";
    private final QueryGateway queryGateway;

    @Value("${cloud.aws.endpoint.url}")
    private String endpointUrl;

    AppVersionManagementController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;

    }

    @GetMapping("/add")
    public String addAppVersionPage(@RequestParam(defaultValue = "App") String appType, @PathVariable String appId, Model model)
            throws InterruptedException, ExecutionException {
        App app = queryGateway
                .query(new FindAppByIdQuery(appId), ResponseTypes.instanceOf(App.class))
                .get();
        CreateAppVersionDto createAppVersionDto = new CreateAppVersionDto(appId, app.getType(),
                getResourceUrl(APPS_PREFIX + appId), app.getName(),
                app.getCreatedOn());
        model.addAttribute("editAppVersionDto", createAppVersionDto);
        return "apps/add-new-version";
    }

    @GetMapping("/{versionId}")
    public String updateAppVersionPage(@PathVariable String appId, @PathVariable String versionId, Model model)
            throws InterruptedException, ExecutionException {
        App app = queryGateway
                .query(new FindAppByIdQuery(appId), ResponseTypes.instanceOf(App.class))
                .get();
        EditAppVersionDto editableAppVersionDto = convertToEditableAppVersionDto(app, versionId);

        model.addAttribute("editableAppVersionDto", editableAppVersionDto);
        model.addAttribute("appType", app.getType());
        return "apps/update-existing-version";
    }

    @ModelAttribute("user")
    UserInfo addLoggedUserInfoToModel(Principal principal) {
        UserInfo userInfo = null;
        if (Objects.nonNull(principal)) {
            userInfo = new UserInfo(principal.getName(), null);
        }
        return userInfo;
    }

    private EditAppVersionDto convertToEditableAppVersionDto(App app, String versionId) {
        AppVersion appVersion = app.getAppVersions().stream().filter(version -> Objects.equals(version.getVersionId(), versionId))
                .collect(Collectors.toList()).get(0);
        String imageUrl = getResourceUrl(APPS_PREFIX + app.getId());
        List<String> galleryImageUrls = appVersion.getGalleryImages().stream()
                .map(appVersionGalleryImage -> getResourceUrl(appVersionGalleryImage.getThumbnailImageName()))
                .collect(Collectors.toList());
        return new EditAppVersionDto(app.getId(), versionId, imageUrl, app.getName(),
                appVersion.getCreatedOn(), appVersion.getPrice().getValue(), appVersion.getShortDescription(),
                appVersion.getLongDescription(), galleryImageUrls,
                appVersion.getUpdateInformation(), appVersion.getVersionNumber(),
                getResourceUrl(APPS_VERSION_PREFIX + versionId));
    }

    private String getResourceUrl(String key) {
        return new StringBuilder(endpointUrl).append("/").append(key).toString();
    }
}
