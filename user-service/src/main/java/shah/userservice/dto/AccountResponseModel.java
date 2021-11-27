package shah.userservice.dto;

import lombok.Data;

@Data
public class AccountResponseModel {
  private String accountType;
  private Double balance;
  private String accountNumber;
  private Long userId;
}
