package com.resttransfer.service;


import com.resttransfer.dao.DataBase;
import com.resttransfer.exception.CustomException;
import com.resttransfer.model.User;

import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

//http://localhost:8080/user/ivanov
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserService {

    private final DataBase DATABASE = DataBase.getDataBase();

    private static Logger LOGGER = Logger.getLogger(UserService.class);

    @GET
    @Path("/{userName}")
    public User getUserByName(@PathParam("userName") String userName) throws CustomException {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Request Received for get User by Name " + userName);
        final User user = DATABASE.getUserDAO().getUserByName(userName);
        if (user == null) {
            throw new WebApplicationException("User Not Found", Response.Status.NOT_FOUND);
        }
        return user;
    }

    @POST
    @Path("/create")
    public User createUser(User user) throws CustomException {
        if (DATABASE.getUserDAO().getUserByName(user.getUserName()) != null) {
            throw new WebApplicationException("User name already exist", Response.Status.BAD_REQUEST);
        }
        final long uId = DATABASE.getUserDAO().insertUser(user);
        return DATABASE.getUserDAO().getUserById(uId);
    }

    @PUT
    @Path("/{userId}")
    public Response updateUser(@PathParam("userId") long userId, User user) throws CustomException {
        final int updateCount = DATABASE.getUserDAO().updateUser(userId, user);
        if (updateCount == 1) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{userId}")
    public Response deleteUser(@PathParam("userId") long userId) throws CustomException {
        int deleteCount = DATABASE.getUserDAO().deleteUser(userId);
        if (deleteCount == 1) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
