package br.com.nat.lingodocs.dto.auth.request;

public record SignInRequest(
        String email,
        String password
) {
}
