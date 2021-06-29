# Image Service for Art Gallery App

The image microservice for an Art Gallery App. Using Spring Boot, GCP, Tika and various other frameworks & libs.  
Work in progress

## Design

![Design Picture](Initial_Design.png?raw=true "Design")

## Configuration

### Defaults

Defaults are provided for all profiles in `src/main/resources/application[-profile].properties`.

### Override

In addition to the standard spring boot locations, it is possible to provide overrides in different locations (depending
on the profile).

#### Dev Profile

If you have additional properties to set (logging level) or are unhappy with the default (e.g. db port), you can place
a `dev.yml` in the root directory. These values will be put on top of the "property resolution stack".

#### Test

Currently, no overrides are provided. Configuration is done automatically via testcontainers and lifecycle hooks.

#### Prod (K8S)

Production profile allows `file:/etc/config/properties/application.yml` and `configtree:/etc/secrets/properties/`. The
secret properties are usually files like `spring.datasource.passsword` which are included by kubernetes as secrets

Templates for required confis and secrets can be found in `./k8s/templates`.

## Dependencies

The following services are expected to run and be configured.

1. Postgres DB
2. Cloud Storage
3. PubSub
4. Redis
5. Keycloak

The docker compose file in `docker/docker-compose.yml` provides the defaults for the required services. This file is
also used by the integration tests. No ports are necessary, as the ambassador container acts as a proxy.  
Additionally, `docker/docker-compose.dev.yml` contains useful defaults for the local dev environment (stable ports,
persistent db, db admin tool, ...).  
It can be started with the
command `docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d [service1 service2]`. Single components
can be configured via configuration (see `dev.yml` above) to use the real service. Make sure to adjust the config to
match the service addresses (real or local). The default settings for the dev profile match the docker-compose file.

## How To Test

Integration Tests (*IT) require docker, since testcontainers are setup via docker-compose. Standard testing procedures
apply.

## JWT from Dev Keycloak

The following command will create an access token in the dev landscape (if docker-compose is used):

`curl --insecure -X POST http://localhost:7072/auth/realms/image-service/protocol/openid-connect/token     --user image-service:secret -H 'content-type: application/x-www-form-urlencoded' -d 'username=alice&password=alice&grant_type=password' | jq --raw-output '.access_token'`