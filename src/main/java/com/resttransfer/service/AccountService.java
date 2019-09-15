package com.resttransfer.service;


import com.resttransfer.dao.DataBase;
import com.resttransfer.exception.CustomException;
import com.resttransfer.model.Account;
import static com.resttransfer.utils.Utils.getBigDecimalScale2Down;

import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;

//http://localhost:8080/account/1
//http://localhost:8080/account/2
@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
public class AccountService {

    private final DataBase daoFactory = DataBase.getDataBase();

    private static final Logger logger = Logger.getLogger(AccountService.class);

    /**
     * Find by account id
     * @param accountId
     * @return
     * @throws CustomException
     */
    @GET
    @Path("/{accountId}")
    public Account getAccount(@PathParam("accountId") long accountId) throws CustomException {
        return daoFactory.getAccountDAO().getAccountById(accountId);
    }

    /**
     * Find balance by account Id
     * @param accountId
     * @return
     * @throws CustomException
     */
    @GET
    @Path("/{accountId}/balance")
    public BigDecimal getBalance(@PathParam("accountId") long accountId) throws CustomException {
        final Account account = daoFactory.getAccountDAO().getAccountById(accountId);

        if(account == null){
            throw new WebApplicationException("Account not found", Response.Status.NOT_FOUND);
        }
        return account.getBalance();
    }

    /**
     * Create Account
     * @param account
     * @return
     * @throws CustomException
     */
    @PUT
    @Path("/create")
    public Account createAccount(Account account) throws CustomException {
        final long accountId = daoFactory.getAccountDAO().createAccount(account);
        return daoFactory.getAccountDAO().getAccountById(accountId);
    }

    /**
     * Deposit amount by account Id
     * @param accountId
     * @param amount
     * @return
     * @throws CustomException
     */
    @PUT
    @Path("/{accountId}/deposit/{amount}")
    public Account deposit(@PathParam("accountId") long accountId,@PathParam("amount") BigDecimal amount) throws CustomException {

        if (amount.compareTo(getBigDecimalScale2Down("0")) <=0){
            throw new WebApplicationException("Invalid Deposit amount", Response.Status.BAD_REQUEST);
        }

        daoFactory.getAccountDAO().updateAccountBalance(accountId,amount.setScale(4, RoundingMode.HALF_EVEN));
        return daoFactory.getAccountDAO().getAccountById(accountId);
    }

    @PUT
    @Path("/{accountId}/withdraw/{amount}")
    public Account withdraw(@PathParam("accountId") long accountId,@PathParam("amount") BigDecimal amount) throws CustomException {

        if (amount.compareTo(getBigDecimalScale2Down("0")) <=0){
            throw new WebApplicationException("Invalid Deposit amount", Response.Status.BAD_REQUEST);
        }
        BigDecimal delta = amount.negate();
        if (logger.isDebugEnabled())
            logger.debug("Withdraw service: delta change to account  " + delta + " Account ID = " +accountId);
        daoFactory.getAccountDAO().updateAccountBalance(accountId,delta.setScale(4, RoundingMode.HALF_EVEN));
        return daoFactory.getAccountDAO().getAccountById(accountId);
    }

    @DELETE
    @Path("/{accountId}")
    public Response deleteAccount(@PathParam("accountId") long accountId) throws CustomException {
        int deleteCount = daoFactory.getAccountDAO().deleteAccountById(accountId);
        if (deleteCount == 1) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}

