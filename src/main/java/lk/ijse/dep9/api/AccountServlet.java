package lk.ijse.dep9.api;

import jakarta.annotation.Resource;
import jakarta.json.JsonException;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.api.dto.AccountDTO;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet(name = "account-servlet", value = "/accounts/*",loadOnStartup = 0)//class load and make instance when start
public class AccountServlet extends HttpServlet {

   @Resource(lookup = "java:comp/env/jdbc/dep9-boc")
    private DataSource pool;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getPathInfo()==null || request.getPathInfo().equals("/")){
            try {
                if(request.getContentType()==null || !request.getContentType().startsWith("application/json")){
                    throw new JsonException("Invalid JSON");
                }
                AccountDTO accountDTO = JsonbBuilder.create().fromJson(request.getReader(), AccountDTO.class);
                createAccount(accountDTO,response);
            } catch (JsonException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());
            }

        }else {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }
    private void createAccount(AccountDTO accountDTO, HttpServletResponse response) throws IOException {
        System.out.println("Create a new account");
        System.out.println(accountDTO);

        try {
            Connection connection = pool.getConnection();
            System.out.println("Create Pool");
            if (accountDTO.getName()==null || !accountDTO.getName().matches("[A-Za-z ]+")){
                throw new JsonException("Invalid account holder name");

            }else if(accountDTO.getAddress()==null || accountDTO.getAddress().isBlank()){
                throw new JsonException("Invalid account holder address");
            }
            accountDTO.setAccount(UUID.randomUUID().toString());
            accountDTO.setBalance(BigDecimal.ZERO);

            PreparedStatement stm = connection.prepareStatement("INSERT INTO  Account (account_number,holder_name,holder_address) VALUES (?,?,?)");
            stm.setString(1,accountDTO.getAccount());
            stm.setString(2,accountDTO.getName());
            stm.setString(3,accountDTO.getAddress());

            if(stm.executeUpdate()==1){
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setContentType("application/json");
                JsonbBuilder.create().toJson(accountDTO,response.getWriter());

            }else {
                throw new JsonException("Something went wrong ,try again");
            }
        } catch (SQLException e) {
           e.printStackTrace();
           response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
        }
    }
}
