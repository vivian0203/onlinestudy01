package onlinestudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class OrderManagementController {

   @Autowired
   OrderManagementRepository orderManagementRepository;

   @RequestMapping(value = "/orderManagements/registerEvaluation",
      method = RequestMethod.GET,
      produces = "application/json;charset=UTF-8")
   
  
      public boolean registerEvaluation(HttpServletRequest request, HttpServletResponse response) {
      boolean status = false;

      String orderNo = String.valueOf(request.getParameter("orderNo"));
      
      System.out.println("@@@@@@@@@@@@@@@@@evaluation orderNo@" + request.getParameter("orderNo"));
      System.out.println("@@@@@@@@@@@@@@@@@evaluation score@" + request.getParameter("score"));
      
      OrderManagement orderManagement = orderManagementRepository.findByOrderNo(orderNo);

     // if(orderManagement.orderNo()){
           orderManagement.setScore(request.getParameter("score"));
           orderManagementRepository.save(orderManagement);
           status = true;
     // }

      return status;
   }

}
