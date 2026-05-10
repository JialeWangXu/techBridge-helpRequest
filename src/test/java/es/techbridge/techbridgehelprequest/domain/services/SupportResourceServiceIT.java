package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.application.port.out.resourceFacade.ResourceFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SupportResourceServiceIT {

    @MockitoBean
    ResourceFacade resourceFacade;

    @Autowired
    private SupportResourceService supportResourceService;

    @BeforeEach
    void setUp() {
        supportResourceService = new SupportResourceService(resourceFacade);
    }

    @Test
    void uploadResource_ShouldUploadAndReturnKey() throws IOException {
        // GIVEN
        String key = "test-file.pdf";
        MockMultipartFile file = new MockMultipartFile(
                "file", "original.pdf", "application/pdf", "contenido-binario".getBytes()
        );

        BDDMockito.given(this.resourceFacade.uploadResource(any(String.class),any(MultipartFile.class)))
                .willReturn(key);

        // WHEN
        String result = supportResourceService.uploadSupportSessionResource(key, file);
        assertEquals(key, result);
    }

    @Test
    void downLoadResource_ShouldReturnPresignedUrl() {
        // GIVEN
        String key = "recurso-persona-mayor.pdf";
        String expectedUrl = "https://s3.amazonaws.com/fake-url-signed";

        BDDMockito.given(this.resourceFacade.downLoadResource(any(String.class)))
                .willReturn(expectedUrl);

        // WHEN
        String result = supportResourceService.downLoadSupportSessionResource(key);

        // THEN
        assertEquals(expectedUrl, result);
    }

}
