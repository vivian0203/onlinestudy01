package onlinestudy;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LearningStatusInquiryRepository extends CrudRepository<LearningStatusInquiry, Long> {

    List<LearningStatusInquiry> findByOrderNo(String orderNo);
  

        void deleteByOrderNo(String orderNo);
   
}