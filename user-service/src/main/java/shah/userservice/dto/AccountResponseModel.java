package shah.userservice.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AccountResponseModel {
  private String accountType;
  private Double balance;
  private String accountNumber;
  private Long userId;
}
