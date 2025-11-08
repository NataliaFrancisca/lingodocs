package br.com.nat.lingodocs.dto.auth.response;

public record AuthTokens(
        String accessToken,
        String idToken,
        String refreshToken,
        Integer expiresIn
) {
}
