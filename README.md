# JobTracker

Backend de um sistema para organizar candidaturas, etapas de processos
seletivos, feedbacks e aderencia entre habilidades do candidato e requisitos
das vagas.

O projeto esta sendo construido como laboratorio de boas praticas de backend,
arquitetura e evolucao profissional, com foco em um fluxo real do ponto de
vista do candidato.

## Estado atual

Hoje o projeto ja possui:

- cadastro de usuario
- cadastro de skills e vagas
- candidaturas (`Application`)
- etapas do processo seletivo (`Stage`)
- habilidades do candidato (`UserSkill`)
- requisitos da vaga (`JobRequirement`)
- notas e feedbacks (`Note`)
- historico consolidado da candidatura
- analise de matching entre perfil e vaga
- autenticacao minima com HTTP Basic

## Linguagem de dominio

- `User`: representa o perfil do candidato no sistema.
- `Skill`: representa uma habilidade conhecida pelo sistema.
- `UserSkill`: representa uma habilidade do candidato com nivel e tempo de experiencia.
- `Job`: representa a vaga publicada por uma empresa.
- `JobRequirement`: representa um requisito de uma vaga, com nivel desejado, peso e indicacao de `must-have`.
- `Application`: representa a candidatura feita para uma vaga especifica.
- `Stage`: representa uma etapa do processo seletivo dentro de uma candidatura.
- `Note`: representa uma observacao ou feedback vinculado a uma candidatura e, opcionalmente, a uma etapa.

Essas definicoes existem para evitar que uma entidade assuma responsabilidades
que pertencem a outra. Exemplo: `Job` e `Application` sao conceitos diferentes
no dominio.

## Estrutura do projeto

- `domain`: entidades e contratos centrais do negocio.
- `application`: casos de uso, servicos de aplicacao e DTOs.
- `interfaces`: controllers REST e tratamento de excecoes.
- `infrastructure`: persistencia JPA, seguranca, mapeadores e configuracoes.

## Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Validation
- Spring Data JPA
- Flyway
- Spring Security
- H2
- Maven
- Angular 19
- SCSS
- npm

## Como executar

### Backend

Executar a aplicacao localmente:

```bash
./mvnw spring-boot:run
```

Executar a aplicacao com Docker Compose:

```bash
docker compose up --build
```

Esse fluxo sobe o backend e o banco local. O frontend Angular continua sendo
executado separadamente em `frontend/`.

Executar os testes:

```bash
./mvnw test
```

### Frontend Angular

O frontend mora em `frontend/` e foi pensado para melhorar a experiencia local de
quem clona o projeto.

Requisitos recomendados:

- Node 22 LTS recomendado
- Google Chrome ou Chromium para a execucao dos testes do Angular

Instalar dependencias:

```bash
cd frontend
npm install
```

Subir o frontend em modo desenvolvimento:

```bash
npm start
```

O `ng serve` usa um proxy local para encaminhar `/api` e `/actuator` para o
backend em `http://localhost:8080`, evitando problemas de CORS durante o
desenvolvimento.

Build do frontend:

```bash
npm run build
```

Testes do frontend:

```bash
npm test
```

O script usa `ChromeHeadlessNoSandbox`, que se mostrou mais estavel para a
validacao local e no CI.

### Fluxo local completo

Em dois terminais:

Terminal 1:

```bash
./mvnw spring-boot:run
```

Terminal 2:

```bash
cd frontend
npm install
npm start
```

Depois abra:

- `http://localhost:4200`

Fluxo sugerido para primeira execucao:

1. criar uma conta pela tela de acesso do frontend
2. entrar no workspace
3. cadastrar uma vaga
4. vincular suas principais skills ao perfil
5. criar uma candidatura
6. abrir o detalhe da candidatura e explorar etapas, notas, requisitos e matching

Tambem da para usar o proprio dashboard como central operacional para:

- registrar nota rapida
- iniciar ou concluir a proxima etapa
- limpar a proxima acao manual
- encerrar ou reativar candidaturas sem abrir o detalhe

