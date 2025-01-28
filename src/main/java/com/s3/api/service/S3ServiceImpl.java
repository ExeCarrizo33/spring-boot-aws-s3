package com.s3.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements IS3Service{

    private final S3Client s3Client;

    @Value("${spring.destination.folder}")
    private String destinationFolder;

    @Override
    public String createBucket(String bucketName) {
        CreateBucketResponse response = this.s3Client.createBucket(bb -> bb.bucket(bucketName));
        return "Bucket creado en el ubicacion: " + response.location();
    }

    @Override
    public String checkIfBucketExist(String bucketName) {
        try {
            this.s3Client.headBucket(hb -> hb.bucket(bucketName));
            return "Bucket encontrado";
        } catch (S3Exception e) {
            return "Bucket no encontrado";
        }

    }

    @Override
    public List<String> getAllBuckets() {
        ListBucketsResponse bucketsResponse = this.s3Client.listBuckets();

        if (bucketsResponse.hasBuckets()) {
            return bucketsResponse.buckets()
                    .stream()
                    .map(Bucket::name)
                    .toList();
        }else {
            return List.of();
        }
    }

    @Override
    public Boolean uploadFile(String bucketName, String key, Path fileLocation) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectResponse putObjectResponse = this.s3Client.putObject(putObjectRequest, fileLocation);
        return putObjectResponse.sdkHttpResponse().isSuccessful();
    }

    @Override
    public void downloadFile(String bucketName, String key) throws IOException {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = this.s3Client.getObjectAsBytes(getObjectRequest);

        String filename;
        if (key.contains("/")) {
            filename = key.substring(key.lastIndexOf("/"));
        }else {
            filename = key;
        }

        String filePath = Paths.get(destinationFolder, filename).toString();

        File file = new File(filePath);
        file.getParentFile().mkdir();

        try(FileOutputStream fileOutputStream = new FileOutputStream(file)){
            fileOutputStream.write(objectBytes.asByteArray());
        } catch (IOException e) {
            throw  new IOException("Error al descargar el archivo " + e.getCause());
        }

    }

    @Override
    public String generatePresignedUploadUrl(String bucketName, String key, Duration duration) {
        return "";
    }

    @Override
    public String generatePresignedDownloadUrl(String bucketName, String key, Duration duration) {
        return "";
    }
}
