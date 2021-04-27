package com.manager.aws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@TestPropertySource(properties = {
        "cloud.aws.s3.bucket=test-bucket",
})
@Tag("Service")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing AwsStorageService")
public class AwsStorageServiceTest {

    private @InjectMocks AwsStorageService awsStorageService;

    @Mock
    private AmazonS3Client amazonS3Client;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(awsStorageService, "bucket", "bucket");
        ReflectionTestUtils.setField(awsStorageService, "endpointUrl", "endpointUrl");
    }

    @Test
    @DisplayName("Test AwsStorageService delete key Operation")
    public void when_Delete_Key_Then_Key_Deleted() {
        awsStorageService.delete("key");

        verify(amazonS3Client).deleteObject("bucket", "key");
    }

    @Test
    @DisplayName("Test AwsStorageService store key Operation")
    public void when_Store_Key_Then_Key_Deleted() {
        File file = mock(File.class);
        awsStorageService.store("key", file);

        verify(amazonS3Client).putObject(any());
    }

    @Test
    @DisplayName("Test AwsStorageService get Resource Url")
    public void when_getResourceUrl_Then_Return_ResourceUrl() {
        String resourceUrl = awsStorageService.getResourceUrl("key");
        assertThat(resourceUrl).contains("key");
    }

    @Test
    @DisplayName("Test AwsStorageService Bulk Delete Operation")
    public void when_bulkDelete_Then_All_Keys_Deleted() {
        S3ObjectSummary objectSummary = mock(S3ObjectSummary.class);
        ObjectListing objectListing = mock(ObjectListing.class);

        when(objectListing.getObjectSummaries()).thenReturn(Arrays.asList(objectSummary));
        when(objectSummary.getKey()).thenReturn("key1");
        when(amazonS3Client.listObjects(anyString(), anyString())).thenReturn(objectListing);

        awsStorageService.bulkDelete("users", "user");

        verify(amazonS3Client).deleteObjects(ArgumentMatchers.any());
    }
}
