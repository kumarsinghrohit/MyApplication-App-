package com.profile.query;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The Axon query that the client sends when the {@link App}s in the read model
 * are to be requested by a specific ID.
 */
@Getter(value = AccessLevel.PUBLIC)
@AllArgsConstructor
public final class FindAppByIdQuery {

    private final String appId;

}
