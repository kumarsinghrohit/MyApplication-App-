package com.manager.web;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.coreapi.exception.InvalidAppLongDescriptionException;
import com.coreapi.exception.InvalidAppPriceException;
import com.coreapi.exception.InvalidAppShortDescriptionException;
import com.coreapi.exception.InvalidDeveloperIdException;
import com.coreapi.valueobject.AppId;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionId;
import com.coreapi.valueobject.AppVersionLongDescription;
import com.coreapi.valueobject.AppVersionNumber;
import com.coreapi.valueobject.AppVersionPrice;
import com.coreapi.valueobject.AppVersionShortDescription;
import com.coreapi.valueobject.AppVersionUpdateInfo;
import com.coreapi.valueobject.DeveloperId;
import com.manager.command.AddAppVersionCommand;
import com.manager.command.UpdateAppVersionCommand;
import com.manager.projection.AppVersionNumberMap;
import com.manager.projection.GetAppVersionsNumberByIdQuery;

/**
 * Controller responsible for serving requests related to management of an app's
 * versions.
 */
@Controller
@RequestMapping("/apps/{appId}/versions")
public class AppVersionManagerController {
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final MessageSource messageSource;
    private final AppContentUploadService appContentUploadService;

    AppVersionManagerController(CommandGateway commandGateway, QueryGateway queryGateway,
            AppContentUploadService appContentUploadService,
            MessageSource messageSource) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.messageSource = messageSource;
        this.appContentUploadService = appContentUploadService;
    }

    @PostMapping
    @ResponseBody
    public String addAppVersion(@PathVariable String appId, AppVersionDto appVersionDto)
            throws InterruptedException, ExecutionException, IOException {
        List<String> errorMessages = new ArrayList<>();
        DeveloperId developerId = null;
        AppVersionPrice appPrice = createAppPrice(appVersionDto.getPrice(), errorMessages);
        AppVersionShortDescription appShortDescription = createAppShortDescription(appVersionDto.getShortDescription(),
                errorMessages);
        AppVersionLongDescription appLongDescription = createAppLongDescription(appVersionDto.getLongDescription(),
                errorMessages);
        validateAppVersionWhileCreate(appId, appVersionDto.getVersion(), errorMessages, appVersionDto.getName());
        try {
            developerId = new DeveloperId(appVersionDto.getDeveloperId());
        } catch (InvalidDeveloperIdException e) {
            errorMessages.add(messageSource.getMessage("invalid.developer.id", null, LocaleContextHolder.getLocale()));
        }
        if (!errorMessages.isEmpty()) {
            throw new InvalidAppDataException(errorMessages);
        }

        String appVersionId = UUID.randomUUID().toString();
        appContentUploadService.uploadBinaryContent(appVersionId, appVersionDto.getBinary());
        List<AppVersionGalleryImage> galleryImages = appContentUploadService.uploadGalleryImages(appVersionId,
                appVersionDto.getGalleryImages(), getImageGalleryIndex(appVersionDto.getGalleryImages()));
        commandGateway.sendAndWait(new AddAppVersionCommand(
                new AppId(appId),
                new AppVersionId(appVersionId),
                appPrice,
                appShortDescription,
                appLongDescription,
                galleryImages,
                new AppVersionUpdateInfo(appVersionDto.getWhatsNewInVersion()),
                new AppVersionNumber(appVersionDto.getVersion()),
                LocalDate.now(),
                developerId));
        return messageSource.getMessage("create.app.version.success.message",
                new String[] { appVersionDto.getVersion(), appVersionDto.getName() },
                LocaleContextHolder.getLocale());
    }

    @PutMapping("/{versionId}")
    @ResponseBody
    public String updateAppVersion(@PathVariable String appId, @PathVariable String versionId,
            @ModelAttribute AppVersionDto appVersionDto) throws InterruptedException, ExecutionException, IOException {
        List<String> errorMessages = new ArrayList<>();
        AppVersionPrice appPrice = createAppPrice(appVersionDto.getPrice(), errorMessages);
        AppVersionShortDescription appShortDescription = createAppShortDescription(appVersionDto.getShortDescription(),
                errorMessages);
        AppVersionLongDescription appLongDescription = createAppLongDescription(appVersionDto.getLongDescription(),
                errorMessages);
        validateAppVersionWhileUpdate(appId, appVersionDto.getVersion(), errorMessages, appVersionDto.getName(), versionId);
        if (!errorMessages.isEmpty()) {
            throw new InvalidAppDataException(errorMessages);
        }
        if (Objects.nonNull(appVersionDto.getBinary()) && !appVersionDto.getBinary().isEmpty()) {
            appContentUploadService.uploadBinaryContent(versionId, appVersionDto.getBinary());
        }
        List<AppVersionGalleryImage> galleryImageUrls = new ArrayList<>();
        if (Objects.nonNull(appVersionDto.getGalleryImages())) {
            galleryImageUrls = appContentUploadService.uploadGalleryImages(versionId,
                    appVersionDto.getGalleryImages(),
                    Arrays.stream(appVersionDto.getGalleryImageIndexesToUpdate()).boxed().toArray(Integer[]::new));
        }
        Map<Integer, AppVersionGalleryImage> galleryImageUrlsUpdateMap = getGalleryImageUrlsUpdateMap(versionId,
                new ArrayList<>(galleryImageUrls), appVersionDto.getGalleryImageIndexesToUpdate());
        commandGateway.sendAndWait(new UpdateAppVersionCommand(new AppId(appId), new AppVersionId(versionId), appPrice,
                appShortDescription,
                appLongDescription,
                galleryImageUrls,
                new AppVersionUpdateInfo(appVersionDto.getWhatsNewInVersion()),
                new AppVersionNumber(appVersionDto.getVersion()), null,
                galleryImageUrlsUpdateMap));
        return messageSource.getMessage("update.app.version.success.message",
                new String[] { appVersionDto.getVersion(), appVersionDto.getName() },
                LocaleContextHolder.getLocale());
    }

    private void validateAppVersionWhileCreate(String appId, String appVersion, List<String> errorMessages, String appName)
            throws InterruptedException, ExecutionException {
        Map<String, String> appVersions = queryGateway
                .query(new GetAppVersionsNumberByIdQuery(appId), ResponseTypes.instanceOf(AppVersionNumberMap.class)).get()
                .getAppVersionsNumberMap();
        if (appVersions.keySet().stream().anyMatch(version -> version.equalsIgnoreCase(appVersion))) {
            errorMessages.add(messageSource.getMessage("app.version.already.exists",
                    new String[] { appName, appVersion },
                    LocaleContextHolder.getLocale()));
        }
    }

    private void validateAppVersionWhileUpdate(String appId, String appVersion, List<String> errorMessages, String appName,
            String versionId)
            throws InterruptedException, ExecutionException {
        Map<String, String> appVersions = queryGateway
                .query(new GetAppVersionsNumberByIdQuery(appId), ResponseTypes.instanceOf(AppVersionNumberMap.class)).get()
                .getAppVersionsNumberMap();
        if (appVersions.keySet().stream().anyMatch(version -> version.equalsIgnoreCase(appVersion))
                && !Objects.equals(appVersions.get(appVersion.toUpperCase()), versionId)) {
            errorMessages.add(messageSource.getMessage("app.version.already.exists",
                    new String[] { appName, appVersion },
                    LocaleContextHolder.getLocale()));
        }
    }

    private AppVersionLongDescription createAppLongDescription(String longDescription, List<String> errorMessages) {
        AppVersionLongDescription appLongDescription = null;
        try {
            appLongDescription = new AppVersionLongDescription(longDescription);
        } catch (InvalidAppLongDescriptionException e) {
            errorMessages.add(messageSource.getMessage("invalid.app.long.description", null, LocaleContextHolder.getLocale()));
        }
        return appLongDescription;
    }

    private AppVersionShortDescription createAppShortDescription(String shortDescription, List<String> errorMessages) {
        AppVersionShortDescription appShortDescription = null;

        try {
            appShortDescription = new AppVersionShortDescription(shortDescription);
        } catch (InvalidAppShortDescriptionException e) {
            errorMessages.add(messageSource.getMessage("invalid.app.short.description", null, LocaleContextHolder.getLocale()));
        }
        return appShortDescription;
    }

    private AppVersionPrice createAppPrice(String price, List<String> errorMessages) {
        AppVersionPrice appPrice = null;
        try {
            Double appPriceValue = 0.0;
            if (!StringUtils.isEmpty(price)) {
                appPriceValue = Double.parseDouble(price);
            }
            appPrice = new AppVersionPrice(Currency.getInstance("EUR"), appPriceValue);
        } catch (InvalidAppPriceException | NumberFormatException e) {
            errorMessages.add(messageSource.getMessage("invalid.app.price", null, LocaleContextHolder.getLocale()));
        }
        return appPrice;
    }

    private Map<Integer, AppVersionGalleryImage> getGalleryImageUrlsUpdateMap(String versionId,
            List<AppVersionGalleryImage> galleryImageUrls, int[] indexesToUpdate) throws IOException {
        if (galleryImageUrls.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, AppVersionGalleryImage> galleryImageUrlUpdateMap = new HashMap<>();
        Arrays.stream(indexesToUpdate).forEach(index -> galleryImageUrlUpdateMap.put(index, galleryImageUrls.remove(0)));

        return galleryImageUrlUpdateMap;
    }

    private Integer[] getImageGalleryIndex(MultipartFile[] galleryImages) {
        if (Objects.nonNull(galleryImages) && galleryImages.length > 0) {
            return IntStream.range(0, galleryImages.length).map(i -> i).boxed().toArray(Integer[]::new);
        }
        return null;
    }
}
