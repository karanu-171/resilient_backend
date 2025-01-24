package com.emtech.loanapp.Loan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query(value = "select * from loan", nativeQuery = true)
    List<Loan> findAllLoans();

}
