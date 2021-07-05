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
    @Autowired LearningEvaluationRepository learningEvaluationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverLearningManagementStarted_StartEvaluation(@Payload LearningManagementStarted learningManagementStarted){

        if(!learningManagementStarted.validate()) return;
        
        LearningEvaluation learningEvaluation = new LearningEvaluation();
        learningEvaluation.setOrderNo(learningManagementStarted.getOrderNo());
        learningEvaluation.setCourseNo(learningManagementStarted.getCourseNo());
        learningEvaluation.setPhonenum(learningManagementStarted.getPhonenum());
        learningEvaluation.setBegindate(learningManagementStarted.getBegindate());
        learningEvaluation.setEnddate(learningManagementStarted.getEnddate());

        learningEvaluationRepository.save(learningEvaluation);

        System.out.println("\n\n##### listener StartEvaluation : " + learningManagementStarted.toJson() + "\n\n");
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverLearningManagementCanceled_CancelEvaluation(@Payload LearningManagementCanceled learningManagementCanceled){

        if(!learningManagementCanceled.validate()) return;
            LearningEvaluation learningEvaluation = learningEvaluationRepository.findByOrderNo(learningManagementCanceled.getOrderNo());
            learningEvaluationRepository.delete(learningEvaluation);

            System.out.println("\n\n##### listener CancelEvaluation : " + learningManagementCanceled.toJson() + "\n\n");
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
