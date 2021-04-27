package com.manager.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.tika.Tika;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.coreapi.exception.InvalidImageFormatException;
import com.coreapi.exception.InvalidImageSizeException;
import com.coreapi.valueobject.AppVersionGalleryImage;
import com.manager.aws.AwsStorageService;
import com.manager.aws.FileResizeService;

@Service
class AppContentUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppContentUploadService.class);

    private static final String STORAGE_FOLDER_NAME = "resized-files";

    private static final int LARGE_IMAGE_HEIGHT = 350;
    private static final int LARGE_IMAGE_WIDTH = 300;
    private static final int SMALL_IMAGE_HEIGHT = 100;
    private static final int SMALL_IMAGE_WIDTH = 100;

    private static final String LARGE_IMAGE_FILE_PREFIX = "LARGE_";
    private static final String SMALL_IMAGE_FILE_PREFIX = "SMALL_";

    private static final String MIME_JPEG = "image/jpeg";
    private static final String MIME_PNG = "image/png";

    private final FileResizeService fileResizeService;
    private final AwsStorageService awsStorageService;
    private final MessageSource messageSource;
    private final Path rootLocation;

    public AppContentUploadService(FileResizeService fileResizeService, AwsStorageService storageService,
            MessageSource messageSource) {
        this.rootLocation = Paths.get(STORAGE_FOLDER_NAME);
        this.fileResizeService = fileResizeService;
        this.awsStorageService = storageService;
        this.messageSource = messageSource;
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(rootLocation);
    }

    void uploadBinaryContent(String versionId, MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String initialTemporaryPath = getTemporaryPathToTransferFile(fileName);
        File uploadedFile = processMultipartFile(file, initialTemporaryPath);
        storeContentToAws(AwsStorageService.APPS_VERSION_PREFIX + versionId, uploadedFile);
        FileSystemUtils.deleteRecursively(uploadedFile);
    }

    void uploadProfileImage(String appId, MultipartFile file, boolean isResizeRequired) throws IOException {
        validateImage(file);
        String fileName = getFileName(file);
        String initialTemporaryPath = getTemporaryPathToTransferFile(fileName);
        // Copy the file from the request to a temporary location for further processing
        File originalFileInTempLocation = processMultipartFile(file, initialTemporaryPath);
        File profileImageFile = null;
        if (isResizeRequired) {
            // Resize the file into two sizes [resulting files are in /resized-files]
            profileImageFile = resizeFile(originalFileInTempLocation, fileName, LARGE_IMAGE_HEIGHT, LARGE_IMAGE_WIDTH,
                    LARGE_IMAGE_FILE_PREFIX);
            storeContentToAws(AwsStorageService.APPS_PREFIX + appId, profileImageFile);
            FileSystemUtils.deleteRecursively(profileImageFile);
        } else {
            storeContentToAws(AwsStorageService.APPS_PREFIX + appId, originalFileInTempLocation);
        }
        FileSystemUtils.deleteRecursively(originalFileInTempLocation);
    }

    List<AppVersionGalleryImage> uploadGalleryImages(String versionId, MultipartFile[] multipartFiles, Integer[] indexesToUpdate)
            throws IOException {
        List<AppVersionGalleryImage> resultList = new LinkedList<>();
        if (Objects.nonNull(multipartFiles) && multipartFiles.length > 0) {
            for (int i = 0; i < multipartFiles.length; i++) {
                MultipartFile multipartFile = multipartFiles[i];
                validateImage(multipartFile);

                String fileName = getFileName(multipartFile);
                String initialTemporaryPath = getTemporaryPathToTransferFile(fileName);

                // Copy the file from the request to a temporary location for further processing
                File originalFileInTempLocation = processMultipartFile(multipartFile, initialTemporaryPath);

                // Resize the file into two sizes [resulting files are in /resized-files]
                File profileImageFile = resizeFile(originalFileInTempLocation, fileName, LARGE_IMAGE_HEIGHT, LARGE_IMAGE_WIDTH,
                        LARGE_IMAGE_FILE_PREFIX);
                File thumbnailImageFile = resizeFile(originalFileInTempLocation, fileName, SMALL_IMAGE_HEIGHT, SMALL_IMAGE_WIDTH,
                        SMALL_IMAGE_FILE_PREFIX);
                FileSystemUtils.deleteRecursively(originalFileInTempLocation);

                // Store the resized files in the persistent storage
                String thumbnailImageKey = AwsStorageService.APPS_GALLERY_PREFIX + SMALL_IMAGE_FILE_PREFIX + versionId
                        + indexesToUpdate[i];
                String profileImageKey = AwsStorageService.APPS_GALLERY_PREFIX + LARGE_IMAGE_FILE_PREFIX + versionId
                        + indexesToUpdate[i];

                storeContentToAws(thumbnailImageKey, thumbnailImageFile);
                storeContentToAws(profileImageKey, profileImageFile);

                resultList.add(new AppVersionGalleryImage(thumbnailImageKey, profileImageKey));

                // Delete the resized files as we have now stored them in the persistent storage
                FileSystemUtils.deleteRecursively(thumbnailImageFile);
                FileSystemUtils.deleteRecursively(profileImageFile);
            }
        }
        return resultList;
    }

    private void storeContentToAws(String key, File file) {
        awsStorageService.store(key, file);
    }

    private String getFileName(MultipartFile multipartFile) {
        return StringUtils.cleanPath(multipartFile.getOriginalFilename());
    }

    private void validateImage(MultipartFile file) {
        if (!isValidImageFormat(file)) {
            String message = messageSource.getMessage("invalid.image.format", null, LocaleContextHolder.getLocale());
            throw new InvalidImageFormatException(message);
        }

        if (file.getSize() > (5 * 1024 * 1024)) {
            String message = messageSource.getMessage("invalid.image.size", null, LocaleContextHolder.getLocale());
            throw new InvalidImageSizeException(message);
        }
    }

    private String getTemporaryPathToTransferFile(String fileName) {
        Path destinationPath = Paths.get(System.getProperty("java.io.tmpdir"));
        return destinationPath.resolve(fileName).toString();
    }

    private File processMultipartFile(MultipartFile file, String tempPath) throws IOException {
        File uploadedImageFile = new File(tempPath);
        file.transferTo(uploadedImageFile);
        return uploadedImageFile;
    }

    private File resizeFile(File file, String fileName, int height, int width, String fileNamePrefix) throws IOException {
        File resizedGalleryImageFile = fileResizeService.resizeImage(file, width, height);
        File destinationLocationFile = this.rootLocation.resolve(fileNamePrefix + fileName).toFile();
        FileOutputStream largeImageFileOutputStream = new FileOutputStream(destinationLocationFile);
        FileInputStream fileInputStream = new FileInputStream(resizedGalleryImageFile);
        IOUtils.copy(fileInputStream, largeImageFileOutputStream);
        fileInputStream.close();
        largeImageFileOutputStream.close();
        return destinationLocationFile;
    }

    private boolean isValidImageFormat(MultipartFile file) {
        String contentType = getContentType(file).orElse(null);
        return Objects.equals(MIME_JPEG, contentType)
                || Objects.equals(MIME_PNG, contentType);
    }

    private Optional<String> getContentType(MultipartFile file) {
        try {
            Tika tika = new Tika();
            return Optional.ofNullable(tika.detect(file.getInputStream()));
        } catch (IOException e) {
            LOGGER.error("Unable to probe content type for {}", file);
            LOGGER.error("Exception while detecting the content type: {}", e.getMessage());
            return Optional.empty();
        }
    }
}