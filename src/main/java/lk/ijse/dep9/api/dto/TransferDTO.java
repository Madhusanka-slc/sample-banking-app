package lk.ijse.dep9.api.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;


/*@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString*/

@Data
@AllArgsConstructor
@NoArgsConstructor//note
public class TransferDTO implements Serializable {

    private String type;
    private String from;
    private String to;
    private BigDecimal amount;

}
