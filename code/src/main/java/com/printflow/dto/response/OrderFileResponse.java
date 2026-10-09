package com.printflow.dto.response;

public record OrderFileResponse(
        Long id,
        String fileName,
        String filePath,
        String fileType
) {
}
