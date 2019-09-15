package com.resttransfer.service;


import com.resttransfer.dao.DataBase;
import com.resttransfer.exception.CustomException;
import com.resttransfer.model.UserTransaction;
import org.apache.log4j.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Currency;

@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionService {

    private final DataBase DATABASE = DataBase.getDataBase();
    private static Logger LOGGER = Logger.getLogger(TransactionService.class);

    /**
     * Transfer fund between two accounts.
     * @param transaction
     * @return
     * @throws CustomException
     */
    //curl --header "Content-Type: application/json" --request POST --data '{"currencyCode":"USD","amount":1.00,"fromAccountId":2,"toAccountId":1}' http://localhost:8080/transaction
    @POST
    public Response transferFund(UserTransaction transaction) throws CustomException {

        String currency = transaction.getCurrencyCode();
        if (validateCurrencyCode(currency)) {
            int updateCount = DATABASE.getAccountDAO().transferAccountBalance(transaction);
            if (updateCount == 2) {
                return Response.status(Response.Status.OK).build();
            } else {
                throw new WebApplicationException("Transaction failed", Response.Status.BAD_REQUEST);
            }
        } else {
            throw new WebApplicationException("Currency Code Invalid ", Response.Status.BAD_REQUEST);
        }

    }

    private boolean validateCurrencyCode(String inputCcyCode) {
        try {
            return Currency.getInstance(inputCcyCode).getCurrencyCode().equals(inputCcyCode);
        } catch (Exception e) {
            LOGGER.warn("Currency Code is invalid ", e);
        }
        return false;
    }

}

