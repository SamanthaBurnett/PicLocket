package com.sbproject.piclocket.controller;

import com.sbproject.piclocket.dto.CreateUploadRequest;
import com.sbproject.piclocket.dto.CreateUploadResponse;
import com.sbproject.piclocket.dto.PhotoResponse;
import com.sbproject.piclocket.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    /**
     * Send request to get a presigned S3 URL.
     *
     * @param request represents photo metadata
     * @return {@link CreateUploadResponse} represents the presigned URL and photo id
     */
    @PostMapping("/upload-request")
    public CreateUploadResponse createUploadRequest(@RequestBody CreateUploadRequest request) {
        return photoService.createUploadRequest(request);
    }

    /**
     * Returns a list of uploaded photos.
     *
     * @return list of {@link PhotoResponse}
     */
    @GetMapping
    public List<PhotoResponse> getUploadedPhotos() {
        return photoService.getUploadedPhotos();
    }
}
