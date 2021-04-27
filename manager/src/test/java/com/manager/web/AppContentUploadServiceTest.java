package com.manager.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.coreapi.exception.InvalidImageFormatException;
import com.coreapi.exception.InvalidImageSizeException;
import com.manager.aws.AwsStorageService;
import com.manager.aws.FileResizeService;

@ExtendWith(SpringExtension.class)
@DisplayName("AppContentUploadService::  upload multipart files test case.")
@Tag("Service")
public class AppContentUploadServiceTest {
    @Value("classpath:/upload-apps-original/invalid-image.jpg")
    private Resource invalidImage;

    @Value("classpath:/upload-apps-test/test-image.png")
    private Resource resourceFile;

    @Value("classpath:/upload-apps-test/test_data.txt")
    private Resource textResourceFile;

    @Value("classpath:/upload-apps-original")
    private Resource sourceDirectory;

    @Value("classpath:/upload-apps-test")
    private Resource destinationDirectory;

    @SpyBean
    private FileResizeService fileResizeService;

    @MockBean
    private AwsStorageService awsStorageService;

    @Mock
    private MessageSource messageSource;

    private @InjectMocks AppContentUploadService instanceUnderTest;

    @BeforeEach
    public void setUp() throws IOException {
        ReflectionTestUtils.setField(instanceUnderTest, "awsStorageService", awsStorageService);
        ReflectionTestUtils.setField(instanceUnderTest, "fileResizeService", fileResizeService);
        File sourceFolder = new File(sourceDirectory.getFile().getPath());
        File destinationFolder = new File(destinationDirectory.getFile().getPath());
        FileSystemUtils.copyRecursively(sourceFolder, destinationFolder);
    }

    @Test
    @DisplayName("when upload binary file then expect successful result.")
    public void whenUploadBinaryFileThenExpectsuccessfulResult() throws IOException {
        MockMultipartFile file = new MockMultipartFile("app.txt", "test_data.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                textResourceFile.getInputStream());
        instanceUnderTest.uploadBinaryContent("versionId", file);
        assertEquals("app.txt", file.getName());
    }

    @Test
    @DisplayName("when file type invalid then throw exception.")
    public void whenFileTypeInvalid_ThenThrowException() throws IOException {
        File file = new File(textResourceFile.getFile().getPath());
        FileInputStream fis = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "multipart/form-data", fis);

        assertThrows(InvalidImageFormatException.class, () -> {
            instanceUnderTest.uploadProfileImage("appId", multipartFile, false);
        });
        assertEquals("test_data.txt", multipartFile.getOriginalFilename());
    }

    @Test
    @DisplayName("Upload a valid gallery image.")
    public void whenUploadGalleryImages_Then_ImageUploaded() throws IOException {
        File file = new File(resourceFile.getFile().getPath());
        FileInputStream fileInputStream = new FileInputStream(file);
        MockMultipartFile multipart = new MockMultipartFile(
                "file", file.getName(), "multipart/form-data",
                fileInputStream);
        instanceUnderTest.uploadProfileImage("appId", multipart, false);
        assertEquals("test-image.png", file.getName());
    }

    @Test
    @DisplayName("when upload invalid image file and resize is required then expect successful result.")
    public void whenUploadInvalidImageFileThenExpectInvalidException() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenThrow(IOException.class);
        assertThrows(InvalidImageFormatException.class, () -> {
            instanceUnderTest.uploadProfileImage("appId", multipartFile, true);
        });
    }

    @Test
    @DisplayName("when file too large then throw exception.")
    public void whenFileTooLarge_Then_ThrowException() throws IOException {
        File file = new File(invalidImage.getFile().getPath());
        FileInputStream fis = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "multipart/form-data", fis);

        MultipartFile[] files = { multipartFile };
        Integer[] indexToUpdate = { 0 };

        assertThrows(InvalidImageSizeException.class, () -> {
            instanceUnderTest.uploadGalleryImages("versionId", files, indexToUpdate);
        });
        assertEquals("invalid-image.jpg", multipartFile.getOriginalFilename());
    }
}