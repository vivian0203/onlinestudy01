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
    @Autowired LearningManagementRepository learningManagementRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCourseOrderCanceled_CancelLearningManagement(@Payload CourseOrderCanceled courseOrderCanceled){

        if(!courseOrderCanceled.validate()) return;
        
           LearningManagement learningManagement = learningManagementRepository.findByOrderNo(courseOrderCanceled.getOrderNo());
           learningManagementRepository.delete(learningManagement);

        
        System.out.println("\n\n##### listener CancelLearningManagement : " + courseOrderCanceled.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCourseOrdered_StartLearningManagement(@Payload CourseOrdered courseOrdered){

        if(!courseOrdered.validate()) return;
        
          LearningManagement learningManagement = new LearningManagement();
          learningManagement.setOrderNo(courseOrdered.getOrderNo());
          learningManagement.setCourseNo(courseOrdered.getCourseNo());
          learningManagement.setOrderdate(courseOrdered.getOrderdate());
          learningManagement.setBegindate(courseOrdered.getBegindate());
          learningManagement.setEnddate(courseOrdered.getEnddate());
          learningManagement.setPhonenum(courseOrdered.getPhonenum());

          learningManagementRepository.save(learningManagement);
        
        System.out.println("\n\n##### listener StartLearningManagement : " + courseOrdered.toJson() + "\n\n");
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
