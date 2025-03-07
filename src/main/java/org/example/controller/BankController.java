//package org.example;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/banks")
//public class BankController {
//
//    @Autowired
//    private BankApiService bankApiService;
//
//    @GetMapping("/load-from-api")
//    public ResponseEntity<List<Bank>> loadBanksFromApi() {
//        try {
//            List<Bank> banks = bankApiService.getBanksFromApi();
//            return ResponseEntity.ok(banks);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//}
