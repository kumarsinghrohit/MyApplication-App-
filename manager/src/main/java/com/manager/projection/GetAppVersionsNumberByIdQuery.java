package com.manager.projection;

import lombok.AccessLevel;
import lombok.Getter;
import com.coreapi.valueobject.AppId;

/**
 * The Axon query that the client sends when all versions of a particular app in
 * the read model is requested for given {@link AppId}.
 */
@Getter(value = AccessLevel.PACKAGE)
public class GetAppVersionsNumberByIdQuery {
    private String appId;

    public GetAppVersionsNumberByIdQuery(String appId) {
        super();
        this.appId = appId;
    }

}
