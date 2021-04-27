package com.manager.command;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import com.coreapi.event.AppDeleted;
import com.coreapi.event.AppVersionDeleted;
import com.coreapi.valueobject.AppId;
import com.coreapi.valueobject.AppVersion;
import com.coreapi.valueobject.AppVersionId;
import com.coreapi.valueobject.AppVersionLifeCycle;

/**
 * Saga type of Event Listener, responds on {@link AppDeleted} event and
 * dispatch {@link DeleteAppVersion} command.
 */
@Saga
public class AppVersionLifeCycleManagementSaga {

    @Autowired
    private CommandGateway commandGateway;

    /**
     * Event signifies the start of a transaction starting with @StartSaga and
     * dispatch {@link DeleteAppVersion}
     */
    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    public void on(AppDeleted event) {
        String id = event.getId().getValue();
        List<AppVersion> appVersions = event.getAppVersions();
        int numberOfVersion = appVersions.size();
        int index = 1;
        for (AppVersion appVersion : appVersions) {
            commandGateway.sendAndWait(new DeleteAppVersion(new AppId(id), new AppVersionId(appVersion.getVersionId()),
                    AppVersionLifeCycle.DISCONTINUED, index++, numberOfVersion));
        }
    }

    @SagaEventHandler(associationProperty = "id")
    public void on(AppVersionDeleted event) throws InterruptedException, ExecutionException {
        if (Objects.equals(event.getCurrentAppVersionIndex(), event.getTotalNumberOfAppVersions())) {
            SagaLifecycle.end();
        }
    }

}
