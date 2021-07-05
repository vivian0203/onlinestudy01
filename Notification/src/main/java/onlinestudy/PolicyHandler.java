package onlinestudy;

import onlinestudy.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired SmsHistoryRepository smsHistoryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverLearningManagementCanceled_SendSms(@Payload LearningManagementCanceled learningManagementCanceled){

        if(!learningManagementCanceled.validate()) return;
        
          SmsHistory smsHistory = new SmsHistory();
          smsHistory.setPhonenum(learningManagementCanceled.getPhonenum());
          smsHistory.setStatus("학습관리취소");

          smsHistoryRepository.save(smsHistory);

        
        System.out.println("\n\n##### listener SendSms : " + learningManagementCanceled.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverLearningManagementStarted_SendSms(@Payload LearningManagementStarted learningManagementStarted){

        if(!learningManagementStarted.validate()) return;
        
          SmsHistory smsHistory = new SmsHistory();
          smsHistory.setPhonenum(learningManagementStarted.getPhonenum());
          smsHistory.setStatus("학습관리시작");

          smsHistoryRepository.save(smsHistory);

        
        System.out.println("\n\n##### listener SendSms : " + learningManagementStarted.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCourseOrdered_SendSms(@Payload CourseOrdered courseOrdered){

        if(!courseOrdered.validate()) return;
        
          SmsHistory smsHistory = new SmsHistory();
          smsHistory.setPhonenum(courseOrdered.getPhonenum());
          smsHistory.setStatus("강좌주문");

          smsHistoryRepository.save(smsHistory);

        
        System.out.println("\n\n##### listener SendSms : " + courseOrdered.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCourseOrderCanceled_SendSms(@Payload CourseOrderCanceled courseOrderCanceled){

        if(!courseOrderCanceled.validate()) return;
        
          SmsHistory smsHistory = new SmsHistory();
          smsHistory.setPhonenum(courseOrderCanceled.getPhonenum());
          smsHistory.setStatus("강좌주문취소");

          smsHistoryRepository.save(smsHistory);

        System.out.println("\n\n##### listener SendSms : " + courseOrderCanceled.toJson() + "\n\n");
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
