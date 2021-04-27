package com.profile.config.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ElasticSearchConfig {

    // get the value of host from .yml file to build RestClient
    @Value("${elasticsearch.host}")
    private String host;

    // get the value of scheme from .yml file to build RestClient
    @Value("${elasticsearch.scheme}")
    private String scheme;

    // get the value of port from .yml file to build RestClient
    @Value("${elasticsearch.port}")
    private int port;

    @Bean
    public RestHighLevelClient restClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, scheme)));
    }
}
