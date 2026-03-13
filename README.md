# JobTracker

Backend de um sistema para organizar candidaturas, etapas de processos seletivos,
feedbacks e aderencia entre habilidades do candidato e requisitos das vagas.

## Linguagem de dominio

- `User`: representa o perfil do candidato no sistema.
- `Job`: representa a vaga publicada por uma empresa.
- `Application`: representara a candidatura feita para uma vaga especifica.
- `Stage`: representara cada etapa do processo seletivo dentro de uma candidatura.

Essas definicoes existem para evitar que `Job` assuma responsabilidades de
`Application`, ja que a vaga e a candidatura sao conceitos diferentes no dominio.

## Estrutura atual

- `domain`: entidades e contratos centrais do negocio.
- `application`: casos de uso e DTOs.
- `interfaces`: controllers REST e tratamento de excecoes.
- `infrastructure`: persistencia JPA, mapeadores e adaptadores.

## Perfis de configuracao

- `dev`: perfil padrao para execucao local com H2 em memoria.
- `test`: perfil usado pelos testes automatizados.

O projeto segue com Flyway para versionamento do banco e `ddl-auto=validate`
para garantir que o mapeamento JPA acompanhe as migrations.
