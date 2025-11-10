package br.com.nat.lingodocs.dto.bucket;

import java.util.List;

public record FilesResponse(
        List<String> files
) {
}
