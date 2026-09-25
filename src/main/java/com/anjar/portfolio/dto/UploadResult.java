package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {
    private String url;
    private String publicId;
    private Integer width;
    private Integer height;
    private String format;
    private Long bytes;
}