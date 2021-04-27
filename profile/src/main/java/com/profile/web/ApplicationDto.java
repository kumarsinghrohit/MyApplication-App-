package com.profile.web;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.profile.query.App;

/**
 * The Data Transfer Object used for exchanging {@link App} instances with the
 * client side.
 */
@Getter
@AllArgsConstructor
class ApplicationDto {
    private String id;
    private String name;
    private String imageUrl;
    private String versionLifeCycle;
    private String visibility;
    private LocalDate createdOn;
}
