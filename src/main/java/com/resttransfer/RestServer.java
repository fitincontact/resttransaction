package com.resttransfer;

import com.resttransfer.dao.DataBase;
import com.resttransfer.service.AccountService;
import com.resttransfer.service.ServiceExceptionMapper;
import com.resttransfer.service.TransactionService;
import com.resttransfer.service.UserService;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class RestServer {

    private static Logger LOGGER = Logger.getLogger(RestServer.class);

    public static void main(String[] args) throws Exception {
        startDataBase();
        startService();
    }

    private static void startDataBase() throws Exception {
        LOGGER.info("Preparing DataBase");
        DataBase h2DaoFactory = DataBase.getDataBase();
        h2DaoFactory.insertTestData();
        LOGGER.info("Preparing DataBase Complete");
    }

    private static void startService() throws Exception {
        LOGGER.info("Start Service");
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitParameter("jersey.config.server.provider.classnames",
                UserService.class.getCanonicalName() + "," + AccountService.class.getCanonicalName() + ","
                        + ServiceExceptionMapper.class.getCanonicalName() + ","
                        + TransactionService.class.getCanonicalName());

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            LOGGER.info("STOP & DESTROY for Server, Exception ", e);
            server.stop();
            server.destroy();
        }
    }

}
