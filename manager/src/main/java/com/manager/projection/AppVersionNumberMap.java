package com.manager.projection;

import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter(value = AccessLevel.PUBLIC)
public class AppVersionNumberMap {

    private Map<String, String> appVersionsNumberMap;

}
