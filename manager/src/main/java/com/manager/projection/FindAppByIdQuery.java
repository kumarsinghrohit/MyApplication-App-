package com.manager.projection;

/**
 * The Axon query that the client sends when the {@link App}s in the read
 * model are to be requested by a specific ID.
 */
public final class FindAppByIdQuery {

    private final String appId;

    /**
     * @param appId the ID of the app to be retrieved
     */
    public FindAppByIdQuery(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }
}
