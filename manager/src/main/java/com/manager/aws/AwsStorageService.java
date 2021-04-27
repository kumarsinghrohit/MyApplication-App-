package com.manager.aws;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * The {@code AwsStorageService} is the file storage service to store the
 * provided files into the AWS s3.
 */
@Service
public class AwsStorageService {

    public static final String APPS_PREFIX = "apps/";
    public static final String APPS_VERSION_PREFIX = APPS_PREFIX + "versions/";
    public static final String APPS_GALLERY_PREFIX = APPS_VERSION_PREFIX + "gallery/";

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsStorageService.class);

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.endpoint.url}")
    private String endpointUrl;

    public AwsStorageService(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    /**
     * Stores the list of files into the AWS S3 under the provided path name as
     * prefix.
     *
     * @param key  : key along with the prefix which provides the path where the
     *             files will be stored beneath it. E.g. products prefix will ensure
     *             all the provided files will be stored with /product/<file>
     *             format.
     * @param file : file which needs to be stored.
     */
    public void store(String key, File file) {
        LOGGER.info("Inside AWS storage service to store file with key: {}.", key);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
        putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
        amazonS3Client.putObject(putObjectRequest);
    }

    /**
     * deletes the file into the AWS S3 with the provided key.
     *
     * @param key : key of the file which needs to be deleted.
     */
    public void delete(String key) {
        LOGGER.info("Inside AWS storage service to delete file with key: {}", key);
        amazonS3Client.deleteObject(bucket, key);
    }

    /**
     * Deletes all the file with the given prefixes, prefix is nothing but the name
     * of the folder/ folders separated by the separator.
     *
     * @param prefix   : name of the prefix.
     * @param prefixes : array of the prefixes from which files need to be deleted.
     */
    public void bulkDelete(String prefix, String... prefixes) {
        LOGGER.info("Inside AWS storage service to delete all files except the users");
        List<String> keys = new ArrayList<>();
        keys.addAll(getKeys(prefix));
        for (String p : prefixes) {
            keys.addAll(getKeys(p));
        }
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket)
                .withKeys(keys.stream().toArray(String[]::new));
        amazonS3Client.deleteObjects(deleteObjectsRequest);
    }

    public String getResourceUrl(String key) {
        return new StringBuilder(endpointUrl).append("/").append(key).toString();
    }

    private List<String> getKeys(String prefix) {
        return amazonS3Client.listObjects(bucket, prefix).getObjectSummaries().stream().map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }
}
