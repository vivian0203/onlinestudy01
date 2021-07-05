package onlinestudy;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="smsHistories", path="smsHistories")
public interface SmsHistoryRepository extends PagingAndSortingRepository<SmsHistory, Long>{


}
