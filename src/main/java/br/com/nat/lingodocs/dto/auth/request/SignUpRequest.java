package br.com.nat.lingodocs.dto.auth.request;

public record SignUpRequest(
        String email,
        String password,
        String name
) {
}
