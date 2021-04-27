package com.manager.web;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
class EditAppDto {

    private String name;
    private MultipartFile image;
    private String appType;
    private String developerId;
}
