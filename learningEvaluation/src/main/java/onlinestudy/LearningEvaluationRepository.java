package onlinestudy;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="learningEvaluations", path="learningEvaluations")
public interface LearningEvaluationRepository extends PagingAndSortingRepository<LearningEvaluation, Long>{
  
   LearningEvaluation findByOrderNo(String orderNo);

}
