package com.manager.projection;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * The Axon query that the client sends when all the {@code App}s in the read
 * model are to be requested.
 */
@Getter(value = AccessLevel.PUBLIC)
public final class FindAppByNameQuery {

    private final String name;

    public FindAppByNameQuery(String name) {
        this.name = name;
    }
}
