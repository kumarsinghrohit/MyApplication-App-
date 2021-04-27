package com.manager.web;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.coreapi.exception.AppNameAlreadyExistsException;
import com.coreapi.exception.InvalidAppLongDescriptionException;
import com.coreapi.exception.InvalidAppNameException;
import com.coreapi.exception.InvalidAppPriceException;
import com.coreapi.exception.InvalidAppShortDescriptionException;
import com.coreapi.exception.InvalidDeveloperIdException;
import com.coreapi.valueobject.AppId;
import com.coreapi.valueobject.AppLifeCycle;
import com.coreapi.valueobject.AppName;
import com.coreapi.valueobject.AppType;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.coreapi.valueobject.AppVersionId;
import com.coreapi.valueobject.AppVersionLongDescription;
import com.coreapi.valueobject.AppVersionNumber;
import com.coreapi.valueobject.AppVersionPrice;
import com.coreapi.valueobject.AppVersionShortDescription;
import com.coreapi.valueobject.DeveloperId;
import com.manager.command.CreateAppCommand;
import com.manager.command.DeleteAppCommand;
import com.manager.command.UpdateAppCommand;
import com.manager.projection.App;
import com.manager.projection.FindAppByIdQuery;
import com.manager.projection.FindAppByNameQuery;

@Controller
class AppManagerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppManagerController.class);
    private static final String EMPTY_STRING = "";
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final MessageSource messageSource;
    private final AppContentUploadService appContentUploadService;

    AppManagerController(CommandGateway commandGateway, QueryGateway queryGateway, MessageSource messageSource,
            AppContentUploadService appContentUploadService) {
        this.commandGateway = commandGateway;
        this.messageSource = messageSource;
        this.appContentUploadService = appContentUploadService;
        this.queryGateway = queryGateway;
    }

    @PostMapping("/apps")
    @ResponseBody
    public String createApp(CreateAppDto createAppDto)
            throws IOException, InterruptedException, ExecutionException {
        List<String> errorMessages = new ArrayList<>();
        AppName appName = null;
        AppType appType = null;
        AppVersionPrice appPrice = null;
        AppVersionShortDescription appShortDescription = null;
        AppVersionLongDescription appLongDescription = null;
        DeveloperId developerId = null;

        appType = AppType.get(createAppDto.getAppType());
        if (Objects.isNull(appType)) {
            errorMessages.add(messageSource.getMessage("invalid.app.type", null, LocaleContextHolder.getLocale()));
        }
        try {
            appName = new AppName(createAppDto.getName());
            boolean isAppNameUnique = isAppNameUnique(appName.getValue());
            if (isAppNameUnique) {
                throw new AppNameAlreadyExistsException();
            }
        } catch (InvalidAppNameException e) {
            errorMessages.add(messageSource.getMessage("invalid.app.name", null, LocaleContextHolder.getLocale()));
        } catch (AppNameAlreadyExistsException e) {
            errorMessages.add(messageSource.getMessage("app.name.already.exists", new String[] { createAppDto.getName() },
                    LocaleContextHolder.getLocale()));
        }

        try {
            Double appPriceValue = 0.0;
            if (!StringUtils.isEmpty(createAppDto.getPrice())) {
                appPriceValue = Double.parseDouble(createAppDto.getPrice());
            }
            appPrice = new AppVersionPrice(Currency.getInstance("EUR"), appPriceValue);
        } catch (InvalidAppPriceException | NumberFormatException e) {
            errorMessages.add(messageSource.getMessage("invalid.app.price", null, LocaleContextHolder.getLocale()));
        }
        try {
            appShortDescription = new AppVersionShortDescription(createAppDto.getShortDescription());
        } catch (InvalidAppShortDescriptionException e) {
            errorMessages.add(messageSource.getMessage("invalid.app.short.description", null, LocaleContextHolder.getLocale()));
        }
        try {
            appLongDescription = new AppVersionLongDescription(createAppDto.getLongDescription());
        } catch (InvalidAppLongDescriptionException e) {
            errorMessages.add(messageSource.getMessage("invalid.app.long.description", null, LocaleContextHolder.getLocale()));
        }
        try {
            developerId = new DeveloperId(createAppDto.getDeveloperId());
        } catch (InvalidDeveloperIdException e) {
            errorMessages.add(messageSource.getMessage("invalid.developer.id", null, LocaleContextHolder.getLocale()));
        }
        if (!errorMessages.isEmpty()) {
            throw new InvalidAppDataException(errorMessages);
        }
        String appId = UUID.randomUUID().toString();
        String appVersionId = UUID.randomUUID().toString();
        appContentUploadService.uploadProfileImage(appId, createAppDto.getFile(), false);
        appContentUploadService.uploadBinaryContent(appVersionId, createAppDto.getBinary());
        List<AppVersionGalleryImage> galleryImages = appContentUploadService.uploadGalleryImages(appVersionId,
                createAppDto.getGalleryImages(), getImageGalleryIndex(createAppDto.getGalleryImages()));
        commandGateway.sendAndWait(new CreateAppCommand(
                new AppId(appId),
                appName,
                appType,
                new AppVersionId(appVersionId),
                appPrice,
                appShortDescription,
                appLongDescription,
                galleryImages,
                new AppVersionNumber(createAppDto.getVersion()),
                developerId,
                LocalDate.now()));
        return messageSource.getMessage("create.app.success.message",
                new String[] { Optional.ofNullable(appType).orElse(AppType.APP).getValue().toLowerCase(),
                        appName.getValue() },
                LocaleContextHolder.getLocale());
    }

    /**
     * Update the {@link App} by a specific ID and returns the successful message.
     *
     * @param appId      : the ID of the app which has to be edited.
     * @param editAppDto : dto holds edit app attribute details.
     * @return String representing the message of successful execution.
     * @throws IOException          : throws in case of thread is interrupted.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @PutMapping("/apps/{appId}")
    @ResponseBody
    public String updateApp(@PathVariable String appId, EditAppDto editAppDto)
            throws IOException, InterruptedException, ExecutionException {
        List<String> validationMessages = new ArrayList<>();
        AppName appName = null;

        try {
            appName = new AppName(editAppDto.getName());
            boolean isAppNameUnique = isAppNameUnique(appName.getValue());
            if (isAppNameUnique && !Objects.equals(appId, getAppIdWithName(appName.getValue()))) {
                throw new AppNameAlreadyExistsException();
            }
        } catch (InvalidAppNameException e) {
            validationMessages
                    .add(messageSource.getMessage("invalid.app.name", null, LocaleContextHolder.getLocale()));
        } catch (AppNameAlreadyExistsException e) {
            validationMessages.add(messageSource.getMessage("app.name.already.exists", new String[] { editAppDto.getName() },
                    LocaleContextHolder.getLocale()));
        }

        if (!validationMessages.isEmpty()) {
            throw new InvalidAppDataException(validationMessages);
        }

        MultipartFile file = editAppDto.getImage();
        if (Objects.nonNull(file) && file.getSize() > 0) {
            appContentUploadService.uploadProfileImage(appId, file, false);
        }

        commandGateway.sendAndWait(new UpdateAppCommand(new AppId(appId), appName));
        LOGGER.info("Updated app details id: {}", appId);
        return messageSource.getMessage("update.app.success.message",
                new String[] { editAppDto.getAppType().toLowerCase(), appName.getValue() }, LocaleContextHolder.getLocale());
    }

    /**
     * End point responsible to serve requests that update app and appversion's
     * active lifeCycle to discontinue.
     *
     * @param appId     The ID of the {@link App} to delete
     * @param principal
     * @return String status of the result of the operation
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @ResponseBody
    @DeleteMapping("/apps/{appId}")
    public String deleteApp(@PathVariable(name = "appId") String appId)
            throws InterruptedException, ExecutionException {
        App app = queryGateway
                .query(new FindAppByIdQuery(appId), ResponseTypes.instanceOf(App.class)).get();

        commandGateway.sendAndWait(new DeleteAppCommand(new AppId(appId), AppLifeCycle.DISCONTINUED, app.getAppVersions()));
        LOGGER.info("Deleted app details with id: {}", appId);
        return messageSource.getMessage("delete.app.success.message", new String[] { app.getName(), app.getType().toLowerCase() },
                LocaleContextHolder.getLocale());
    }

    private Integer[] getImageGalleryIndex(MultipartFile[] galleryImages) {
        if (Objects.nonNull(galleryImages) && galleryImages.length > 0) {
            return IntStream.range(0, galleryImages.length).map(i -> i).boxed().toArray(Integer[]::new);
        }
        return null;
    }

    private String getAppIdWithName(String inputAppName)
            throws InterruptedException, ExecutionException {
        return getExistingAppsByName(inputAppName).stream()
                .filter(app -> inputAppName.equalsIgnoreCase(app.getName()))
                .map(App::getId)
                .collect(Collectors.toList()).get(0);
    }

    private boolean isAppNameUnique(String inputAppName)
            throws InterruptedException, ExecutionException {
        return getExistingAppsByName(inputAppName).stream()
                .map(App::getName).anyMatch(inputAppName::equalsIgnoreCase);
    }

    private List<App> getExistingAppsByName(String name)
            throws InterruptedException, ExecutionException {
        List<App> apps = queryGateway.query(new FindAppByNameQuery(name), ResponseTypes.multipleInstancesOf(App.class))
                .get();
        List<App> appList = apps.stream().filter(app -> app.getLifeCycle().equalsIgnoreCase(AppLifeCycle.ACTIVE.getValue()))
                .collect(Collectors.toList());
        return appList;
    }
}
