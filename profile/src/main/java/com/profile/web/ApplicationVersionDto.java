package com.profile.web;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.coreapi.valueobject.AppVersionGalleryImage;

@AllArgsConstructor
@Getter
@Setter
public class ApplicationVersionDto {
    private String id;
    private Double price;
    private String shortDescription;
    private String longDescription;
    private List<AppVersionGalleryImage> galleryImages;
    private String updateInformation;
    private String versionNumber;
    private String versionLifeCycle;
    private String visibility;
    private LocalDate createdOn;
}
