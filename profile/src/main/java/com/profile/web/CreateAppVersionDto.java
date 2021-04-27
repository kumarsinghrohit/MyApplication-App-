package com.profile.web;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class CreateAppVersionDto {
    private String id;
    private String appType;
    private String imageUrl;
    private String name;
    private LocalDate createdOn;
}
