package com.manager.projection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AppDaoService {

    private final AppRepository appRepository;
    private final ObjectMapper objectMapper;

    AppDaoService(final AppRepository appRepository, ObjectMapper objectMapper) {
        this.appRepository = appRepository;
        this.objectMapper = objectMapper;
    }

    void createApp(AppEntity appEntity) throws IOException {
        appRepository.save(appEntity);
    }

    void updateApp(String appId, String name) throws IOException {
        appRepository.updateApp(appId, name);
    }

    void addVersion(String id, AppVersionEntity appVersionEntity) throws IOException {
        appRepository.addVersion(id, appVersionEntity);
    }

    List<AppEntity> findAppByName(String name) throws IOException {
        return getApps(appRepository.findAppByName(name));
    }

    AppVersionNumberMap findversionNumbersById(String appId) throws IOException {
        return getVersions(appRepository.findById(appId));
    }

    AppEntity findById(String appId) throws IOException {
        return getAppEntity(appRepository.findById(appId));
    }

    private AppEntity getAppEntity(GetResponse getResponse) {
        AppEntity appEntity = objectMapper.convertValue(getResponse.getSourceAsMap(), AppEntity.class);
        return appEntity;
    }

    private AppVersionNumberMap getVersions(GetResponse getResponse) {
        AppEntity appEntity = objectMapper.convertValue(getResponse.getSourceAsMap(), AppEntity.class);
        Map<String, String> appVersionsNumberMap = new HashMap<>();
        appEntity.getAppVersions().forEach(appVersionEntity -> {
            appVersionsNumberMap.put(appVersionEntity.getVersionNumber().toUpperCase(), appVersionEntity.getVersionId());
        });
        return new AppVersionNumberMap(appVersionsNumberMap);
    }

    private List<AppEntity> getApps(SearchResponse searchResponse) {
        List<AppEntity> apps = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()) {
            AppEntity appEntity = objectMapper.convertValue(hit.getSourceAsMap(), AppEntity.class);
            apps.add(appEntity);
        }
        return apps;
    }

    void updateVersionShortDescription(String appId, String versionId, String shortDescription) throws IOException {
        appRepository.updateVersionShortDescription(appId, versionId, shortDescription);
    }

    void updateVersionLongDescription(String appId, String versionId, String longDescription) throws IOException {
        appRepository.updateVersionLongDescription(appId, versionId, longDescription);
    }

    void updateVersionPrice(String appId, String versionId, Double price) throws IOException {
        appRepository.updateVersionPrice(appId, versionId, price);
    }

    void updateVersionGalleryImages(String appId, String versionId, List<AppVersionGalleryImageEntity> galleryImages)
            throws IOException {
        appRepository.updateVersionGalleryImages(appId, versionId, galleryImages);
    }

    void updateVersionUpdateInformation(String appId, String versionId, String updateInformation) throws IOException {
        appRepository.updateVersionUpdateInformation(appId, versionId, updateInformation);
    }

    void updateVersionNumber(String appId, String versionId, String versionNumber) throws IOException {
        appRepository.updateVersionNumber(appId, versionId, versionNumber);
    }

    void deleteAppVersion(String appId, String versionId, String versionLifeCycle) throws IOException {
        appRepository.deleteAppVersion(appId, versionId, versionLifeCycle);
    }

    void deleteApp(String appId, String lifeCycle) throws IOException {
        appRepository.deleteApp(appId, lifeCycle);
    }
}
