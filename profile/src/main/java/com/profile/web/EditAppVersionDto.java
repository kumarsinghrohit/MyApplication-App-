package com.profile.web;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class EditAppVersionDto {
    private String appId;
    private String versionId;
    private String imageUrl;
    private String name;
    private LocalDate createdOn;
    private Double price;
    private String shortDescription;
    private String longDescription;
    private List<String> galleryImageUrls;
    private String whatsNewInVersion;
    private String versionNumber;
    private String binary;
}
