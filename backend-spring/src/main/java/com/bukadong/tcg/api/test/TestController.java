package com.bukadong.tcg.api.test;

import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/test")
@RequiredArgsConstructor
public class TestController {
    private final S3Uploader s3Uploader;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<TestResponse> ttuu(
            @RequestPart(value = "Image") MultipartFile image
    ) {
        String imageUrl = s3Uploader.upload(image, "/test/upload");
        return new BaseResponse<>(new TestResponse(imageUrl));
    }
}
