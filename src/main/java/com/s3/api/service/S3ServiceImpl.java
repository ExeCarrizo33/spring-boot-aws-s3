package com.s3.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements IS3Service{

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    @Value("${spring.destination.folder}")
    private String destinationFolder;


    /**
     * Crea un nuevo bucket en S3.
     *
     * @param bucketName El nombre del bucket a crear.
     * @return Un mensaje indicando la ubicación del bucket creado.
     */
    @Override
    public String createBucket(String bucketName) {
        CreateBucketResponse response = this.s3Client.createBucket(bb -> bb.bucket(bucketName));
        return "Bucket creado en el ubicacion: " + response.location();
    }

    /**
     * Verifica si un bucket existe en S3.
     *
     * @param bucketName El nombre del bucket a verificar.
     * @return Un mensaje indicando si el bucket fue encontrado o no.
     */
    @Override
    public String checkIfBucketExist(String bucketName) {
        try {
            this.s3Client.headBucket(hb -> hb.bucket(bucketName));
            return "Bucket encontrado";
        } catch (S3Exception e) {
            return "Bucket no encontrado";
        }

    }

    /**
     * Obtiene una lista de todos los buckets en S3.
     *
     * @return Una lista con los nombres de todos los buckets.
     */
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

    /**
     * Sube un archivo a un bucket en S3.
     *
     * @param bucketName  El nombre del bucket.
     * @param key         La clave del archivo.
     * @param fileLocation La ubicación del archivo a subir.
     * @return true si la subida fue exitosa, false en caso contrario.
     */
    @Override
    public Boolean uploadFile(String bucketName, String key, Path fileLocation) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectResponse putObjectResponse = this.s3Client.putObject(putObjectRequest, fileLocation);
        return putObjectResponse.sdkHttpResponse().isSuccessful();
    }

    /**
     * Descarga un archivo de un bucket en S3.
     *
     * @param bucketName El nombre del bucket.
     * @param key        La clave del archivo.
     * @throws IOException Si ocurre un error durante la descarga.
     */
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

    /**
     * Genera una URL prefirmada para subir un archivo a S3.
     *
     * @param bucketName El nombre del bucket.
     * @param key        La clave del archivo.
     * @param duration   La duración de la validez de la URL.
     * @return La URL prefirmada para subir el archivo.
     */
    @Override
    public String generatePresignedUploadUrl(String bucketName, String key, Duration duration) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest = this.s3Presigner.presignPutObject(presignRequest);

        URL presignedUrl = presignedPutObjectRequest.url();

        return presignedUrl.toString();
    }

    /**
     * Genera una URL prefirmada para descargar un archivo de S3.
     *
     * @param bucketName El nombre del bucket.
     * @param key        La clave del archivo.
     * @param duration   La duración de la validez de la URL.
     * @return La URL prefirmada para descargar el archivo.
     */
    @Override
    public String generatePresignedDownloadUrl(String bucketName, String key, Duration duration) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();



        PresignedGetObjectRequest presignedRequest = this.s3Presigner.presignGetObject(presignRequest);

        URL presignedUrl = presignedRequest.url();

        return presignedUrl.toString();
    }
}
