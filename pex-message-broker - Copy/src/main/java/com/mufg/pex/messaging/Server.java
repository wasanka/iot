package com.mufg.pex.messaging;

import com.mufg.pex.messaging.handler.*;
import com.mufg.pex.messaging.util.Cluster;
import com.mufg.pex.messaging.util.Environment;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Server {

    public static final String ID = Environment.get("HOSTNAME", "broker-01");

    public static void main(String[] args) throws Exception {

        Cluster.getInstance();

        org.h2.tools.Server s = org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", Environment.get("DB_CONSOLE_PORT", "8081"));
        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(Environment.get("BROKER_PORT", "8000"))), 0);

        server.createContext("/", new IndexHandler());
        server.createContext("/static", new StaticFileHandler());
        server.createContext("/status", new StatusHandler());
        server.createContext("/queue", new QueueHandler());
        server.createContext("/message", new MessageHandler());

        server.setExecutor(null); // creates a default executor
        s.start();
        server.start();
    }


}