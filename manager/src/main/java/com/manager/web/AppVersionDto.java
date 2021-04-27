package com.manager.web;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
class AppVersionDto {

    private String name;
    private String price;
    private String shortDescription;
    private String longDescription;
    private MultipartFile[] galleryImages;
    private int[] galleryImageIndexesToUpdate;
    private String whatsNewInVersion;
    private String version;
    private MultipartFile binary;
    private String developerId;
}
