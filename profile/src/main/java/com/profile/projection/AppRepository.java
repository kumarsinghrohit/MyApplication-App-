package com.profile.projection;

import java.io.IOException;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * App repository which fetches the {@link AppEntity} related information.
 */
@Repository
class AppRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppRepository.class);

    private static final int SIZE = 200;
    private static final String SORT_FIELD_KEYWORD = "name.keyword";
    private static final String DEVELOPER_ID = "developerId";
    private static final String TYPE = "type";
    private static final String NAME = "name";
    private static final String LIFE_CYCLE = "lifeCycle";

    private final RestHighLevelClient client;

    AppRepository(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * Fetches all apps that was developed by currently logged-in user.
     *
     * @param developerId
     * @param type
     * @return a {@link SearchResponse} object representing all apps present in the
     *         database related to currently logged in user.
     * @throws IOException
     */
    SearchResponse findByDeveloperIdAndType(String developerId, String type, String lifeCycle) throws IOException {
        LOGGER.info("fetching apps from app repository");
        SearchRequest searchRequest = new SearchRequest(ElasticSearchIndex.INDEX_APPS.getIndexName());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(DEVELOPER_ID + ".keyword", developerId))
                .filter(QueryBuilders.boolQuery().must(QueryBuilders.termQuery(TYPE + ".keyword", type))
                        .must(QueryBuilders.termQuery(LIFE_CYCLE + ".keyword", lifeCycle)));

        searchSourceBuilder.query(query);
        searchSourceBuilder.size(SIZE);
        searchSourceBuilder.sort(new FieldSortBuilder(SORT_FIELD_KEYWORD).order(SortOrder.ASC));
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    GetResponse findById(String appId) throws IOException {
        GetRequest getRequest = new GetRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        return client.get(getRequest, RequestOptions.DEFAULT);
    }

    GetResponse findNameById(String appId) throws IOException {
        String[] includes = new String[] { NAME };
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        GetRequest getRequest = new GetRequest(ElasticSearchIndex.INDEX_APPS.getIndexName(), appId);
        getRequest.fetchSourceContext(fetchSourceContext);
        return client.get(getRequest, RequestOptions.DEFAULT);
    }
}
