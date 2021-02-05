# Doomsday

A java based [gor](https://github.com/buger/gor) middleware app that allows you to verify if your testing server is sending (more or less) the same responses as your prod server.

## Steps

1. Clone the repo with
    ```shell
    git clone git@github.com:rajat19/Doomsday.git
    ```
2. move inside the repo
    ```shell
    cd Doomsday
    ```
3. Download the latest Gor binary from https://github.com/buger/gor/releases (they provide precompiled binaries for Windows, Linux x64 and Mac OS), 
   or you can compile by yourself see[Compilation](https://github.com/buger/goreplay/wiki/Compilation).


4. Build the project with
    ```shell
    ./scripts/compile.sh
    ```

5. Run the project with
    ```shell
    ./scripts/run.sh
    ```
   You can update go-replay options by editing `scripts/run.sh`

## What does it do?

GoReplay will send each request that was sent to the production server to your staging server.
This middleware will compare the response from the acceptation server to that of the production server.

The middleware allows you to write [rules](/src/main/java/com/paradox/geeks/doomsday/rules/Rule.java) that

* [prevent certain requests](/src/main/java/com/paradox/geeks/doomsday/rules/IgnoreStaticRule.java) from being replicated to the auth server
* modify the replay request. To [replace auth headers](/src/main/java/com/paradox/geeks/doomsday/rules/StoreAuthRule.java) for example.
* modify the original or replay response.
    * To [overwrite the replay response's date](/src/main/java/com/paradox/geeks/doomsday/rules/IgnoreDateDifferenceRule.java) with the one from the original.
    * To [unchunk](/src/main/java/com/paradox/geeks/doomsday/rules/UnchunkRule.java) the response.
    * To [unzip](/src/main/java/com/paradox/geeks/doomsday/rules/GunzipRule.java) the response so that they can be better compared.

Gor sends the requests and responses in an undefined order to the middleware, but your rule's methods will always be called with the data you need (the middleware stores data if needed and waits for the rest to arrive).

## How do I use it?

At the moment you're expected to write your own rules and add the to the constructor call in [Application.java](/src/main/java/com/paradox/geeks/doomsday/Application.java) 
if there is a need I could make it more configurable and let you specify what rules to load in the command line.