# policy-service

Правила доступа и HITL (`/v1/policy`). Internal API для **mcp-gateway** при install/uninstall MCP.

- **Контракт:** [`../api-contracts/policy/openapi.yaml`](../api-contracts/policy/openapi.yaml)
- **ERD:** `cursor-context/docs/erd-and-bounded-contexts.md` §6

## Запуск (изолированно)

```bash
docker compose up --build
```

- API: http://localhost:8085/v1/policy/
- Health: http://localhost:8085/v1/policy/health
- Postgres: localhost:5436 (в `platform/` — общий postgres :5432, БД `policy`)

## Internal apply-pack (mcp-gateway)

```http
POST /v1/policy/internal/installations/{installationId}/apply-pack
X-Policy-Internal-Key: dev-internal-key
Content-Type: application/json

{
  "orgId": "...",
  "workspaceId": "...",
  "connectorKey": "notion",
  "policyPackVersion": 1,
  "pack": {
    "rules": [
      {"effect": "allow_read", "resource_pattern": "mcp:notion:*:read", "priority": 100}
    ]
  },
  "installedByUserId": "..."
}
```

Ответ `200`: `{ "policyRuleGroupId": "...", "rulesCreated": 2 }`.

При удалении installation:

```http
DELETE /v1/policy/internal/installations/{installationId}/revoke-pack
X-Policy-Internal-Key: dev-internal-key
```

## Переменные

| Переменная | По умолчанию |
|------------|----------------|
| `DB_HOST` | localhost |
| `DB_NAME` | policy |
| `POLICY_INTERNAL_API_KEY` | dev-internal-key |

## Platform compose

Полный контур: `../../platform/docker-compose.yml` — `POLICY_SERVICE_ENABLED=true` в mcp.

## Сборка

```bash
./gradlew shadowJar
```
