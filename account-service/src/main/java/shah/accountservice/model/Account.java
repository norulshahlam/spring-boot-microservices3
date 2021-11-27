package shah.accountservice.model;


import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String accountType;
    private Double balance;
    private String accountNumber;
    private Long userId;
}