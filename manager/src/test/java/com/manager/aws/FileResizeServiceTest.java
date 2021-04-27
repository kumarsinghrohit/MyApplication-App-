package com.manager.aws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileSystemUtils;

import com.manager.aws.FileResizeService;

@ExtendWith(SpringExtension.class)
@DisplayName("Testing FileResizeService")
@Tag("Service")
public class FileResizeServiceTest {

    @Value("classpath:/upload-apps-original")
    private Resource sourceDirectory;

    @Value("classpath:/upload-apps-test")
    private Resource destinationDirectory;
    
    @Value("classpath:/upload-apps-test/test-image.png")
    private Resource resourceFile;

    private @InjectMocks FileResizeService instanceUnderTest;

    @BeforeEach
    private void setUp() throws IOException {
        File sourceFolder = new File(sourceDirectory.getFile().getPath());
        File destinationFolder = new File(destinationDirectory.getFile().getPath());
        FileSystemUtils.copyRecursively(sourceFolder, destinationFolder);
    }

    @Test
    @DisplayName("Testing FileResizeService with an image file")
    public void when_resizeImage_Then_Return_Resized_Image() throws Exception {
        File file = new File(resourceFile.getFile().getPath());
        File resizedFile = instanceUnderTest.resizeImage(file, 100, 100);

        assertThat(resizedFile.getName()).isEqualTo("test-image.png");
        assertTrue(resizedFile.isFile());
    }
}
