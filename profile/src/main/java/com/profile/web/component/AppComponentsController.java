package com.profile.web.component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coreapi.valueobject.AppVersion;
import com.profile.query.App;
import com.profile.query.FindAppByIdQuery;
import com.profile.web.ApplicationVersionDto;

/**
 * Controller responsible for serving various app related UI components to be
 * used by other services.
 */
@Controller
@PreAuthorize("permitAll")
@RequestMapping(value = "/inc/{appId}")
public class AppComponentsController {

    private static final String APPS_PREFIX = "apps/";
    @Value("${cloud.aws.endpoint.url}")
    private String endpointUrl;
    private static final Logger LOGGER = LoggerFactory.getLogger(AppComponentsController.class);

    private final QueryGateway queryGateway;

    AppComponentsController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @ModelAttribute("FrontendUrl")
    public String getFrontendUrl(@Value("${frontend-url}") String contextPath) {
        return contextPath;
    }

    /**
     * Controller responsible for serving the product card component.
     *
     * @param appId               The ID of the product whose product card component
     *                            is sought.
     * @param destination         The URL which should be navigated to, when the
     *                            product card is clicked.
     * @param isStatic            Boolean indicating whether we want to disable
     *                            hover animation. False by default.
     * @param isReduced           If true, returns a reduced version of the card
     *                            with ratings and price removed. False by default.
     * @param iconBackgroundColor The hex color code for the background of the icon.
     *                            Default #F5F5F5.
     * @return HTML template for the product card.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @GetMapping("/app-catalog-card")
    public String getAppCatalogCard(@PathVariable(name = "appId") String appId,
            @RequestParam(name = "target", defaultValue = "") String destination,
            @RequestParam(name = "static", defaultValue = "false") Boolean isStatic,
            @RequestParam(name = "reduced", defaultValue = "false") Boolean isReduced,
            @RequestParam(name = "icon_background_color", defaultValue = "transparent") String iconBackgroundColor, Model model)
            throws InterruptedException, ExecutionException {
        LOGGER.info(
                "Request for app catalog card info with request info: product id: {}, destination: {}, static: {}, reduced: {}, icon_background_color: {}",
                appId, destination, isStatic, isReduced, iconBackgroundColor);

        App app = queryGateway.query(new FindAppByIdQuery(appId), ResponseTypes.instanceOf(App.class)).get();
        AppVersion appVersion = app.getAppVersions().get(app.getAppVersions().size() - 1);

        model.addAttribute("app", convertToApplicationDto(app));
        model.addAttribute("version", convertToApplicationVersionDto(appVersion));
        model.addAttribute("static", isStatic);
        model.addAttribute("reduced", isReduced);
        model.addAttribute("icon_background_color", iconBackgroundColor);
        model.addAttribute("target", destination);
        return "apps/app-catalog-card";
    }

    /**
     * A component that returns HTML text indicating the type [app/library] of the
     * app.
     *
     * @param appId The ID of the app whose app type is to be gotten as HTML text.
     * @return HTML template containing text indicating app type
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @GetMapping("/app-type")
    @ResponseBody
    public String getAppTypeComponent(@PathVariable String appId) throws InterruptedException, ExecutionException {
        App app = queryGateway.query(new FindAppByIdQuery(appId), ResponseTypes.instanceOf(App.class)).get();
        return app.getType();
    }

    /**
     * A component that returns HTML text indicating the name of the app.
     *
     * @param appId The ID of the app whose name is to be gotten as HTML text.
     * @return HTML template containing text indicating app name
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @GetMapping("/app-name")
    @ResponseBody
    public String getAppNameComponent(@PathVariable String appId) throws InterruptedException, ExecutionException {
        App app = queryGateway.query(new FindAppByIdQuery(appId), ResponseTypes.instanceOf(App.class)).get();
        return app.getName();
    }

    /**
     * A component that returns HTML text indicating the short description of the
     * app.
     *
     * @param appId The ID of the app whose short description is to be gotten as
     *              HTML text.
     * @return HTML template containing text indicating app short description
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @GetMapping("/short-description")
    @ResponseBody
    public String getAppShortDescriptionComponent(@PathVariable String appId) throws InterruptedException, ExecutionException {
        App app = queryGateway.query(new FindAppByIdQuery(appId), ResponseTypes.instanceOf(App.class)).get();
        List<AppVersion> appVersions = app.getAppVersions();

        return appVersions.get(appVersions.size() - 1).getShortDescription();
    }

    /**
     * A component that returns HTML text indicating the long description of the
     * app.
     *
     * @param appId The ID of the app whose long description is to be gotten as HTML
     *              text.
     * @return HTML template containing text indicating app long description
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @GetMapping("/long-description")
    @ResponseBody
    public String getAppLongDescriptionComponent(@PathVariable String appId) throws InterruptedException, ExecutionException {
        App app = queryGateway.query(new FindAppByIdQuery(appId), ResponseTypes.instanceOf(App.class)).get();
        List<AppVersion> appVersions = app.getAppVersions();

        return appVersions.get(appVersions.size() - 1).getLongDescription();
    }

    /**
     * A component that returns HTML text indicating the last modified date of the
     * app.
     *
     * @param appId The ID of the app whose last modified date is to be gotten as
     *              HTML text.
     * @return HTML template containing text indicating app last modified date
     * @apiNote Currently, as there is no last modified date stored, it returns the
     *          created date of the app.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @GetMapping("/last-modified-on")
    @ResponseBody
    public String getAppLastModifiedOnComponent(@PathVariable String appId) throws InterruptedException, ExecutionException {
        App app = queryGateway.query(new FindAppByIdQuery(appId), ResponseTypes.instanceOf(App.class)).get();
        List<AppVersion> appVersions = app.getAppVersions();

        // TODO: Return lastModifiedOn when implemented
        return appVersions.get(appVersions.size() - 1).getCreatedOn().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    /**
     * A component that returns HTML UI for the price-pill component
     *
     * @param appId The ID of the app whose price pill component is to be gotten.
     * @return HTML template containing styled price-pill component
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @GetMapping("/price-pill")
    public String getAppPricePill(@PathVariable String appId, Model model) throws InterruptedException, ExecutionException {
        App app = queryGateway.query(new FindAppByIdQuery(appId), ResponseTypes.instanceOf(App.class)).get();
        List<AppVersion> appVersions = app.getAppVersions();

        model.addAttribute("appPrice", appVersions.get(appVersions.size() - 1).getPrice().getValue());
        return "apps/app-price-pill";
    }

    /**
     * A component that returns image for the profile icon of the app.
     *
     * @param appId The ID of the app whose profile photo is to be gotten.
     * @return HTML template containing image of the app profile photo.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @GetMapping("/profile-image")
    public String getAppProfileImageComponent(@PathVariable String appId, Model model) {
        model.addAttribute("imageUrl", getImageUrl(appId));
        return "apps/app-profile-image";
    }

    private ApplicationDto convertToApplicationDto(App app) {
        int latestAppVersion = app.getAppVersions().size() - 1;
        AppVersion appVersion = app.getAppVersions().get(latestAppVersion);
        return new ApplicationDto(app.getId(), app.getName(), appVersion.getVersionLifeCycle(),
                appVersion.getVisibility(), appVersion.getCreatedOn(), getImageUrl(app.getId()));
    }

    private ApplicationVersionDto convertToApplicationVersionDto(AppVersion appVersion) {
        return new ApplicationVersionDto(
                appVersion.getVersionId(), appVersion.getPrice().getValue(), appVersion.getShortDescription(),
                appVersion.getLongDescription(), appVersion.getGalleryImages(), appVersion.getUpdateInformation(),
                appVersion.getVersionNumber(), appVersion.getVersionLifeCycle(), appVersion.getVisibility(),
                appVersion.getCreatedOn());
    }

    private String getImageUrl(String appId) {
        String key = APPS_PREFIX + appId;
        return new StringBuilder(endpointUrl).append("/").append(key).toString();
    }
}
