package com.coreapi.valueobject;

import java.time.LocalDate;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(value = AccessLevel.PUBLIC)
public class AppVersion implements Comparable<AppVersion> {

    private String versionId;
    private AppVersionPrice price;
    private String shortDescription;
    private String longDescription;
    private List<AppVersionGalleryImage> galleryImages;
    private String updateInformation;
    private String versionNumber;
    private String versionLifeCycle;
    private String visibility;
    private LocalDate createdOn;

    public AppVersion(String versionId, AppVersionPrice price, String shortDescription, String longDescription,
            List<AppVersionGalleryImage> galleryImages, String updateInformation, String versionNumber, String versionLifeCycle,
            String visibility, LocalDate createdOn) {
        super();
        this.versionId = versionId;
        this.price = price;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.galleryImages = galleryImages;
        this.updateInformation = updateInformation;
        this.versionNumber = versionNumber;
        this.versionLifeCycle = versionLifeCycle;
        this.visibility = visibility;
        this.createdOn = createdOn;
    }

    @Override
    public int compareTo(AppVersion appVersion) {
        return this.getVersionNumber().compareTo(appVersion.getVersionNumber());
    }
}
