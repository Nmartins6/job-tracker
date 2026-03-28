# JobTracker Frontend

Frontend Angular local para reduzir o atrito de quem quer explorar o backend
sem depender apenas de requests manuais.

## O que esta pronto

- tela de acesso com criacao de conta e login
- workspace para:
  - criar vagas
  - criar skills
  - vincular skills ao perfil do usuario
  - criar candidaturas
- lista de candidaturas com filtros por empresa e status
- painel com leitura de prioridades baseada em etapas e prazos
- detalhe de candidatura com:
  - status macro
  - requisitos da vaga
  - etapas
  - notas
  - historico consolidado
  - leitura de matching
  - remocao de requisito, nota e etapa

## Requisitos

- Node 22 LTS recomendado
- backend Spring Boot rodando em `http://localhost:8080`
- Google Chrome ou Chromium para a execucao dos testes

## Rodando localmente

Instale as dependencias:

```bash
npm install
```

Suba o app:

```bash
npm start
```

Abra:

```text
http://localhost:4200
```

O frontend usa `proxy.conf.json` para encaminhar chamadas de `/api` e
`/actuator` para o backend local.

Fluxo sugerido:

1. criar a conta
2. cadastrar skills e vincular as principais ao perfil
3. mapear a vaga
4. abrir a candidatura
5. usar o detalhe para registrar requisitos, etapas, notas e acompanhar o matching

## Build

```bash
npm run build
```

## Testes

```bash
CHROME_BIN=$(which google-chrome || which chromium || which chromium-browser) npm test -- --watch=false --browsers=ChromeHeadless
```
