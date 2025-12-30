package com.amante.clinicmanagement.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() {
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
    }

    // ==========================================
    // UPLOAD TESTS
    // ==========================================

    @Test
    void uploadImage_SuccessAndValidationErrors() throws IOException {
        // Test 1: Success case
        MockMultipartFile validFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        String expectedUrl = "https://url.com/img.jpg";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("secure_url", expectedUrl);

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResponse);
        String result = cloudinaryService.uploadImage(validFile, "folder");
        assertEquals(expectedUrl, result);

        // Test 2: Empty file
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cloudinaryService.uploadImage(emptyFile, "folder"));
        assertEquals("File is empty", exception.getMessage());

        // Test 3: Null content type
        MockMultipartFile nullContentFile = new MockMultipartFile("file", "test", null, "content".getBytes());
        exception = assertThrows(IllegalArgumentException.class,
                () -> cloudinaryService.uploadImage(nullContentFile, "folder"));
        assertEquals("File must be an image", exception.getMessage());

        // Test 4: Invalid content type (not image)
        MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        exception = assertThrows(IllegalArgumentException.class,
                () -> cloudinaryService.uploadImage(pdfFile, "folder"));
        assertEquals("File must be an image", exception.getMessage());
    }

    // ==========================================
    // DELETE TESTS
    // ==========================================

    @Test
    void deleteImage_SuccessScenarios_AllExtensionCases() throws IOException {
        // Test 1: URL with extension (lastDotIndex > 0)
        String urlWithExt = "https://res.cloudinary.com/demo/image/upload/v12345/folder/id.jpg";
        cloudinaryService.deleteImage(urlWithExt);
        verify(uploader).destroy(eq("folder/id"), anyMap());

        // Test 2: URL without extension (lastDotIndex not found, returns full string)
        String urlNoExt = "https://res.cloudinary.com/demo/image/upload/v123/doctors/sample-id";
        cloudinaryService.deleteImage(urlNoExt);
        verify(uploader).destroy(eq("doctors/sample-id"), anyMap());

        // Test 3: URL with dot at start (lastDotIndex = 0, not > 0)
        String urlDotAtStart = "https://res.cloudinary.com/demo/image/upload/v123/.hiddenfile";
        cloudinaryService.deleteImage(urlDotAtStart);
        verify(uploader).destroy(eq(".hiddenfile"), anyMap());

        // Verify total destroy calls
        verify(uploader, times(3)).destroy(anyString(), anyMap());
    }

    @Test
    void deleteImage_ValidationErrors_NoDestroyCall() throws IOException {
        // Test 1: Null URL
        cloudinaryService.deleteImage(null);

        // Test 2: Invalid URL (no /upload/)
        cloudinaryService.deleteImage("https://google.com/image.jpg");

        // Test 3: Short URL (parts.length < MINIMUM_URL_PARTS)
        cloudinaryService.deleteImage("https://res.cloudinary.com/demo/image/upload/");

        // Verify no destroy calls were made for any invalid cases
        verify(uploader, never()).destroy(anyString(), anyMap());
    }
}