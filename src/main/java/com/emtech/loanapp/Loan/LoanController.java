package com.emtech.loanapp.Loan;


import com.emtech.loanapp.Response.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("api/v1/loan")
@RestController
public class LoanController {

    @Autowired
    LoanService loanService;

    @PostMapping("add")
    public ResponseEntity<?> addLoan(@RequestBody Loan loan) {
        try {
            EntityResponse response = loanService.addLoan(loan);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception e) {
            log.error("Caught Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while adding the loan.");
        }
    }

    @GetMapping("all")
    public ResponseEntity<List<EntityResponse>> getLoan(){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(loanService.getAll());
        }catch (Exception e){
            log.info("Catched Error {} " + e);
            return null;
        }
    }

    @GetMapping("loan/{sn}")
    public ResponseEntity<?> getLoanAndSchedule(@PathVariable Long sn) {
        try {
            EntityResponse response = loanService.getLoanAndSchedule(sn);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception e) {
            log.error("Error fetching loan and schedule for sn {}: {}", sn, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching the loan and its schedule.");
        }
    }

}
