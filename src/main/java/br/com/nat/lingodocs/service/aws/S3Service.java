package br.com.nat.lingodocs.service.aws;

import br.com.nat.lingodocs.dto.bucket.FileResponse;
import br.com.nat.lingodocs.dto.bucket.FilesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class S3Service {
    @Value("${aws.s3.bucket.name}")
    private String BUCKET_NAME;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3Service(){
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public String insert(MultipartFile file, String fileName, String username){
        try{
            String outputFileName = "inbound/" + username + "/" + fileName;

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(this.BUCKET_NAME)
                            .key(outputFileName)
                            .contentType("text/plain; charset=utf-8")
                            .metadata(Map.of(
                                    "username", username
                            ))
                    .build(), RequestBody.fromBytes(file.getBytes()));

            return outputFileName;
        }catch (S3Exception ex){
            throw new RuntimeException("Erro ao tentar adicionar o arquivo txt no bucket.");
        } catch (IOException e) {
            throw new RuntimeException("Erro ao tentar válidar o arquivo txt.");
        }
    }

    public FilesResponse getAllFilesName(String username){
        try{
            String prefix = "outbound/" + username;

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(this.BUCKET_NAME)
                    .prefix(prefix)
                    .build();

            var listObjects = s3Client.listObjectsV2(request);

            var response = listObjects.contents().stream()
                    .map(S3Object::key)
                    .map(key -> Paths.get(key).getFileName().toString())
                    .toList();

            if (response.isEmpty()){
                throw new NoSuchElementException("Nenhum arquivo encontrado.");
            }

            return new FilesResponse(response);
        }catch (S3Exception ex){
            throw new RuntimeException("Erro ao tentar listar os arquivos disponíveis para você.");
        }
    }

    public FileResponse get(String originalName, String username){
        String file = originalName;

        if (!originalName.endsWith(".txt")){
            file = originalName + ".txt";
        }

        String key = "outbound/" + username + "/" + file;

        if (!fileExists(key)){
            throw new NoSuchElementException("Arquivo indicado não foi encontrado.");
        }

        try{
            GetObjectRequest getObject = GetObjectRequest.builder()
                    .bucket(this.BUCKET_NAME)
                    .key(key)
                    .responseContentDisposition("attachment; filename=\"" + file + "\"")
                    .responseContentType("application/octet-stream")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getObject)
                    .build();

            PresignedGetObjectRequest presignedRequest =
                    this.s3Presigner.presignGetObject(presignRequest);

            return new FileResponse(file, presignedRequest.url().toString());
        }catch (S3Exception ex){
            throw new RuntimeException("Erro ao tentar gerar URL para o arquivo.");
        }
    }

    private boolean fileExists(String key){
        try{
            HeadObjectRequest req = HeadObjectRequest.builder()
                    .bucket(this.BUCKET_NAME)
                    .key(key)
                    .build();

            s3Client.headObject(req);
            return true;
        }catch (S3Exception ex){
            if (ex.statusCode() == 404){
                return false;
            }
            throw new RuntimeException("Erro ao acessar S3");
        }
    }
}
