# JobTracker Frontend

Frontend Angular local para reduzir o atrito de quem quer explorar o backend
sem depender apenas de requests manuais.

## O que esta pronto

- tela de acesso com criacao de conta e login
- workspace para criar vagas e candidaturas
- lista de candidaturas
- detalhe de candidatura com:
  - status macro
  - etapas
  - notas
  - historico consolidado
  - leitura de matching

## Requisitos

- Node 22 LTS ou superior
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

## Build

```bash
npm run build
```

## Testes

```bash
CHROME_BIN=$(which google-chrome || which chromium || which chromium-browser) npm test -- --watch=false --browsers=ChromeHeadless
```
