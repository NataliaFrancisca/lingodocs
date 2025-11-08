package br.com.nat.lingodocs.dto.auth.request;

public record ConfirmSignUpRequest(
        String email,
        String code
) {
}
