# logdrift

> CLI tool to detect schema drift in structured log outputs across microservice deployments.

---

## Installation

```bash
curl -sSL https://raw.githubusercontent.com/yourorg/logdrift/main/install.sh | bash
```

Or build from source:

```bash
./mvnw clean package && mv target/logdrift.jar /usr/local/bin/logdrift
```

---

## Usage

Point `logdrift` at two log snapshots to compare their schemas:

```bash
logdrift compare --baseline logs/v1.0/auth-service.log --current logs/v2.1/auth-service.log
```

**Example output:**

```
[DRIFT DETECTED] auth-service
  + Added field:   request.traceId (string)
  - Removed field: metadata.legacyToken (string)
  ~ Type changed:  user.id  integer → string
```

### Options

| Flag | Description |
|------|-------------|
| `--baseline` | Path to the reference log file |
| `--current` | Path to the log file to compare |
| `--format` | Output format: `text` (default), `json`, `csv` |
| `--strict` | Exit with non-zero code on any drift |
| `--ignore` | Comma-separated list of fields to exclude |

Run `logdrift --help` for the full list of commands.

---

## Requirements

- Java 17+
- Structured logs in JSON format (NDJSON supported)

---

## Contributing

Pull requests are welcome. Please open an issue first to discuss any significant changes.

---

## License

[MIT](LICENSE) © 2024 yourorg