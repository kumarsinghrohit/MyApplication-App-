package com.coreapi.valueobject;

import org.apache.commons.lang3.StringUtils;

import lombok.Value;
import com.coreapi.exception.InvalidAppIdException;

@Value
public final class AppId {

    private String value;

    public AppId(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new InvalidAppIdException();
        }
        this.value = value;
    }
}
