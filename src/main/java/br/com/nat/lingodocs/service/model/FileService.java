package br.com.nat.lingodocs.service.model;

import br.com.nat.lingodocs.dto.bucket.FileResponse;
import br.com.nat.lingodocs.dto.bucket.FilesResponse;
import br.com.nat.lingodocs.service.aws.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    @Autowired
    private S3Service s3Service;

    private final long MAX_FILE_SIZE = 1024 * 1024;

    public String upload(MultipartFile file, String username){
        try{
            this.validateTextFile(file);
            String originalFileName = this.fixOriginalName(file);
            System.out.println(originalFileName);
            System.out.println("calling from upload method");
            return this.s3Service.insert(file, originalFileName, username);
        }catch (Exception ex){
            throw new RuntimeException("Erro ao tentar fazer upload do arquivo txt. ");
        }
    }

    public FilesResponse getAll(String username){
        return this.s3Service.getAllFilesName(username);
    }

    public FileResponse get(String name, String username){
        return this.s3Service.get(name, username);
    }

    private void validateTextFile(MultipartFile file){
        if (file == null || file.isEmpty()){
            throw new IllegalArgumentException("Arquivo vazio.");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".txt")){
            throw new IllegalArgumentException("Somente arquivos TXT são permitidos.");
        }

        if (file.getSize() > MAX_FILE_SIZE){
            throw new IllegalArgumentException("Tamanho máximo permitido: " + (MAX_FILE_SIZE / 1024) + " KB");
        }
    }

    private String fixOriginalName(MultipartFile file){
        int MAX_FILENAME_LENGHT = 60;
        String originalFileName = file.getOriginalFilename();

        if (originalFileName.length() > MAX_FILENAME_LENGHT){
            throw new IllegalArgumentException("Nome do arquivo excede " + MAX_FILE_SIZE + " caracteres");
        }

        originalFileName = originalFileName.replace("_", "-");
        originalFileName = originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "-");
        originalFileName = originalFileName.replaceAll("-{2,}", "-");
        originalFileName = originalFileName.replaceAll("^-+|-+$", "");
        originalFileName = originalFileName.toLowerCase();

        return originalFileName;
    }
}
