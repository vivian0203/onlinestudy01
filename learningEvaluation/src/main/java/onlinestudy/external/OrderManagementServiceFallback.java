package onlinestudy.external;

import org.springframework.stereotype.Component;

@Component
public class OrderManagementServiceFallback implements OrderManagementService{

    @Override
    public boolean registerEvaluation(String orderNo,String score){
        System.out.println("★★★★★★★★★★★Circuit breaker has been opened. Fallback returned instead.★★★★★★★★★★★");
        return false;
    }
}