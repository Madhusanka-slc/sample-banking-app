package lk.ijse.dep9.api.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;


@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransactionDTO implements Serializable {
    private String type;
    private String account;
    private BigDecimal amount;
}
