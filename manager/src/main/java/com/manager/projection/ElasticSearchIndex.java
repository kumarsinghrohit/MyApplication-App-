package com.manager.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Elastic Search Index enum holds indices for application.
 *
 */
@AllArgsConstructor
@Getter
enum ElasticSearchIndex {

    INDEX_APPS("apps");

    private String indexName;
}
