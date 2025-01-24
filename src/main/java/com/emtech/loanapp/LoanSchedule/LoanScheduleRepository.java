package com.emtech.loanapp.LoanSchedule;

import com.emtech.loanapp.Loan.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoanScheduleRepository extends JpaRepository<LoanSchedule, Long> {

    @Query(value = "select * from loan_schedule where loan_sn =:sn", nativeQuery = true)
    List<LoanSchedule> findLoanAndSchedule(Long sn);

}
