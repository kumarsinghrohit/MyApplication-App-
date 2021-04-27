package com.manager.projection;

import java.time.LocalDate;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.coreapi.valueobject.AppVersion;

@Getter(value = AccessLevel.PUBLIC)
@AllArgsConstructor
public class App {

    private String id;
    private String name;
    private String type;
    private List<AppVersion> appVersions;
    private String developerId;
    private String lifeCycle;
    private LocalDate createdOn;
}
