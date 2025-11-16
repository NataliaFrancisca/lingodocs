# API Lingodocs

Essa API tem como objetivo realizar a tradução automática de arquivos `.txt`.

> 🌟 Essa API foi construída com o objetivo de integrar os principais serviços da AWS como parte do meu estudo. É um projeto básico e que pode ter erros, mas o objetivo principal foi de entender como esses serviços funcionavam e como integrar eles.

> 🚨 A tradução dos arquivos é gerada por Inteligência Artificial, então é sempre importante revisar conteúdo gerado.
> 🔗 Confira a função responsável pela tradução: https://github.com/NataliaFrancisca/lingodocs-translate

## Detalhes
- O usuário acessa a API e deve se autenticar para acessar os serviços.
- A autenticação é o cadastro e confirmação (é realizada por e-mail).
- Depois de autenticado, o usuário tem o acesso permitido.
- O usuário pode fazer o upload do arquivo .txt, listar os arquivos já traduzidos (exibe somente o título desses arquivos) e buscar um arquivo (gerando uma URL).

## Funcionamento

### Autenticação
| rota | método | body |
|------|--------|------|
| /api/auth/signup | POST | {name; email; password} |
| /api/auth/confirm | POST | {email; token} | 
| /api/auth/signin | POST | {email; password} | 
| /api/auth/refresh | POST | {refreshToken} | 

### Arquivos
| rota | método | body | auth |
|------|--------|------|------|
| /api/file/upload | POST | {file} | JWT |
| /api/file/all | GET | - | JWT |
| /api/file | GET | {name} | JWT |

## Tecnologias
- Java
- Spring Boot
- Gemini API
- Amazon S3
- Amazon Lambda
- Amazon API Gateway
- Amazon CloudWatch
- Amazon Cognito

## Fluxo do Projeto:
1. Usuário cria conta via endpoint.
2. Recebe código de confirmação por e-mail (Cognito).
3. Faz login e recebe o JWT.
4. Envia um arquivo .txt para o S3 via API.
5. A Lambda (lingodocs translate) dispara a função de tradução para os arquivos no bucket (/inboud).
6. Tradução é salva em outra pasta do bucket (/outbound).
8. API permite listar e gerar URL de download.

## Arquitetura do Projeto:


