package com.manager.command;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coreapi.event.AppCreated;
import com.coreapi.event.AppDeleted;
import com.coreapi.event.AppUpdated;
import com.coreapi.event.AppVersionAdded;
import com.coreapi.valueobject.AppId;
import com.coreapi.valueobject.AppLifeCycle;
import com.coreapi.valueobject.AppName;
import com.coreapi.valueobject.AppType;
import com.coreapi.valueobject.AppVersionLifeCycle;
import com.coreapi.valueobject.AppVersionVisibility;
import com.coreapi.valueobject.DeveloperId;

@Aggregate
class AppAggregate {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppAggregate.class);

    @AggregateIdentifier
    private AppId id;
    private AppName name;
    private AppType type;
    private DeveloperId developerId;
    @AggregateMember
    private List<AppVersion> appVersions = new ArrayList<>();
    private LocalDate createdOn;
    private LocalDate lastModifiedOn;
    private AppLifeCycle lifeCycle;

    private AppAggregate() {

    }

    @CommandHandler
    private AppAggregate(CreateApp createAppCommand) {
        LOGGER.info("Create App command occured with app name: {}", createAppCommand.getName().getValue());
        apply(new AppCreated(createAppCommand.getId(),
                createAppCommand.getName(),
                createAppCommand.getType(),
                createAppCommand.getVersionId(),
                createAppCommand.getPrice(),
                createAppCommand.getShortDescription(),
                createAppCommand.getLongDescription(),
                createAppCommand.getGalleryImages(),
                createAppCommand.getVersionNumber(),
                AppVersionLifeCycle.ACTIVE,
                AppVersionVisibility.PUBLIC,
                createAppCommand.getDeveloperId(),
                AppLifeCycle.ACTIVE,
                createAppCommand.getCreatedOn()));
    }

    @EventSourcingHandler
    private void on(AppCreated appCreated) {
        LOGGER.info("App created event occured with app name: {}", appCreated.getName().getValue());
        this.id = appCreated.getId();
        this.name = appCreated.getName();
        this.type = appCreated.getType();
        this.appVersions.add(new AppVersion(appCreated.getVersionId(), appCreated.getPrice(),
                appCreated.getShortDescription(), appCreated.getLongDescription(), appCreated.getGalleryImages(),
                null, appCreated.getVersionNumber(), appCreated.getVersionLifeCycle(),
                appCreated.getVisibility(), appCreated.getCreatedOn()));
        this.lifeCycle = appCreated.getLifeCycle();
        this.createdOn = appCreated.getCreatedOn();
    }

    @CommandHandler
    private void handle(UpdateApp updateAppCommand) {
        AppId editableAppId = updateAppCommand.getId();
        if (!Objects.equals(this.name, updateAppCommand.getName())) {
            apply(new AppUpdated(editableAppId, updateAppCommand.getName()));
        }
    }

    @CommandHandler
    private void handle(AddAppVersion addAppVersionCommand) {
        LOGGER.info("add App version command occured for app id: {}", addAppVersionCommand.getId().getValue());
        apply(new AppVersionAdded(addAppVersionCommand.getId(), addAppVersionCommand.getVersionId(),
                addAppVersionCommand.getPrice(),
                addAppVersionCommand.getShortDescription(),
                addAppVersionCommand.getLongDescription(),
                addAppVersionCommand.getGalleryImages(),
                addAppVersionCommand.getUpdateInformation(),
                addAppVersionCommand.getVersionNumber(),
                AppVersionLifeCycle.ACTIVE,
                AppVersionVisibility.PUBLIC,
                addAppVersionCommand.getCreatedOn()));
    }

    @EventSourcingHandler
    private void on(AppVersionAdded appVersionAdded) {
        this.appVersions.add(new AppVersion(appVersionAdded.getVersionId(), appVersionAdded.getPrice(),
                appVersionAdded.getShortDescription(), appVersionAdded.getLongDescription(),
                appVersionAdded.getGalleryImages(),
                appVersionAdded.getUpdateInformation(), appVersionAdded.getVersionNumber(),
                appVersionAdded.getVersionLifeCycle(),
                appVersionAdded.getVisibility(), appVersionAdded.getCreatedOn()));
        this.lastModifiedOn = appVersionAdded.getCreatedOn();
    }

    @EventSourcingHandler
    private void on(AppUpdated appUpdated) {
        this.name = appUpdated.getName();
    }

    @CommandHandler
    private void handle(DeleteApp deleteApp) {
        apply(new AppDeleted(deleteApp.getId(), this.type, deleteApp.getLifeCycle(), deleteApp.getAppVersions()));
    }

    @EventSourcingHandler
    private void on(AppDeleted appDeleted) {
        this.lifeCycle = appDeleted.getLifeCycle();
    }
}
