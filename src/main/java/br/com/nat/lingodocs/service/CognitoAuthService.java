package br.com.nat.lingodocs.service;

import br.com.nat.lingodocs.dto.auth.response.AuthTokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class CognitoAuthService {
    @Value("${aws.cognito.region}")
    private String region;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret:}")
    private String clientSecret;

    private CognitoIdentityProviderClient cognitoClient;

    private CognitoIdentityProviderClient getCognitoClient(){
        if (cognitoClient == null){
            cognitoClient = CognitoIdentityProviderClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }

        return cognitoClient;
    }

    public String signUp(String email, String password, String name){
        try{
            AttributeType emailAttr = AttributeType.builder()
                    .name("email")
                    .value(email)
                    .build();

            AttributeType nameAttr = AttributeType.builder()
                    .name("name")
                    .value(name)
                    .build();

            SignUpRequest.Builder signUpRequestBuilder = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .password(password)
                    .userAttributes(emailAttr, nameAttr);

            if (clientSecret != null && !clientSecret.isEmpty()){
                String secretHash = calculateSecretHash(email);
                signUpRequestBuilder.secretHash(secretHash);
            }

            SignUpResponse response = getCognitoClient().signUp(signUpRequestBuilder.build());
            return response.userSub();
        }catch (CognitoIdentityProviderException ex){
            throw new RuntimeException("Erro ao registrar usuário: " + ex.awsErrorDetails());
        }
    }

    public void confirmSignUp(String email, String confirmationCode){
        try{
            ConfirmSignUpRequest.Builder confirmRequest = ConfirmSignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .confirmationCode(confirmationCode);

            if (clientSecret != null && !clientSecret.isEmpty()){
                String secretHash = calculateSecretHash(email);
                confirmRequest.secretHash(secretHash);
            }

            getCognitoClient().confirmSignUp(confirmRequest.build());
        }catch (CognitoIdentityProviderException ex){
            throw new RuntimeException("Erro ao confirmar usuário: " + ex.awsErrorDetails());
        }
    }

    public AuthTokens signIn(String email, String password){
        try{
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", email);
            authParams.put("PASSWORD", password);

            if (clientSecret != null && !clientSecret.isEmpty()) {
                String secretHash = calculateSecretHash(email);
                authParams.put("SECRET_HASH", secretHash);
            }

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse = getCognitoClient().initiateAuth(authRequest);
            AuthenticationResultType result = authResponse.authenticationResult();

            return new AuthTokens(
                    result.accessToken(),
                    result.idToken(),
                    result.refreshToken(),
                    result.expiresIn()
            );
        } catch (CognitoIdentityProviderException  ex) {
            throw new RuntimeException("Erro ao autenticar: " + ex.awsErrorDetails().errorMessage());
        }
    }

    public AuthTokens refreshToken(String refreshToken){
        try{
            Map<String, String> authParams = new HashMap<>();
            authParams.put("REFRESH_TOKEN", refreshToken);

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .clientId(clientId)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse = getCognitoClient().initiateAuth(authRequest);
            AuthenticationResultType result = authResponse.authenticationResult();

            return new AuthTokens(
                result.accessToken(),
                result.idToken(),
                refreshToken,
                result.expiresIn()
            );
        } catch (CognitoIdentityProviderException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String calculateSecretHash(String username) {
        try {
            String message = username + clientId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    clientSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular SECRET_HASH", e);
        }
    }
}
