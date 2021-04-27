package com.manager.projection;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("enum")
@DisplayName("Testing ElasticSearchIndex enum")
public class ElasticSearchIndexTest {

    @Test
    @DisplayName("Testing all values")
    public void should() {
        assertThat(ElasticSearchIndex.INDEX_APPS.getIndexName()).isEqualTo("apps");
    }
}
