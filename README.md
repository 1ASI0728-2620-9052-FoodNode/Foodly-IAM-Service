# Foodly-IAM-Service

Identity service of Foodly. It registers and authenticates users and issues the
JWT that every other Foodly service validates. Default port: `8081`.

## Configuration

Spring Boot reads these environment variables (see [`.env.example`](.env.example);
the real `.env` is ignored by Git and must never be committed).

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `DB_URL` | No | `jdbc:postgresql://localhost:5433/foodly_iam` | PostgreSQL JDBC URL |
| `DB_USERNAME` | No | `foodly_iam` | Database user |
| `DB_PASSWORD` | **Yes** | — | Database password |
| `JWT_SECRET_BASE64` | **Yes** | — | HS256 signing key, standard Base64 of at least 32 bytes |
| `JWT_ISSUER` | No | `foodly-iam` | `iss` claim of every issued token |
| `JWT_AUDIENCES` | No | `foodly-iam,foodly-business,foodly-radar,foodly-messaging,foodly-reservation,foodly-community` | Comma-separated `aud` claim |

### JWT contract

Tokens are signed with HS256 and carry `iss`, `aud`, `sub` (user UUID), `role`
(`HUARIQUE_OWNER` or `GASTRONOMIC_EXPLORER`, without the `ROLE_` prefix), `iat`
and `exp` (60 minutes). Clients send the `token` field of the login/register
response as `Authorization: Bearer <token>`.

- **`JWT_SECRET_BASE64`**: generate it with `openssl rand -base64 32` and share it
  with the other services through a private channel, never through Git, chat,
  PRs or logs. The bytes are the *decoded* Base64 value.
- **`JWT_ISSUER`**: IAM rejects tokens whose `iss` differs when it validates them.
- **`JWT_AUDIENCES`**: every consumer must find its own audience in the list.
  The list **must include `foodly-iam`**: IAM validates its own tokens against
  it and the service refuses to start otherwise. Clock skew is fixed at 60 s.

Matching Business configuration (see `Foodly-Business-Service/docs/iam-business-jwt.md`):

| Business | Value |
|---|---|
| `IAM_JWT_SECRET_BASE64` | same value as `JWT_SECRET_BASE64` |
| `IAM_JWT_ISSUER` | same value as `JWT_ISSUER` (`foodly-iam`) |
| `IAM_JWT_AUDIENCE` | `foodly-business` |
| `IAM_OWNER_ROLE` | `HUARIQUE_OWNER` |

> Radar and Messaging currently read `JWT_SECRET` as UTF-8 text instead of
> decoding Base64, so they will not accept these tokens until that is aligned.

## Tests

```bash
./mvnw test                                      # needs PostgreSQL and the env vars above
./mvnw test -Dtest='!IamServiceApplicationTests' # unit tests only, no database
```