## Perfis de configuracao

- `dev`: perfil padrao para execucao local com H2 em memoria e console H2 habilitado.
- `test`: perfil usado pelos testes automatizados.

O projeto usa Flyway para versionamento do banco e `ddl-auto=validate` para
garantir que o mapeamento JPA acompanhe as migrations.

## Autenticacao

O projeto possui autenticacao minima com HTTP Basic.

Regras atuais:

- `POST /api/v1/users` e publico para permitir criacao de usuario.
- os demais endpoints exigem autenticacao.
- as senhas sao persistidas com BCrypt.
- `GET /api/v1/auth/me` retorna `id`, `name` e `email` do usuario autenticado.

Exemplo de uso:

Criar usuario:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Nicolas",
    "email":"nicolas@example.com",
    "password":"123456",
    "headline":"Backend Developer",
    "location":"Brasil",
    "bio":"Bio"
  }'
```

Validar autenticacao:

```bash
curl -u nicolas@example.com:123456 \
  http://localhost:8080/api/v1/auth/me
```

Resposta esperada:

```json
{
  "id": "uuid-do-usuario",
  "name": "Nicolas",
  "email": "nicolas@example.com"
}
```

Consumir um endpoint protegido:

```bash
curl -u nicolas@example.com:123456 \
  http://localhost:8080/api/v1/applications
```

## Endpoints principais

Fluxo de usuario:

- `POST /api/v1/users`
- `GET /api/v1/auth/me`

Fluxo de candidaturas:

- `POST /api/v1/applications`
- `GET /api/v1/applications`
- `GET /api/v1/applications/{id}`
- `PATCH /api/v1/applications/{id}/status`
- `GET /api/v1/applications/{id}/history`

Fluxo de etapas:

- `POST /api/v1/stages`
- `GET /api/v1/stages/{id}`
- `GET /api/v1/applications/{applicationId}/stages`
- `PATCH /api/v1/stages/{id}/start`
- `PATCH /api/v1/stages/{id}/complete`
- `DELETE /api/v1/stages/{id}`

Perfil tecnico e requisitos:

- `POST /api/v1/user-skills`
- `GET /api/v1/user-skills/{id}`
- `GET /api/v1/users/{userId}/skills`
- `POST /api/v1/job-requirements`
- `GET /api/v1/job-requirements/{id}`
- `GET /api/v1/jobs/{jobId}/requirements`
- `DELETE /api/v1/job-requirements/{id}`

Analise e feedback:

- `GET /api/v1/jobs/{jobId}/matching?userId={userId}`
- `POST /api/v1/notes`
- `GET /api/v1/notes/{id}`
- `GET /api/v1/applications/{applicationId}/notes`
- `DELETE /api/v1/notes/{id}`

## Frontend atual

O MVP Angular atual cobre:

- autenticacao basica reaproveitando o HTTP Basic do backend
- criacao de conta e abertura de sessao
- workspace local para criar vagas, skills, skills do usuario e candidaturas
- listagem de candidaturas com filtros por empresa e status
- leitura de prioridades no dashboard com prazos e proximos passos
- tela de detalhe com:
  - atualizacao de status
  - requisitos da vaga
  - etapas
  - notas
  - historico consolidado
  - leitura de matching
  - remocao de requisito, nota e etapa

## Observacoes

- Em `dev`, a aplicacao usa H2 em memoria.
- A seguranca fica habilitada por padrao durante a execucao normal.
- Nos testes, a propriedade `jobtracker.security.enabled=false` e usada para
  manter os testes de controller focados no contrato HTTP, enquanto os cenarios
  de autenticacao ficam cobertos por testes de integracao especificos.

## TODOs de containerizacao

- mover credenciais de exemplo do `docker-compose.yml` para variaveis externas, como `.env`, antes de qualquer uso fora do ambiente local
- evitar expor `5432:5432` quando apenas a aplicacao precisar acessar o banco
- usar credenciais diferentes das de desenvolvimento em qualquer ambiente publicado
