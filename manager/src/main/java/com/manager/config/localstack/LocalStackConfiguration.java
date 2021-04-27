package com.manager.config.localstack;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.smartup.localstack.EnableLocalStack;

@EnableLocalStack
@Configuration
@Profile("dev")
public class LocalStackConfiguration {

}
