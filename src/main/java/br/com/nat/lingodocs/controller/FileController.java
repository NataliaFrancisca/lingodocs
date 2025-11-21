package br.com.nat.lingodocs.controller;

import br.com.nat.lingodocs.dto.auth.response.MessageResponse;
import br.com.nat.lingodocs.dto.bucket.FileResponse;
import br.com.nat.lingodocs.dto.bucket.FilesResponse;
import br.com.nat.lingodocs.service.model.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/file")
public class FileController {

    @Autowired
    private FileService service;

    private String getUsernameFromAuth(Authentication auth){
        var jwt = (Jwt) auth.getPrincipal();
        return jwt.getClaim("username");
    }

    @PostMapping("/upload")
    public ResponseEntity<MessageResponse> upload(@RequestParam("file") MultipartFile file, Authentication auth){
        var username = getUsernameFromAuth(auth);
        this.service.upload(file, username);
        return ResponseEntity.ok().body(new MessageResponse("response: ", "Upload realizado com sucesso."));
    }

    @GetMapping("/all")
    public ResponseEntity<FilesResponse> getAll(Authentication auth){
        var username = getUsernameFromAuth(auth);
        var response = this.service.getAll(username);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<FileResponse> get(@RequestParam String fileName, Authentication auth){
        var username = getUsernameFromAuth(auth);
        var response = this.service.get(fileName, username);
        return ResponseEntity.ok().body(response);
    }
}
