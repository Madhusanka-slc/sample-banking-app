package lk.ijse.dep9.api;

import jakarta.annotation.Resource;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.ijse.dep9.api.dto.AccountDTO;
import lk.ijse.dep9.api.dto.TransactionDTO;
import lk.ijse.dep9.api.dto.TransferDTO;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;

//@WebServlet(name = "transaction-servlet", value = "/transactions/*", loadOnStartup = 0)
public class TransactionServlet2 extends HttpServlet {
    @Resource(lookup = "java:comp/env/jdbc/dep9-boc")
    private DataSource pool;
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getPathInfo()==null || request.getPathInfo().equals("/")){
            try {
                if(request.getContentType()==null || !request.getContentType().startsWith("application/json")){
                    throw new JsonbException("Invalid JSON");
                }

                String json = request.getReader().lines().reduce("", (p, c) -> p + c);
                System.out.println(json);

                JsonParser parser = Json.createParser(new StringReader(json));//input stream
                parser.next();
                JsonObject jsonObj = parser.getObject();
                String transactionType = jsonObj.getString("type");

                if(transactionType.equalsIgnoreCase("withdraw")){

                    TransactionDTO transactionDTO =JsonbBuilder.create().fromJson(json, TransactionDTO.class);
                    withdrawMoney(transactionDTO,response);

                } else if (transactionType.equalsIgnoreCase("deposit")) {

                    TransactionDTO transactionDTO =JsonbBuilder.create().fromJson(json, TransactionDTO.class);
                    System.out.println(transactionDTO);
                    depositMoney(transactionDTO,response);

                } else if(transactionType.equalsIgnoreCase("transfer")){

                    TransferDTO transferDTO=JsonbBuilder.create().fromJson(json,TransferDTO.class);
                    transferMoney(transferDTO,response);


                }else {
                    throw new JsonbException("Invalid JSON");
                }

            } catch (JsonException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid JSON");
            }

        }else {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }


    private void depositMoney(TransactionDTO transactionDTO, HttpServletResponse response) throws IOException {
        // 1 validation
        try {
            System.out.println(transactionDTO.getAccount());
            if(transactionDTO.getAccount()==null || !transactionDTO.getAccount().
                    matches("^([A-Fa-f0-9]{8}(-[A-Fa-f0-9]{4}){3}-[A-Fa-f0-9]{12})$")){
                throw new JsonException("Invalid account number");

            } else if (transactionDTO.getAmount() == null ||
                transactionDTO.getAmount().compareTo(new BigDecimal(100))<0){
                throw new JsonException("Invalid amount");

                }

            Connection connection = pool.getConnection();
            PreparedStatement stm = connection.prepareStatement("SELECT  * FROM  Account WHERE  account_number = ?");
            stm.setString(1,transactionDTO.getAccount());
            ResultSet rst = stm.executeQuery();
            if(!rst.next()){

                throw new JsonException("Not exist account");

            }
            try {
                //Buffer
                connection.setAutoCommit(false);
                PreparedStatement stmUpdate = connection.
                        prepareStatement("UPDATE Account SET balance = balance + ? WHERE  account_number = ?");

                stmUpdate.setBigDecimal(1,transactionDTO.getAmount());
                stmUpdate.setString(2,transactionDTO.getAccount());
                if (stmUpdate.executeUpdate() !=1) {
                    throw new SQLException("Failed to update the balance");
                }

//                if(true){
//                    System.out.println("something");
//                }

                PreparedStatement stmNewTransaction = connection.
                        prepareStatement("INSERT INTO Transaction (account, type, description, amount, date) VALUES (?, ?, ?, ?, ? )");

                stmNewTransaction.setString(1,transactionDTO.getAccount());
                stmNewTransaction.setString(2,"CREDIT");
                stmNewTransaction.setString(3,"Deposit");
                stmNewTransaction.setBigDecimal(4,transactionDTO.getAmount());
                stmNewTransaction.setTimestamp(5,new Timestamp(new Date().getTime()));

                if (stmNewTransaction.executeUpdate() !=1) {
                    throw new SQLException("Failed to add a transaction record");
                }

                // Flush - Persist
                connection.commit();
                ResultSet resultSet = stm.executeQuery();
                resultSet.next();
                String name = resultSet.getString("holder_name");
                String address = resultSet.getString("holder_address");
                BigDecimal balance = resultSet.getBigDecimal("balance");

                AccountDTO accountDTO = new AccountDTO(transactionDTO.getAccount(), name, address, balance);

                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setContentType("application/json");
                JsonbBuilder.create().toJson(accountDTO, response.getWriter());

            }catch (Throwable t){

                // Go to prev state
                connection.rollback();
                t.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Failed to deposit the money");

            }finally {
                connection.setAutoCommit(true);
            }

            connection.close();

        } catch (JsonException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Failed to deposit the money to account");
        }
    }


    private void withdrawMoney(TransactionDTO transactionDTO, HttpServletResponse response) throws IOException {


    }

    private void transferMoney(TransferDTO transferDTO, HttpServletResponse response) throws IOException {

    }


}
