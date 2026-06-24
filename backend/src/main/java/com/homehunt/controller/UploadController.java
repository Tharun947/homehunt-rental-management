package com.homehunt.controller;

import com.homehunt.dto.ImageUploadResponse;
import com.homehunt.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {
    private final FileStorageService fileStorageService;

    @PostMapping("/images")
    @PreAuthorize("hasAnyRole('LANDLORD','ADMIN')")
    ImageUploadResponse image(@RequestParam("file") MultipartFile file) {
        return new ImageUploadResponse(fileStorageService.storeImage(file));
    }
}
