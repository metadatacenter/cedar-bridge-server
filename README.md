# cedar-bridge-server

[![CI](https://github.com/metadatacenter/cedar-bridge-server/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/metadatacenter/cedar-bridge-server/actions/workflows/ci.yml)

CEDAR's integration service for external identifiers and authorities. Its DataCite API transforms CEDAR
metadata into DataCite records, mints DOIs, and retrieves DOI metadata. Its external-authority API
normalizes search and detail lookups across ORCID, ROR, PFAS/CompTox, PubMed, RRID, NIH grants, and
DOI providers.

The reactor contains `cedar-bridge-server-core` for DataCite model and transformation logic and
`cedar-bridge-server-application` for the deployable Dropwizard service and external-authority adapters.

## Development

CEDAR backend development uses Java 17. From a configured CEDAR workspace:

```bash
export CEDAR_HOME="$HOME/CEDAR"
source "$CEDAR_HOME/cedar-profile-native-develop.sh"
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw test
```

Use `cedar-development/ops/cedar-services.sh` to run the service with the rest of the native stack.
The canonical setup, build, test, dependency, and runtime instructions are in the
[CEDAR backend runbook](https://github.com/metadatacenter/cedar-development/blob/develop/ops/BACKEND-RUNBOOK.md).
