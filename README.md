# RPGConsole V9.1

RPGConsole is a console RPG developed in Java as a Computer Engineering project. V9 adds the Trading Post, a regional map and battle music for normal, epic and boss encounters. V9.1 identifies each campaign by a persistent UUID, so campaigns with the same leader name have separate listings and rewards.

## Run the Windows release

Extract the Windows x64 ZIP and run `RPGConsole.cmd`. The game includes its own Java runtime.

To use the Trading Post, start the executable API JAR from the same release with Java 25. Run it in the directory where the local database should be stored:

```bat
java -jar marketplace-api-0.0.1-SNAPSHOT.jar --server.address=127.0.0.1
```

Check http://localhost:8080/api/health. In the game, visit an inn and select **[4] Posto de Trocas**. The game remains playable with the API stopped. Use the API JAR distributed with V9.1. It stores trades in a separate local database.

## Run from source

1. Use JDK 25 and import `RPGConsole` as an existing Eclipse project. Set its JRE System Library to a Java 25 installation.
2. Run `rpg.Main` with `RPGConsole` as the working directory.
3. For the Trading Post, import `marketplace-api` as an existing Maven project and run `MarketplaceApiApplication`, or run `mvnw.cmd spring-boot:run` from that directory on Windows.

The game targets Java 25. The API retains its Java 17 compilation target and was built and tested with JDK 25.

## Save compatibility

V9.1 loads format 4 saves and writes format 5, which includes the campaign ID. Back up the original save before updating.

## Validation

The V9.0.0 run of Maven `verify` passed 201 tests, including 200 HTTP concurrency rounds with two buyers. Each round produced one successful purchase, one conflict, one persisted sale and one pending reward.

Manual gameplay checks covered listing creation, purchase, reward claim, cancellation, campaign persistence after restarting, the API unavailable message, regional map navigation and all three battle tracks.

See [Trading Post documentation](RPGConsole/docs/MARKETPLACE.md) for persistence, concurrency and local deployment limits.
