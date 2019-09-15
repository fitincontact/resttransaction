package com.resttransfer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resttransfer.dao.DataBase;
import com.resttransfer.service.AccountService;
import com.resttransfer.service.ServiceExceptionMapper;
import com.resttransfer.service.TransactionService;
import com.resttransfer.service.UserService;

import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class TestService {
    private static Server SERVER = null;
    private static PoolingHttpClientConnectionManager CONNECTIONMANAGER
            = new PoolingHttpClientConnectionManager();

    protected static HttpClient HTTPCLIENT;
    protected static DataBase DATABASE = DataBase.getDataBase();
    protected ObjectMapper OBJECTMAPPER = new ObjectMapper();
    protected URIBuilder URIBUILDER =
            new URIBuilder().setScheme("http").setHost("localhost:8081");


    @BeforeClass
    public static void setup() throws Exception {
        DATABASE.insertTestData();
        startServer();
        CONNECTIONMANAGER.setDefaultMaxPerRoute(100);
        CONNECTIONMANAGER.setMaxTotal(200);
        HTTPCLIENT = HttpClients.custom()
                .setConnectionManager(CONNECTIONMANAGER)
                .setConnectionManagerShared(true)
                .build();
    }

    @AfterClass
    public static void closeClient() {
        HttpClientUtils.closeQuietly(HTTPCLIENT);
    }

    private static void startServer() throws Exception {
        if (SERVER == null) {
            SERVER = new Server(8081);
            ServletContextHandler context =
                    new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            SERVER.setHandler(context);
            ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");
            servletHolder.setInitParameter("jersey.config.server.provider.classnames",
                    UserService.class.getCanonicalName() + "," +
                            AccountService.class.getCanonicalName() + "," +
                            ServiceExceptionMapper.class.getCanonicalName() + "," +
                            TransactionService.class.getCanonicalName());
            SERVER.start();
        }
    }
}