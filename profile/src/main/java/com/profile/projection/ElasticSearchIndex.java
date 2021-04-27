package com.profile.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Elastic Search Index enum holds indices for app.
 *
 */
@AllArgsConstructor
@Getter
enum ElasticSearchIndex {

    INDEX_APPS("apps");

    private String indexName;
}
