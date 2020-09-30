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
| SPRING_DATASOURCE_USERNAME | DB Username                                                    | no       | postgres  |
| SPRING_DATASOURCE_PASSWORD | DB Password                                                    | no       | admin     |
| DB_MAX_POOL_SIZE           | Database max pool size                                         | no       | 100       |
| DB_CONNECTION_TIMEOUT      | Database connection timeout                                    | no       | 30000     |
| DB_POOL_MIN_IDLE           | Database min number of idle connection in the pool             | no       | 100       |
| DB_POOL_IDLE_TIMEOUT       | Max time that a connection is allowed to sit idle in the pool  | no       | 600000    |
| ENVIRONMENT                | Runtime environment the API is running at                      | no       | local     | 

### Local environment
In order to ease the execution of the application in a local environment, we support dotenv (`.env`) files that, when
present, allow for the quick setting of environmental variables as if they had been defined at the OS level using
the typical `export VAR=value` mechanism. If you want to override any of the sensible defaults for any of the variables
above, you might want to create a `.env` file at the root of this project, following this sample pattern:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://someHost:9876/some_db_name
SPRING_DATASOURCE_USERNAME=some-funky-user
SPRING_DATASOURCE_PASSWORD=password123-is-not-secure!
...
```

## Run the application
**NOTE:** It is assumed that prior to the execution of the app, the configured database must be up and running.

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
