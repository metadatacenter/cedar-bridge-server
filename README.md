# cedar-bridge-server

[![CI](https://github.com/metadatacenter/cedar-bridge-server/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/metadatacenter/cedar-bridge-server/actions/workflows/ci.yml)

CEDAR's integration service for external identifiers and authorities. Its DataCite API transforms CEDAR
metadata into DataCite records, mints DOIs, and retrieves DOI metadata. Its external-authority API
normalizes search and detail lookups across ORCID, ROR, PFAS/CompTox, PubMed, RRID, NIH grants, and
DOI providers.

`POST /ext-auth/{authority}/verify` accepts `{ "iri": "..." }` and returns `status`, `code`,
`message` and the unchanged `iri`. It recognizes the authority's identifier before resolving it
through the existing registry service, and requires the returned record to identify the requested
entry. `passed` and `failed` describe completed checks; unavailable registries or unusable answers
produce `unavailable`. DataCite absence is also `unavailable`, because other DOI registration
agencies were not checked. Malformed requests return 400. These facts do not decide save policy.

The reactor contains `cedar-bridge-server-core` for DataCite model and transformation logic and
`cedar-bridge-server-application` for the deployable Dropwizard service and external-authority adapters.

## Development

CEDAR backend development uses Java 17. From a configured CEDAR workspace:

```bash
export CEDAR_HOME="$HOME/CEDAR"
CEDAR_PROFILE=develop source "$CEDAR_HOME/cedar-development/bin/templates/cedar-profile-native.sh"
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw test
```

Use `cedarcli native start all` to run the service with the rest of the native stack.
The canonical setup, build, test, dependency, and runtime instructions are in the
[CEDAR backend runbook](https://github.com/metadatacenter/cedar-development/blob/develop/ops/BACKEND-RUNBOOK.md).
