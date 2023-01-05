package lk.ijse.dep9.api.dto;


import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AccountDTO implements Serializable {
    private String account;
    private String name;
    private String address;
    private BigDecimal balance;

}
