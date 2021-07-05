package onlinestudy;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="learningManagements", path="learningManagements")
public interface LearningManagementRepository extends PagingAndSortingRepository<LearningManagement, Long>{

    LearningManagement findByOrderNo(String orderNo);
}
