package com.profile.web;

import java.security.Principal;
import java.util.Collections;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.coreapi.valueobject.AppLifeCycle;
import com.coreapi.valueobject.AppVersion;
import com.profile.query.App;
import com.profile.query.FindAppByIdQuery;
import com.profile.query.FindAppsByDeveloperIdAndTypeQuery;

/**
 * A controller that exposes end points that generate the various client-side
 * HTML pages for {@link App} management.
 */
@Controller
@PreAuthorize("hasRole('user')")
class AppManagementController {

    private static final String APPS_PREFIX = "apps/";
    private static final String TYPE_APP = "App";
    private static final String TYPE_LIBRARY = "Library";
    private final QueryGateway queryGateway;
    @Value("${cloud.aws.endpoint.url}")
    private String endpointUrl;

    AppManagementController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @ModelAttribute("user")
    UserInfo addLoggedUserInfoToModel(Principal principal) {
        UserInfo userInfo = null;
        if (Objects.nonNull(principal)) {
            userInfo = new UserInfo(principal.getName(), null);
        }
        return userInfo;
    }

    /**
     * End point responsible to show the page that lists all apps or libraries that
     * was developed by the logged in user.
     *
     * @param model   : {@link Model} instance to hold model attributes.
     * @param appType : type of application. E.g. App or Library
     * @return : page name that will render the app/lib developed by a user.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/")
    public String getDevelopedApps(Principal principal, Model model,
            @RequestParam(name = "type", defaultValue = "App") String appType)
            throws InterruptedException, ExecutionException {
        if (Objects.equals("App", appType)) {
            return showDevelopedApps(principal, model);
        }
        return showDevelopedLibraries(principal, model);
    }

    @GetMapping("/apps/create")
    public String appLibCreateForm(@RequestParam(defaultValue = "App") String appType, Model model) {
        model.addAttribute("appType", appType);
        return "apps/create-app-lib";
    }

    @GetMapping("/apps/edit/{id}")
    public String editDevelopedApp(@PathVariable String id, Model model, Principal principal)
            throws InterruptedException, ExecutionException {
        App app = queryGateway
                .query(new FindAppByIdQuery(id), ResponseTypes.instanceOf(App.class))
                .get();
        EditApplicationDto mapAppToEditApplicationDto = convertToEditApplicationDto(app, principal.getName());
        List<ApplicationVersionDto> versionDto = convertToApplicationVersionDto(app);
        model.addAttribute("appVersions", versionDto);
        model.addAttribute("app", mapAppToEditApplicationDto);
        return "apps/edit-app-lib";
    }

    private String showDevelopedApps(Principal principal, Model model) throws InterruptedException, ExecutionException {
        List<App> apps = queryGateway
                .query(new FindAppsByDeveloperIdAndTypeQuery(TYPE_APP, principal.getName(),
                        AppLifeCycle.ACTIVE.getValue()),
                        ResponseTypes.multipleInstancesOf(App.class))
                .get();
        List<ApplicationDto> applicationsDto = apps.stream()
                .map(this::convertToApplicationDto)
                .collect(Collectors.toList());
        model.addAttribute("developedApps", applicationsDto);
        return "apps/developed-apps";
    }

    private String showDevelopedLibraries(Principal principal, Model model) throws InterruptedException, ExecutionException {
        List<App> apps = queryGateway
                .query(new FindAppsByDeveloperIdAndTypeQuery(TYPE_LIBRARY, principal.getName(),
                        AppLifeCycle.ACTIVE.getValue()),
                        ResponseTypes.multipleInstancesOf(App.class))
                .get();
        List<ApplicationDto> applicationsDto = apps.stream()
                .map(this::convertToApplicationDto)
                .collect(Collectors.toList());
        model.addAttribute("developedLibs", applicationsDto);
        return "apps/developed-libraries";
    }

    private EditApplicationDto convertToEditApplicationDto(App app, String developerId) {
        return new EditApplicationDto(app.getId(), app.getName(), app.getType(), developerId,
                getImageUrl(app.getId()), app.getCreatedOn());
    }

    private List<ApplicationVersionDto> convertToApplicationVersionDto(App app) {
        List<AppVersion> appVersions = app.getAppVersions();
        Collections.sort(appVersions);
        return appVersions.stream().map(this::mapApplicationVersionDataToDto).collect(Collectors.toList());
    }

    private ApplicationVersionDto mapApplicationVersionDataToDto(AppVersion appVersion) {
        return new ApplicationVersionDto(appVersion.getVersionId(), appVersion.getPrice().getValue(), appVersion.getShortDescription(),
                appVersion.getLongDescription(), appVersion.getGalleryImages(), appVersion.getUpdateInformation(),
                appVersion.getVersionNumber(), appVersion.getVersionLifeCycle(), appVersion.getVisibility(),
                appVersion.getCreatedOn());
    }

    private ApplicationDto convertToApplicationDto(App app) {
        int latestAppVersion = app.getAppVersions().size() - 1;
        AppVersion appVersion = app.getAppVersions().get(latestAppVersion);
        return new ApplicationDto(app.getId(), app.getName(), getImageUrl(app.getId()), appVersion.getVersionLifeCycle(),
                appVersion.getVisibility(), appVersion.getCreatedOn());
    }

    public String getImageUrl(String appId) {
        String key = APPS_PREFIX + appId;
        return new StringBuilder(endpointUrl).append("/").append(key).toString();
    }
}
