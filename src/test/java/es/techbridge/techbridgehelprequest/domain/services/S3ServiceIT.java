package es.techbridge.techbridgehelprequest.domain.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class S3ServiceIT {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Autowired
    private S3Service s3Service;

    private final String bucketName = "techbridge-test-s3";

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client, s3Presigner);
        ReflectionTestUtils.setField(s3Service, "bucketName", bucketName);
    }

    @Test
    void uploadResource_ShouldUploadAndReturnKey() throws IOException {
        // GIVEN
        String key = "test-file.pdf";
        MockMultipartFile file = new MockMultipartFile(
                "file", "original.pdf", "application/pdf", "contenido-binario".getBytes()
        );

        // WHEN
        String result = s3Service.uploadResource(key, file);

        // THEN
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(captor.capture(), any(RequestBody.class));

        assertEquals(bucketName, captor.getValue().bucket());
        assertEquals(key, captor.getValue().key());
        assertEquals("application/pdf", captor.getValue().contentType());
        assertEquals(key, result);
    }

    @Test
    void downLoadResource_ShouldReturnPresignedUrl() throws Exception {
        // GIVEN
        String key = "recurso-persona-mayor.pdf";
        String expectedUrl = "https://s3.amazonaws.com/fake-url-signed";

        // Mockeamos el comportamiento complejo del presigner
        PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(new URL(expectedUrl));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // WHEN
        String result = s3Service.downLoadResource(key);

        // THEN
        assertEquals(expectedUrl, result);

        // Verificamos que se llamó con el bucket y key correctos
        ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(captor.capture());

        assertEquals(bucketName, captor.getValue().getObjectRequest().bucket());
        assertEquals(key, captor.getValue().getObjectRequest().key());
    }

}
