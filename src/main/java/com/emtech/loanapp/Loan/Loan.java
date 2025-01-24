package com.emtech.loanapp.Loan;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long sn;
    private Double principalAmount;
    private Double interestRate;
    private Double interestAmount;
    private Double totalAmount;
    private String frequencyPeriod;
    private String repaymentPeriod;
    private Date disbursementDate;
    private Date nextRepaymentDate;

}
