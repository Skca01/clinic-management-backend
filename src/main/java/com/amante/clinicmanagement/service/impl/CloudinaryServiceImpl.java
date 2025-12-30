package com.amante.clinicmanagement.service.impl;

import com.amante.clinicmanagement.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {
    private static final int IMAGE_DIMENSION = 500;
    private static final int MINIMUM_URL_PARTS = 2;

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(
            MultipartFile file,
            String folder
    ) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        String publicId = folder + "/" + UUID.randomUUID().toString();

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "folder", folder,
                        "resource_type", "image",
                        "transformation", new Transformation()
                                .width(IMAGE_DIMENSION)
                                .height(IMAGE_DIMENSION)
                                .crop("fill")
                                .gravity("face")
                                .quality("auto")
                ));

        return (String) uploadResult.get("secure_url");
    }

    @Override
    public void deleteImage(String imageUrl) throws IOException {
        // REMOVED the check here. We let the helper method handle nulls.
        String publicId = extractPublicIdFromUrl(imageUrl);

        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("/upload/")) {
            log.warn("Invalid URL, cannot extract public ID: {}", url);
            return null;
        }

        String[] parts = url.split("/upload/");
        if (parts.length < MINIMUM_URL_PARTS) {
            return null;
        }

        String afterUpload = parts[1];
        String publicIdWithExtension = afterUpload.replaceFirst("v\\d+/", "");
        int lastDotIndex = publicIdWithExtension.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return publicIdWithExtension.substring(0, lastDotIndex);
        }

        return publicIdWithExtension;
    }
}