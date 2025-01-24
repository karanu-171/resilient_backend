package com.emtech.loanapp.Loan;


import com.emtech.loanapp.LoanSchedule.LoanSchedule;
import com.emtech.loanapp.LoanSchedule.LoanScheduleRepository;
import com.emtech.loanapp.Response.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanScheduleRepository loanScheduleRepository;

    private void generateLoanSchedule(Loan loan) {
        List<LoanSchedule> schedules = new ArrayList<>();
        double installmentAmount = loan.getTotalAmount() / Integer.parseInt(loan.getRepaymentPeriod());
        double interestAmount = loan.getInterestAmount() / Integer.parseInt(loan.getRepaymentPeriod());
        double principalAmount = loan.getPrincipalAmount() / Integer.parseInt(loan.getRepaymentPeriod());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(loan.getDisbursementDate());

        for (int i = 0; i < Integer.parseInt(loan.getRepaymentPeriod()); i++) {
            LoanSchedule schedule = new LoanSchedule();
            schedule.setInstallmentAmount(installmentAmount);
            schedule.setInterestAmount(interestAmount);
            schedule.setPrincipalAmount(principalAmount);
            schedule.setLoan(loan);

            if ("MONTHS".equalsIgnoreCase(loan.getFrequencyPeriod())) {
                calendar.add(Calendar.MONTH, 1);
            } else if ("WEEKS".equalsIgnoreCase(loan.getFrequencyPeriod())) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }

            schedule.setRepaymentDate(calendar.getTime());

            schedules.add(schedule);
        }

        loanScheduleRepository.saveAll(schedules);
    }

    public EntityResponse addLoan(Loan loan) {
        log.info("Attempting to add loan: {}", loan);
        EntityResponse response = new EntityResponse();
        try {
            double interestAmount = (loan.getPrincipalAmount() * loan.getInterestRate()) / 100;
            double totalAmount = loan.getPrincipalAmount() + interestAmount;

            loan.setInterestAmount(interestAmount);
            loan.setTotalAmount(totalAmount);
            loan.setDisbursementDate(new Date());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            if ("MONTHS".equalsIgnoreCase(loan.getFrequencyPeriod())) {
                calendar.add(Calendar.MONTH, 1);
            } else if ("WEEKS".equalsIgnoreCase(loan.getFrequencyPeriod())) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }

            loan.setNextRepaymentDate(calendar.getTime());

            Loan savedLoan = loanRepository.save(loan);

            generateLoanSchedule(savedLoan);

            response.setMessage("Loan added successfully.");
            response.setStatusCode(HttpStatus.CREATED.value());
            response.setEntity(savedLoan);

            log.info("Loan added successfully: {}", savedLoan);
        } catch (Exception e) {
            log.error("Error while adding loan: {}", e.getMessage(), e);
            response.setMessage("Failed to add loan. Please try again.");
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        }
        return response;
    }

    public List<EntityResponse> getAll() {
        List<EntityResponse> responses = new ArrayList<>();
        try {
            List<Loan> loans = loanRepository.findAllLoans();
            log.info("loans: {}", loans);

            if (loans.isEmpty()) {
                EntityResponse response = new EntityResponse<>();
                response.setMessage("No Loans found.");
                response.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                response.setEntity(null);
                responses.add(response);
            } else {
                EntityResponse response = new EntityResponse<>();
                response.setMessage("Loans fetched successfully.");
                response.setStatusCode(HttpStatus.OK.value());
                response.setEntity(loans);
                responses.add(response);
            }

            log.info("Loan fetched successfully");
        } catch (Exception e) {
            log.error("Error while fetching loan: {}", e.getMessage(), e);

            EntityResponse response = new EntityResponse<>();
            response.setMessage("Failed to fetch loan. Please try again.");
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            responses.add(response);
        }
        return responses;
    }


    public EntityResponse getLoanAndSchedule(Long sn) {
        EntityResponse response = new EntityResponse();
        try {
            // Fetch loan by SN
            Optional<Loan> loanOptional = loanRepository.findById(sn);
            if (loanOptional.isEmpty()) {
                response.setMessage("Loan not found for SN: " + sn);
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
                response.setEntity(null);
                return response;
            }

            Loan loan = loanOptional.get();

            List<LoanSchedule> loanSchedules = loanScheduleRepository.findLoanAndSchedule(sn);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("loan", loan);
            responseData.put("loanSchedule", loanSchedules);

            response.setMessage("Loan and schedule fetched successfully.");
            response.setStatusCode(HttpStatus.OK.value());
            response.setEntity(responseData);

            log.info("Loan and schedule fetched successfully for SN: {}", sn);
        } catch (Exception e) {
            log.error("Error fetching loan and schedule for SN {}: {}", sn, e.getMessage(), e);
            response.setMessage("Failed to fetch loan and schedule. Please try again.");
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        }
        return response;
    }

}
