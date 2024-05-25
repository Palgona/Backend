package com.palgona.palgona.service.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.palgona.palgona.common.error.code.ChatErrorCode;
import com.palgona.palgona.common.error.exception.BusinessException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 AmazonS3;

    public String upload(MultipartFile file) {
        String imageUrl = "";
        String fileName = createFileName(file.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            AmazonS3.putObject(new PutObjectRequest(bucket , fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            imageUrl = AmazonS3.getUrl(bucket, fileName).toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to upload");
        }
        return imageUrl;
    }

    public void deleteFile(String imageUrl) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(
                bucket , parseKeyUrl(imageUrl));
        AmazonS3.deleteObject(deleteObjectRequest);
    }

    private String parseKeyUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
    }

    private String createFileName(String fileName) {
        FileExtension extension = FileExtension.from(fileName);
        return UUID.randomUUID().toString().concat(extension.getExtension());
    }

    public String uploadBase64Image(String base64Image) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(decodedBytes.length);
        metadata.setContentType("image/jpeg");

        String fileName = UUID.randomUUID().toString() + ".jpg";
        try (InputStream inputStream = new ByteArrayInputStream(decodedBytes)) {
            AmazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));
        } catch (IOException e) {
            throw new BusinessException(ChatErrorCode.INVALID_FILE);
        }

        return AmazonS3.getUrl(bucket, fileName).toString();
    }
}
