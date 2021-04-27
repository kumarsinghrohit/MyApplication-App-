package com.manager.projection;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import com.coreapi.valueobject.AppLifeCycle;

/**
 * App repository stores the {@link AppEntity} related information.
 */
@Repository
class AppRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppRepository.class);

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String APP_VERSIONS = "appVersions";
    private static final String CREATED_ON = "createdOn";
    private static final String DEVELOPER_ID = "developerId";

    /********** App Version Content ************/
    private static final String VERSION_ID = "versionId";
    private static final String PRICE = "price";
    private static final String SHORT_DESCRIPTION = "shortDescription";
    private static final String LONG_DESCRIPTION = "longDescription";
    private static final String GALLERY_IMAGES = "galleryImages";
    private static final String UPDATE_INFORMATION = "updateInformation";
    private static final String VERSION_NUMBER = "versionNumber";
    private static final String VERSION_LIFE_CYCLE = "versionLifeCycle";
    private static final String LIFE_CYCLE = "lifeCycle";
    private static final String VISIBILITY = "visibility";
    private static final int SIZE = 200;
    private static final String SORT_FIELD_KEYWORD = "name.keyword";

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;

    AppRepository(RestHighLevelClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    void save(AppEntity appEntity) throws IOException {
        LOGGER.info("creating  app in app repository with id: {}", appEntity.getId());
        IndexRequest indexRequest = new IndexRequest(ElasticSearchIndex.INDEX_APPS.getIndexName()).id(appEntity.getId());
        indexRequest.source(getAppContent(appEntity));
        client.index(indexRequest, RequestOptions.DEFAULT);
    }

    void updateApp(String appId, String name) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        updateRequest.doc(NAME, name);
        client.update(updateRequest, RequestOptions.DEFAULT);
    }

    void addVersion(String id, AppVersionEntity appVersionEntity) throws IOException {
        LOGGER.info("adding a new version with id: {} to app with id: {}", appVersionEntity.getVersionId(), id);
        UpdateRequest updateRequest = new UpdateRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), id);
        Map<String, Object> parameters = Collections.singletonMap("appVersion",
                getAppVersionContent(appVersionEntity).map());
        Script inline = new Script(ScriptType.INLINE, "painless", "ctx._source.appVersions.add(params.appVersion)", parameters);
        updateRequest.script(inline);
        client.update(updateRequest, RequestOptions.DEFAULT);
    }

    void updateVersionGalleryImages(String appId, String versionId, List<AppVersionGalleryImageEntity> galleryImages)
            throws IOException {
        LOGGER.info("updating the version's galleryImages with id: {} from app with id: {}", appId, versionId);
        List<String> profileImageUrls = galleryImages.stream().map(AppVersionGalleryImageEntity::getProfileImageName)
                .collect(Collectors.toList());
        List<String> thumbnailInageUrls = galleryImages.stream().map(AppVersionGalleryImageEntity::getThumbnailImageName)
                .collect(Collectors.toList());
        updateGalleryImageUrl(appId, versionId, profileImageUrls, "profileImageName");
        updateGalleryImageUrl(appId, versionId, thumbnailInageUrls, "thumbnailImageName");

    }

    void updateVersionShortDescription(String appId, String versionId, String shortDescription) throws IOException {
        LOGGER.info("updating the version's shortdescription with id: {} from app with id: {}", appId, versionId);
        UpdateRequest updateRequest = new UpdateRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        Map<String, Object> params = ImmutableMap.of("versionId", versionId, "value", shortDescription);
        Script inline = new Script(ScriptType.INLINE, "painless", createScriptForUpdateVersion(SHORT_DESCRIPTION), params);
        updateRequest.script(inline);
        client.update(updateRequest, RequestOptions.DEFAULT);

    }

    void updateVersionLongDescription(String appId, String versionId, String longDescription) throws IOException {
        LOGGER.info("updating the version's shortdescription with id: {} from app with id: {}", appId, versionId);
        UpdateRequest updateRequest = new UpdateRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        Map<String, Object> params = ImmutableMap.of("versionId", versionId, "value", longDescription);
        Script inline = new Script(ScriptType.INLINE, "painless", createScriptForUpdateVersion(LONG_DESCRIPTION), params);
        updateRequest.script(inline);
        client.update(updateRequest, RequestOptions.DEFAULT);

    }

    void updateVersionPrice(String appId, String versionId, Double price) throws IOException {
        LOGGER.info("updating the version's shortdescription with id: {} from app with id: {}", appId, versionId);
        UpdateRequest updateRequest = new UpdateRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        Map<String, Object> params = ImmutableMap.of("versionId", versionId, "value", price);
        Script inline = new Script(ScriptType.INLINE, "painless", createScriptForUpdateVersion(PRICE + ".value"), params);
        updateRequest.script(inline);
        client.update(updateRequest, RequestOptions.DEFAULT);

    }

    void updateVersionUpdateInformation(String appId, String versionId, String updateInformation) throws IOException {
        LOGGER.info("updating the version's shortdescription with id: {} from app with id: {}", appId, versionId);
        UpdateRequest updateRequest = new UpdateRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        Map<String, Object> params = ImmutableMap.of("versionId", versionId, "value", updateInformation);
        Script inline = new Script(ScriptType.INLINE, "painless", createScriptForUpdateVersion(UPDATE_INFORMATION), params);
        updateRequest.script(inline);
        client.update(updateRequest, RequestOptions.DEFAULT);

    }

    void updateVersionNumber(String appId, String versionId, String versionNumber) throws IOException {
        LOGGER.info("updating the version's shortdescription with id: {} from app with id: {}", appId, versionId);
        UpdateRequest updateRequest = new UpdateRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        Map<String, Object> params = ImmutableMap.of("versionId", versionId, "value", versionNumber);
        Script inline = new Script(ScriptType.INLINE, "painless", createScriptForUpdateVersion(VERSION_NUMBER), params);
        updateRequest.script(inline);
        client.update(updateRequest, RequestOptions.DEFAULT);

    }

    void deleteAppVersion(String appId, String versionId, String versionLifeCycle) throws IOException {
        LOGGER.info("updating the version's lifeCycle with id: {} from app with id: {}", appId, versionId);
        UpdateRequest updateRequest = new UpdateRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        Map<String, Object> params = ImmutableMap.of("versionId", versionId, "value", versionLifeCycle);
        Script inline = new Script(ScriptType.INLINE, "painless", createScriptForUpdateVersion(VERSION_LIFE_CYCLE), params);
        updateRequest.script(inline);
        client.update(updateRequest, RequestOptions.DEFAULT);
    }

    void deleteApp(String appId, String lifeCycle) throws IOException {
        LOGGER.info("updating the app's lifeCycle with id: {} ", appId);
        UpdateRequest updateRequest = new UpdateRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        updateRequest.doc(LIFE_CYCLE, lifeCycle);
        client.update(updateRequest, RequestOptions.DEFAULT);
    }

    GetResponse findById(String appId) throws IOException {
        GetRequest getRequest = new GetRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        return client.get(getRequest, RequestOptions.DEFAULT);
    }

    SearchResponse findAppByName(String name) throws IOException {
        SearchRequest searchRequest = new SearchRequest(ElasticSearchIndex.INDEX_APPS.getIndexName());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(name, NAME));
        searchSourceBuilder.size(SIZE);
        searchSourceBuilder.sort(new FieldSortBuilder(SORT_FIELD_KEYWORD).order(SortOrder.ASC));
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    private void updateGalleryImageUrl(String appId, String versionId, List<String> galleryImage, String imageCategory)
            throws IOException {
        int length = galleryImage.size();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("galleryImage", galleryImage);
        parameters.put("emptyMap", new HashMap<>()); //To create a new index, so that new image urls will be stored.
        String script = "ctx._source." + APP_VERSIONS + ".stream()"
                + ".filter(appVersion -> Objects.equals(appVersion." + VERSION_ID + " , \"" + versionId + "\"))"
                + ".forEach( version ->{IntStream.range(0, " + length + ").forEach(index -> {"
                + "if(index < version." + GALLERY_IMAGES + ".length) {"
                + "version." + GALLERY_IMAGES + "[index]." + imageCategory + "=params.galleryImage.get(index);"
                + "}else { "
                + "version." + GALLERY_IMAGES + ".add(params.emptyMap); "
                + "version." + GALLERY_IMAGES + "[index]." + imageCategory + "=params.galleryImage.get(index);"
                + "}"
                + "});"
                + "return \"\";"
                + "});";
        UpdateRequest updateRequest = new UpdateRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        Script inline = new Script(ScriptType.INLINE, "painless", script, parameters);
        updateRequest.script(inline);
        client.update(updateRequest, RequestOptions.DEFAULT);
    }

    private String createScriptForUpdateVersion(String field) {
        return "ctx._source." + APP_VERSIONS + ".stream()"
                + ".filter(appVersion -> Objects.equals(appVersion." + VERSION_ID + " , params.versionId))"
                + ".forEach( version ->{ version." + field + "= params.value;});";
    }

    private XContentBuilder getAppContent(AppEntity appEntity) throws IOException {
        return jsonBuilder().startObject()
                .field(ID, appEntity.getId())
                .field(NAME, appEntity.getName())
                .field(TYPE, appEntity.getType())
                .startArray(APP_VERSIONS)
                .copyCurrentStructure(getAppVersionContent(appEntity.getAppVersions().get(0)))
                .endArray()
                .timeField(CREATED_ON, appEntity.getCreatedOn())
                .field(DEVELOPER_ID, appEntity.getDeveloperId())
                .field(LIFE_CYCLE, appEntity.getLifeCycle())
                .endObject();
    }

    @SuppressWarnings("unchecked")
    private XContentParser getAppVersionContent(AppVersionEntity appVersionEntity) throws IOException {
        XContentBuilder contentBuilder = jsonBuilder().startObject();
        contentBuilder.field(VERSION_ID, appVersionEntity.getVersionId());
        contentBuilder.field(PRICE, objectMapper.convertValue(appVersionEntity.getPrice(), Map.class));
        contentBuilder.field(SHORT_DESCRIPTION, appVersionEntity.getShortDescription());
        contentBuilder.field(LONG_DESCRIPTION, appVersionEntity.getLongDescription());
//                .array(GALLERY_IMAGES, getAppVersionGalleryImageContent(appVersionEntity.getGalleryImages()))
        contentBuilder.startArray(GALLERY_IMAGES);
        appVersionEntity.getGalleryImages().forEach(galleryImageEntity -> {
            try {
                contentBuilder.startObject();
                contentBuilder.field("thumbnailImageName", galleryImageEntity.getThumbnailImageName());
                contentBuilder.field("profileImageName", galleryImageEntity.getProfileImageName());
                contentBuilder.endObject();
            } catch (IOException e) {
                LOGGER.error("Error while mapping the app version gallery content");
            }
        });
        contentBuilder.endArray();
        contentBuilder.field(UPDATE_INFORMATION, appVersionEntity.getUpdateInformation());
        contentBuilder.field(VERSION_NUMBER, appVersionEntity.getVersionNumber());
        contentBuilder.field(VERSION_LIFE_CYCLE, appVersionEntity.getVersionLifeCycle());
        contentBuilder.field(VISIBILITY, appVersionEntity.getVisibility());
        contentBuilder.timeField(CREATED_ON, appVersionEntity.getCreatedOn());
        contentBuilder.endObject();
        return XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY,
                DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                Strings.toString(contentBuilder));
    }
}
