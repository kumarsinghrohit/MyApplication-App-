package com.profile.web.component;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * The Data Transfer Object used for exchanging {@code Product} instances with
 * the client side.
 */
@Getter
@AllArgsConstructor
class ApplicationDto {

    private String id;
    private String name;
    private String lifecycle;
    private String visibility;
    private LocalDate createdOn;
    private String imageUrl;
}
