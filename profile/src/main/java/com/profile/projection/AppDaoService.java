package com.profile.projection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.coreapi.valueobject.AppId;
import com.profile.query.App;

/**
 * This is interacting service layer between Projection and Repository, through
 * which read operations related to {@link App} are performed.
 */
@Service
public class AppDaoService {

    private final AppRepository appRepository;
    private final ObjectMapper objectMapper;

    AppDaoService(AppRepository appRepository, ObjectMapper objectMapper) {
        this.appRepository = appRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches all apps that was developed by the currently logged-in user.
     *
     * @param developerId
     * @param type
     * @return a list of {@link AppEntity}s representing all apps present in the
     *         database related to currently logged in user.
     * @throws IOException
     */
    List<AppEntity> findByDeveloperIdAndType(String developerId, String type, String lifeCycle) throws IOException {
        return getApps(appRepository.findByDeveloperIdAndType(developerId, type, lifeCycle));
    }

    AppEntity findById(String appId) throws IOException {
        return getAppEntity(appRepository.findById(appId));
    }

    /**
     * Fetches {@link AppNameEntity} for the given {@link AppId}.
     *
     * @param id id of the App
     * @return .{@link AppNameEntity}
     * @throws IOException
     */
    AppNameEntity findNameById(String id) throws IOException {
        return getAppNameEntity(appRepository.findNameById(id));
    }

    private AppNameEntity getAppNameEntity(GetResponse getResponse) {
        return objectMapper.convertValue(getResponse.getSourceAsMap(), AppNameEntity.class);
    }

    private AppEntity getAppEntity(GetResponse getResponse) {
        return objectMapper.convertValue(getResponse.getSourceAsMap(), AppEntity.class);
    }

    private List<AppEntity> getApps(SearchResponse searchResponse) {
        List<AppEntity> appEntities = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()) {
            AppEntity appEntity = objectMapper.convertValue(hit.getSourceAsMap(), AppEntity.class);
            appEntities.add(appEntity);
        }
        return appEntities;
    }
}
