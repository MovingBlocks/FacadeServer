/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.LoggingContext;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.common.ConfigurationSubsystem;
import org.terasology.web.io.ActionResultMessageBodyWriter;
import org.terasology.web.io.gsonUtils.GsonMessageBodyHandler;
import org.terasology.web.serverAdminManagement.ServerAdminsManager;
import org.terasology.web.servlet.HttpAPIServlet;
import org.terasology.web.servlet.WsConnectionServlet;

/**
 * Main class from which the server facade is started.
 * It defines command-line usage, starts the Jetty server, and establishes connections.
 */
public final class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    private static final String[] ARGS_HELP = {"--help", "-help", "/help", "-h", "/h", "-?", "/?"};
    private static final String ARG_ENGINE_DIR = "-homedir=";
    private static final String ARG_ENGINE_SERVER_PORT = "-serverPort=";
    private static final String ARG_WAIT_MANUAL_START = "-dontStartDefault";

    private static boolean autoStart = true;

    private ServerMain() {
        // no instances
    }

    public static void main(String[] args) throws Exception {

        handleArgs(args);
        setupLogging();
        ServerAdminsManager.getInstance().loadAdminList();

        String httpPortEnv = System.getenv("HTTP_PORT");
        if (httpPortEnv == null) {
            httpPortEnv = "8080";
            logger.warn("Environment variable 'HTTP_PORT' not defined - using default {}", httpPortEnv);
        }

        Integer httpPort = Integer.valueOf(httpPortEnv);

        String httpsPortEnv = System.getenv("HTTPS_PORT");
        if (httpsPortEnv == null) {
            httpsPortEnv = "8443";
            logger.warn("Environment variable 'HTTPS_PORT' not defined - using default {}", httpsPortEnv);
        }

        String keystorePassword = System.getenv("KEYSTORE_PASSWORD");
        if (keystorePassword == null) {
            keystorePassword = "ServerKeyPassword";
            logger.warn("Environment variable 'KEYSTORE_PASSWORD' not defined - using default {}", keystorePassword);
        }

        Integer httpsPort = Integer.valueOf(httpsPortEnv);

        // this is mostly for I18nMap, but can have an influence on other
        // string formats. Note that metainfo.ftl explicitly sets the locale to
        // define the date format.
        Locale.setDefault(Locale.ENGLISH);

        Server server = createServer(httpPort, httpsPort, keystorePassword,
                new HttpAPIServlet());

        server.start();
        logger.info("Web server started on port {}!", httpPort);

        EngineRunner.getInstance().runEngine(autoStart);

        server.join();
    }

    private static void handleArgs(String[] args) {
        List<String> helpArgs = Arrays.asList(ARGS_HELP);
        Path homePath = Paths.get(""); //use current directory as default
        for (String arg: args) {
            if (helpArgs.contains(arg)) {
                printUsage();
                System.exit(0);
            } else if (arg.startsWith(ARG_ENGINE_DIR)) {
                homePath = Paths.get(arg.substring(ARG_ENGINE_DIR.length()));
            } else if (arg.startsWith(ARG_ENGINE_SERVER_PORT)) {
                System.setProperty(ConfigurationSubsystem.SERVER_PORT_PROPERTY, arg.substring(ARG_ENGINE_SERVER_PORT.length()));
            } else if (arg.equals(ARG_WAIT_MANUAL_START)) {
                autoStart = false;
            } else {
                System.err.println("Unrecognized command line argument \"" + arg + "\"");
                printUsage();
                System.exit(1);
            }
        }
        try {
            PathManager.getInstance().useOverrideHomePath(homePath);
        } catch (IOException e) {
            System.err.println("Failed to access the engine data directory: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Available command line options:");
        System.out.println(ARG_ENGINE_DIR + ": use the specified directory as the Terasology engine data directory");
        System.out.println(ARG_ENGINE_SERVER_PORT + ": use the specified port for the Terasology server");
        System.out.println(ARG_WAIT_MANUAL_START + ": do not generate and start a game with the default settings, but wait for manual setup via the web interface");
        System.out.println();
        System.out.println("The web server port (default 8080) can be overridden by setting the environment variable HTTP_PORT.");
    }

    private static void setupLogging() {
        Path path = PathManager.getInstance().getLogPath();
        if (path == null) {
            path = Paths.get("logs");
        }

        LoggingContext.initialize(path);
    }

    private static Server createServer(int httpPort, int httpsPort, String keystorePassword, Object... annotatedObjects) throws Exception {
        Server server = new Server(httpPort);

        ResourceHandler logFileResourceHandler = new ResourceHandler();
        logFileResourceHandler.setDirectoriesListed(true);
        logFileResourceHandler.setResourceBase(LoggingContext.getLoggingPath().toString());

        ContextHandler logContext = new ContextHandler("/logs"); // the server uri path
        logContext.setHandler(logFileResourceHandler);

        ResourceHandler webResourceHandler = new ResourceHandler();
        webResourceHandler.setDirectoriesListed(false);
        webResourceHandler.setBaseResource(Resource.newClassPathResource("web"));

        ContextHandler webContext = new ContextHandler("/");     // the server uri path
        webContext.setHandler(webResourceHandler);

        ResourceConfig rc = new ResourceConfig();
        rc.register(new GsonMessageBodyHandler());               // register JSON serializer
        rc.register(new ActionResultMessageBodyWriter());
        rc.register(FreemarkerMvcFeature.class);

        for (Object servlet : annotatedObjects) {
            rc.register(servlet);
        }

        KeyStore keyStore = KeyStore.getInstance("JKS");
        Path keyStorePath = PathManager.getInstance().getInstallPath().resolve("facades").resolve("Server").resolve("keystore.jks");
        File keyStoreFile = new File(keyStorePath.toString());
        try {
            keyStore.load(new FileInputStream(keyStoreFile), keystorePassword.toCharArray());
        } catch (FileNotFoundException e) {
            logger.error("Keystore file not found");
            e.printStackTrace();
        }

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keyStorePath.toString());
        sslContextFactory.setKeyStorePassword(keystorePassword);

        ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory,
                HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory());

        sslConnector.setPort(httpsPort);
        server.addConnector(sslConnector);

        ServletContextHandler jerseyContext = new ServletContextHandler(ServletContextHandler.GZIP);
        jerseyContext.setResourceBase("templates");
        jerseyContext.addServlet(new ServletHolder(new ServletContainer(rc)), "/*");
        jerseyContext.addServlet(new ServletHolder(WsConnectionServlet.class), "/ws/*");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(logContext);
        handlers.addHandler(webContext);
        handlers.addHandler(jerseyContext);

        server.setHandler(handlers);

        return server;
    }
}
