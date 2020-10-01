# minesweeper
REST API that implements the famous Minesweeper game.

## Build, Unit Test and assemble fat JAR
```
./gradlew unitTest assemble
```

## Build and Run Unit + Run Integration Tests (against an embedded DB in memory)
```
./gradlew build
```

## Configuration through environment variables
| Environment Variable       | Description                                                    | Required | Default   |
|----------------------------|----------------------------------------------------------------|----------|-----------|
| DATABASE_URL               | Fully qualified DB URL (includes user and password)            | no       | jdbc:postgresql://localhost:5432/postgres |
| DB_MAX_POOL_SIZE           | Database max pool size                                         | no       | 100       |
| DB_CONNECTION_TIMEOUT      | Database connection timeout                                    | no       | 30000     |
| DB_POOL_MIN_IDLE           | Database min number of idle connection in the pool             | no       | 10        |
| DB_POOL_IDLE_TIMEOUT       | Max time that a connection is allowed to sit idle in the pool  | no       | 600000    |
| ENVIRONMENT                | Runtime environment the API is running at                      | no       | local     | 

### Local environment
In order to ease the execution of the application in a local environment, we support dotenv (`.env`) files that, when
present, allow for the quick setting of environmental variables as if they had been defined at the OS level using
the typical `export VAR=value` mechanism. If you want to override any of the sensible defaults for any of the variables
above, you might want to create a `.env` file at the root of this project, following this sample pattern:

```
DATABASE_URL=jdbc:postgresql://someHost:9876/some_db_name
...
```

## Run the application locally

### Local PostgreSQL DB

A local installation (either physical or using Docker) of a PostgreSQL DB is required in order for this application to
function properly. See [application-local.properties](src/main/resources/application-local.properties) for the expected
host, port, user, password, etc.

In order to ease the task of having this DB up and running quickly, a Docker Compose descriptor is present at the root
of this project (see [docker-compose.yml](docker-compose.yml)). So, provided that you have both
[Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/) installed
and running, you should just run the following in your shell:

```shell script
docker-compose up -d db
``` 

and you should be good **almost** to go.

