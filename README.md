# FacadeServer

This is a headless (i.e. no graphics) application that runs the game as a server instance.
It can be administrated through a REST-based interface or a simple frontend that wraps around these API methods.
Administration comprises starting/stopping the host, configuring modules and the like.

In-game management (e.g. removing users from a running game) should be performed through users with admin rights.

### Setting up from source

This facade can be set up using Gradle as explained in the [Terasology wiki](https://github.com/MovingBlocks/Terasology/wiki/Codebase-Structure#facades).
Here is a step-by-step summary:
* `cd` to the root of your Terasology workspace (the directory where you cloned the main repository [MovingBlocks/Terasology](https://github.com/MovingBlocks/Terasology)).
* Execute `./gradlew fetchFacadeServer`; it will automatically clone this repository in the `facades/Server` directory.
* Keep in mind that while the default branch on GitHub is `develop`, Gradle pulls the `master` branch. So to use the latest available version or work with it, you may consider switching to the `develop` branch:
    * `cd facades/Server`
    * `git checkout develop`
    * `cd ../..`
* Run `./gradlew jar`
* To start a server using FacadeServer, execute `./gradlew facades:Server:run`.

### API documentation

The web server exposes access to the various resources both with an HTTP REST API and over WebSocket.
More information is available in the docs.md file at the root of this repository.
Also, here is a [direct link](http://petstore.swagger.io/?url=https://raw.githubusercontent.com/gianluca-nitti/FacadeServer/rest-methods/swagger.json#/) to view online the Swagger/OpenAPI specification of the HTTP API.

### Related repositories
[Here](https://github.com/gianluca-nitti/FacadeServer-frontend) is the code for a web and mobile frontend to FacadeServer.

### License

This module is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
