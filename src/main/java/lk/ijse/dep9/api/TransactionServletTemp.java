package lk.ijse.dep9.api;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.ijse.dep9.api.dto.TransferDTO;
import lk.ijse.dep9.api.dto.TransactionDTO;

import java.io.IOException;
import java.io.StringReader;

//@WebServlet(name = "transaction-servlet", value = "/transactions/*", loadOnStartup = 0)
public class TransactionServletTemp extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getPathInfo()==null || request.getPathInfo().equals("/")){
            try {
                if(request.getContentType()==null || !request.getContentType().startsWith("application/json")){
                    throw new JsonbException("Invalid JSON");
                }

             /* (1)  String json="";
                String line=null;
                while ((line=request.getReader().readLine()) != null){
                    json +=line;
                }*/
             /*  (2) StringBuilder sb = new StringBuilder();
                request.getReader().lines().forEach(line -> sb.append(line));
                System.out.println(sb.toString());*/


                System.out.println(request.getReader().lines().reduce("",(p,c) -> p+c ));// 3 iter
                String json = request.getReader().lines().reduce("", (p, c) -> p + c);


//                JsonParser parser = Json.createParser(request.getReader());//input stream
                JsonParser parser = Json.createParser(new StringReader(json));//input stream
                parser.next();
                JsonObject jsonObj = parser.getObject();
                String transactionType = jsonObj.getString("type");

                if(transactionType.equalsIgnoreCase("withdraw")){
               /*     TransactionDTO transactionDTO = new TransactionDTO("withdraw",
                            jsonObj.getString("account"),
                            jsonObj.getJsonNumber("amount").bigDecimalValue());*/

                    TransactionDTO transactionDTO =JsonbBuilder.create().fromJson(json, TransactionDTO.class);

                    withdrawMoney(transactionDTO,response);

                }else if(transactionType.equalsIgnoreCase("transfer")){
                  /*  TransferDTO transferDTO = new TransferDTO("transfer",
                            jsonObj.getString("from"),
                            jsonObj.getString("to"),
                            jsonObj.getJsonNumber("amount").bigDecimalValue());*/

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

    private void withdrawMoney(TransactionDTO transactionDTO, HttpServletResponse response) throws IOException {

        System.out.println("Withdraw money");
        System.out.println(transactionDTO);



    }

    private void transferMoney(TransferDTO transferDTO, HttpServletResponse response) throws IOException {
        System.out.println("Transfer money");
        System.out.println(transferDTO);

    }
}