### Create the DB Schema
For various reasons, we have turned the Hibernate Auto DDL feature off, so, the very first time, you will need to run
the [create-db-schema.sql](create-db-schema.sql) SQL script in order to create the tables required by this API. One way
to do so is by connecting to your local DB using the SQL client of your preference ([TablePlus](https://tableplus.com/)
is highly recommended but anyone should work). 

### Run the API
```
./gradlew bootRun
```
or
```
./gradlew bootRun --debug-jvm
```
if you want to leave the application suspended until you connect your debugger to the port it is listening on (5005).

## Examples of manual tests against the API

### Create a game

`POST http://localhost:8080/api/games`

```
{
  "name": "Super easy game",
  "rowsCount": 2,
  "columnsCount": 2,
  "minesCount": 1
}
```

Should return something like this:

```
{
    "id": "a695626b-bc15-422c-b1a2-1b31483be2e9",
    "name": "Super easy game",
    "board": {
        "rowsCount": 2,
        "columnsCount": 2,
        "minesCount": 1,
        "cells": [
            [
                {
                    "row": 0,
                    "column": 0,
                    "mine": false,
                    "revealed": false,
                    "minesAround": 1
                },
                {
                    "row": 0,
                    "column": 1,
                    "mine": false,
                    "revealed": false,
                    "minesAround": 1
                }
            ],
            [
                {
                    "row": 1,
                    "column": 0,
                    "mine": false,
                    "revealed": false,
                    "minesAround": 1
                },
                {
                    "row": 1,
                    "column": 1,
                    "mine": true,
                    "revealed": false,
                    "minesAround": 0
                }
            ]
        ]
    }
}
```

### Update a Game

Now, if we were to attempt to "play" (and win!) the game, by updating the game/board previously created, we should do so
by issuing a request like as follows:

`PATCH http://localhost:8080/api/games/a695626b-bc15-422c-b1a2-1b31483be2e9`

```
{
    "row": 0,
    "column": 0,
    "cellUpdateAction": "REVEAL"
}
```
Similarly, if we proceed to further update the cells \[0, 1\] and \[1, 0\], we will see the following (final) payload:

```
{
    "id": "a695626b-bc15-422c-b1a2-1b31483be2e9",
    "name": "Super easy game",
    "board": {
        "rowsCount": 2,
        "columnsCount": 2,
        "minesCount": 1,
        "cells": [
            [
                {
                    "row": 0,
                    "column": 0,
                    "mine": false,
                    "revealed": true,
                    "minesAround": 1
                },
                {
                    "row": 0,
                    "column": 1,
                    "mine": false,
                    "revealed": true,
                    "minesAround": 1
                }
            ],
            [
                {
                    "row": 1,
                    "column": 0,
                    "mine": false,
                    "revealed": true,
                    "minesAround": 1
                },
                {
                    "row": 1,
                    "column": 1,
                    "mine": true,
                    "revealed": false,
                    "minesAround": 0
                }
            ]
        ]
    },
    "outcome": "WON"
}
```

For further information about this API contract, please refer to the self-hosted [Swagger UI](https://minesweeper2020.herokuapp.com/docs/api).

## Run the application on the cloud

The REST API is hosted at https://minesweeper2020.herokuapp.com. You can use any HTTP client such as Postman or play with
it using its self-contained [Swagger UI](https://minesweeper2020.herokuapp.com/docs/api).

The Open API v3 descriptor for this API can be found [here](https://minesweeper2020.herokuapp.com/docs/api-docs).

## Play the game visually!

I have forked [an open source project](https://github.com/DylanAttal/react-minesweeper) that implemented a front-end for
a REST-based Minesweeper that was pretty similar to this API. The code is [here](https://github.com/ClintEsteMadera/react-minesweeper)
and the game is also deployed in Heroku and can be accessed and played [here](https://minesweeper-ui.herokuapp.com/).

## Javascript SDK for this API

I have also created an SDK for Javascript (Browser/Node.js) which is hosted [here](https://github.com/ClintEsteMadera/minesweeper-sdk).
Please refer to that project's README to learn more about how I generated it automatically based on this API's OpenAPI spec.

It is worth noting that the aforementioned UI is effectively using this SDK, and if you peruse the commits, you will
discover that its inception was fairly smooth and painless.

## Notes on what it was required to build

I was requested to build the following (prioritized from most important to least important):

### Design and implement a documented RESTful API for the game (think of a mobile app for your API)

Not a lot of things to say on this one, it's implemented and fully functional.

### Implement an API client library for the API designed above. Ideally, in a different language, of your preference, to the one used for the API

As mentioned above, the code for the Javascript SDK is available [here](https://github.com/ClintEsteMadera/minesweeper-sdk).

### When a cell with no adjacent mines is revealed, all adjacent squares will be revealed (and repeat)

Implemented.

### Ability to 'flag' a cell with a question mark or red flag

Implemented. The UI, though, does not support the question mark, only the red flag. But the API fully supports both.

### Detect when game is over

When the game is over (either WON or LOST), the Board's resource will have a not null `outcome` property that tells the
user about that. Any further interaction with the game will be rejected.

### Persistence

The API uses PostgreSQL to persist games. Since this was a code challenge, I decided to store the full board in a `jsonb`
column, to avoid unnecessary complications. In real life, one might have to think a little bit more about how to effectively
persist this graph.

### Time tracking

The API supports two timestamps, both associated to the Game resource and updated automatically using
[JPA's Entity Listeners](JPA Entity Lifecycle Events):
- `created`: as its name suggests, it's only updated upon game creation.
- `modified`: this timestamp gets updated every time the Game object is updated.

With these two timestamps, clients can easily derive how much time the user has been playing, etc. 

### Ability to start a new game and preserve/resume the old ones

By means of being able to create / update and retrieve a Game by ID, while persisting them in the DB, they can
theoretically be resumed at any given time. 

### Ability to select the game parameters: number of rows, columns, and mines

This requirement was fully implemented.

### Ability to support multiple users/accounts

This requirement can be as sophisticated as one desires. For the purpose of this challenge, I kept it simple and just
exposed and endpoint for creating users with just a username (i.e. an e-mail address) and the games can be associated to
that username. That's it. I thought of many other complex things like providing users with an API-key, etc. but I preferred
to dedicate that time to the other things I have achieved as part of this challenge.
