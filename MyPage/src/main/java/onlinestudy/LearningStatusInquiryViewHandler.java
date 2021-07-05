package onlinestudy;

import onlinestudy.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class LearningStatusInquiryViewHandler {


    @Autowired
    private LearningStatusInquiryRepository learningStatusInquiryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    
    public void whenLearningManagementStarted_then_CREATE_1 (@Payload LearningManagementStarted learningManagementStarted) {
        try {

            if (!learningManagementStarted.validate()) return;

            // view 객체 생성
            LearningStatusInquiry learningStatusInquiry = new LearningStatusInquiry();
            // view 객체에 이벤트의 Value 를 set 함
            learningStatusInquiry.setId(learningManagementStarted.getId());
            learningStatusInquiry.setOrderNo(learningManagementStarted.getOrderNo());
            learningStatusInquiry.setCourseNo(learningManagementStarted.getCourseNo());
            learningStatusInquiry.setPhonenum(learningManagementStarted.getPhonenum());
            learningStatusInquiry.setBegindate(learningManagementStarted.getBegindate());
            learningStatusInquiry.setEnddate(learningManagementStarted.getEnddate());

            // view 레파지 토리에 save
            learningStatusInquiryRepository.save(learningStatusInquiry);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenEvaluationScoreRegistered_then_UPDATE_1(@Payload EvaluationScoreRegistered evaluationScoreRegistered) {
        try {
            if (!evaluationScoreRegistered.validate()) return;
                // view 객체 조회
            Optional<LearningStatusInquiry> learningStatusInquiryOptional = learningStatusInquiryRepository.findById(evaluationScoreRegistered.getId());
            if( learningStatusInquiryOptional.isPresent()) {
                LearningStatusInquiry learningStatusInquiry = learningStatusInquiryOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                
                learningStatusInquiry.setScore(evaluationScoreRegistered.getScore());
                // view 레파지 토리에 save
                learningStatusInquiryRepository.save(learningStatusInquiry);
            }
            List<LearningStatusInquiry> learningStatusInquiryList = learningStatusInquiryRepository.findByOrderNo(evaluationScoreRegistered.getOrderNo());
            for(LearningStatusInquiry learningStatusInquiry : learningStatusInquiryList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                learningStatusInquiry.setScore(evaluationScoreRegistered.getScore());
                // view 레파지 토리에 save
                learningStatusInquiryRepository.save(learningStatusInquiry);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenBiddingParticipationCanceled_then_DELETE_1(@Payload LearningManagementCanceled learningManagementCanceled) {
        try {
            if (!learningManagementCanceled.validate()) return;
            // view 레파지 토리에 삭제 쿼리
            learningStatusInquiryRepository.deleteById(learningManagementCanceled.getId());
            // view 레파지 토리에 삭제 쿼리
            learningStatusInquiryRepository.deleteByOrderNo(learningManagementCanceled.getOrderNo());
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}

