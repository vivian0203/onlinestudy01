

### Repositories

- https://github.com/vivian0203/onlinestudy.git



### Table of contents

- [서비스 시나리오]

  - [기능적 요구사항]

  - [비기능적 요구사항]

  - [Microservice명]

- [분석/설계]

- [구현]

  - [DDD 의 적용]

  - [폴리글랏 퍼시스턴스]

  - [동기식 호출 과 Fallback 처리]

  - [비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트]

- [운영]

  - [Deploy]

  - [Autoscale (HPA)]

  - [Config Map]

  - [Zero-Downtime deploy (Readiness Probe)] 

  - [Self-healing (Liveness Probe)]

  - [Circuit Breaker]

# 서비스 시나리오

### 기능적 요구 사항

```
• 학생이 강좌를 검색하여 주문한다.
• 학습매니저가 선정되고 학습관리를 시작한다.
• 학습매니저가 학습평가를 진행한다.
• 학습종료시 학습매니저가 최종학습평가를 등록한다.
• 최종학습평가점수는 주문관리에도 반드시 등록해야만 한다.
• 학생이 강좌주문을 취소하면 학습관리와 학습평가도 취소된다.
• 강좌주문, 강좌주문취소, 학습관리, 학습관리취소는 학생에게 SMS를 발송한다.
• 학생은 학습현황을 조회 할 수 있다.

```

### 비기능적 요구 사항

```
1. 트랜잭션
  - 평가결과점수가 등록되면 주문관리에도 평가결과점수가 반드시 등록되어야 한다. (Sync 호출)
2. 장애격리
  - 학습평가 기능이 수행되지 않더라도 주문관리, 학습관리 기능은 365일 24시간 받을 수 있어야 한다. Async (event-driven), Eventual Consistency
  - 학습관리 기능이 과중되면 사용자를 잠시 동안 받지 않고 입찰참여를 잠시후에 하도록 유도한다. Circuit breaker, fallback
3. 성능
  - 학생은 학습현황조회 화면에서 학습상태를 확인 할 수 있어야 한다.CQRS - 조회전용 서비스
```

### Microservice명

```
주문관리 – OrderManagement
학습관리 - LearningManagement
학습평가 - LearningEvaluation
문자알림이력 - Notification
학습현황조회 - MyPage
```


# 분석/설계

### AS-IS 조직 (Horizontally-Aligned)

![1  AS-IS조직](https://user-images.githubusercontent.com/84000922/122162394-7b1c0f80-ceae-11eb-95c4-8952596bb623.png)




### TO-BE 조직 (Vertically-Aligned)
![image](https://user-images.githubusercontent.com/84000893/124391212-81bdda00-dd2a-11eb-8a0a-728f6303e81b.png)




### 이벤트 도출

![image](https://user-images.githubusercontent.com/84000893/124373491-99b34080-dccd-11eb-8eed-7d87f0e1c87c.png)



### 부적격 이벤트 탈락

![image](https://user-images.githubusercontent.com/84000893/124373512-c404fe00-dccd-11eb-9c44-3b0ac6cdb633.png)

```
- 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행
- 학습매니저등록됨 : 속성 정보여서 제외
- 학습진도율관리됨 : 비지니스 로직 추가라 이번 작업에서 제외
- 강좌검색됨, 학습현황조회됨 : UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외 
```




### 액터, 커맨드 부착하여 읽기 좋게

![image](https://user-images.githubusercontent.com/84000893/124374028-b43be880-dcd2-11eb-9975-d82a8e77233b.png)


### 어그리게잇으로 묶기

![image](https://user-images.githubusercontent.com/84000893/124374036-d2094d80-dcd2-11eb-90fd-6140c6058450.png)

```
- 주문관리, 학습관리, 학습평가는 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 묶어줌
```




### 바운디드 컨텍스트로 묶기

![image](https://user-images.githubusercontent.com/84000893/124374662-40044380-dcd8-11eb-9428-675571d7b867.png)

```
도메인 서열 분리
- Core Domain: 주문관리, 학습관리-> 없어서는 안될 핵심 서비스이며, 연간 Up-time SLA 수준을 99.999% 목표, 주문관리 배포주기는 1개월 1회 미만, 학습관리 배포주기는 1주일 1회 미만
- Supporting Domain: 학습평가-> 경쟁력을 내기 위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함. 
- General Domain: Notification-> 알림서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 (핑크색으로 이후 전환할 예정)
```




### 폴리시 부착, 이동 및 컨텍스트 매핑(점선은 Pub/Sub, 실선은 Req/Resp)

![image](https://user-images.githubusercontent.com/84000893/124375146-93c45c00-dcdb-11eb-986c-6aa32ae9b1c8.png)



### 완성된 1차 모형

![image](https://user-images.githubusercontent.com/84000893/124374568-565dcf80-dcd7-11eb-8836-4c9241bef61d.png)



### 1차 완성본에 대한 기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/84000893/124518011-62a17400-de20-11eb-894e-85ee29af8a82.png)

```
1) 학생이 강좌를 주문하면 학습관리를 시작한다.
2) 학습매니저가 선정되어 학습관리를 하고 학습평가가 시작된다.
3) 학습매니저가 학습평가를 등록하면 주문관리에도 학습평가점수가 등록된다.
```




### 1차 완성본에 대한 비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/84000893/124518232-e65b6080-de20-11eb-9a81-33f5c300a3cd.png)

```
1. 트랜잭션
  - 평가결과가 등록되면 주문관리에 평가점수가 등록되어야 한다. (Sync 호출)
2. 장애격리
  - 학습평가 기능이 수행되지 않더라도 주문관리, 학습관리 기능은 365일 24시간 받을 수 있어야 한다. 
    Async (event-driven), Eventual Consistency
  - 학습관리 기능이 과중되면 사용자를 잠시 동안 받지 않고 예약을 잠시후에 하도록 유도한다.
    Circuit breaker, fallback
3. 성능
  - 학생은는 학습현황조회 화면에서 학습 상태를 확인 할 수 있어야 한다.CQRS - 조회전용 서비스
```




### 헥사고날 아키텍처 다이어그램 도출

![image](https://user-images.githubusercontent.com/84000893/124390708-c98f3200-dd27-11eb-9c04-a5113467fb3f.png)





### Git Organization / Repositories

![image](https://user-images.githubusercontent.com/84000893/124391117-03613800-dd2a-11eb-9040-246dfedc61c3.png)


# 구현:

(서비스 별 포트) 분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트 등으로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8085, 8088 이다)

```
cd orderManagement
mvn spring-boot:run

cd learningManagement
mvn spring-boot:run 

cd learningEvaluation
mvn spring-boot:run  

cd Notification
mvn spring-boot:run

cd MyPage
mvn spring-boot:run

cd gateway
mvn spring-boot:run
```

## DDD 의 적용

- (Entity 예시) 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (아래 예시는 주문관리 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다.

```
package onlinestudy;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="OrderManagement_table")
public class OrderManagement {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String orderNo;
    private String courseNo;
    private String phonenum;
    private Date orderdate;
    private Date begindate;
    private Date enddate;
    private String score;

    @PostPersist
    public void onPostPersist(){
        CourseOrdered courseOrdered = new CourseOrdered();
        BeanUtils.copyProperties(this, courseOrdered);
        courseOrdered.publishAfterCommit();

    }
    @PostRemove
    public void onPostRemove(){
        CourseOrderCanceled courseOrderCanceled = new CourseOrderCanceled();
        BeanUtils.copyProperties(this, courseOrderCanceled);
        courseOrderCanceled.publishAfterCommit();

    }
    @PostUpdate
    public void onPostUpdate(){
        EvaluationRegistered evaluationRegistered = new EvaluationRegistered();
        BeanUtils.copyProperties(this, evaluationRegistered);
        evaluationRegistered.publishAfterCommit();

    }
    @PrePersist
    public void onPrePersist(){
    }
    @PreUpdate
    public void onPreUpdate(){
    }
    @PreRemove
    public void onPreRemove(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    public String getCourseNo() {
        return courseNo;
    }

    public void setCourseNo(String courseNo) {
        this.courseNo = courseNo;
    }
    public String getPhonenum() {
        return phonenum;
    }

    public void setPhonenum(String phonenum) {
        this.phonenum = phonenum;
    }
    public Date getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(Date orderdate) {
        this.orderdate = orderdate;
    }
    public Date getBegindate() {
        return begindate;
    }

    public void setBegindate(Date begindate) {
        this.begindate = begindate;
    }
    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }
    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

}
```
- (Repository 예시) Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package onlinestudy;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="learningEvaluations", path="learningEvaluations")
public interface LearningEvaluationRepository extends PagingAndSortingRepository<LearningEvaluation, Long>{
  
   LearningEvaluation findByOrderNo(String orderNo);

}
```

- 적용 후 REST API 의 테스트
1. 주문서비스에서 강좌주문등록 (Async Pub/Sub, Command POTS->Policy)
  - http POST localhost:8081/orderManagements orderNo=01 courseNo=01 phonenum=01011111111 orderdate=20210630 begindate=20210701 enddate=20210831 status="강좌주문"
  - http GET localhost:8082/learningManagements/1

![image](https://user-images.githubusercontent.com/84000893/124385622-7dd18e00-dd11-11eb-854d-11aedad488a7.png)
![image](https://user-images.githubusercontent.com/84000893/124385793-2ed82880-dd12-11eb-8a4f-eb1419283fe1.png)

 2. 학습관리서비스에서 학습관리시작 (Async Pub/Sub, Command PATCH->Policy)
  - http PATCH localhost:8082/learningManagements/1 orderNo=01 managername=조상임  status="학습관리시작"
  - http GET localhost:8083/learningEvaluations/1
  - http GET localhost:8084/smsHistories

![image](https://user-images.githubusercontent.com/84000893/124386255-8d060b00-dd14-11eb-83dd-c757bac0df7e.png)
![image](https://user-images.githubusercontent.com/84000893/124386285-b4f56e80-dd14-11eb-9e75-c88e4b284df1.png)
![image](https://user-images.githubusercontent.com/84000893/124386295-baeb4f80-dd14-11eb-82a1-32a304049cd1.png)

3. 학습평가서비스에서 평가점수등록 (Sync-Req/Res, 주문관리 평가점수 등록되어야만 학습평가서비스에도 등록됨)
  - http PATCH localhost:8083/learningEvaluations/1 endFlg=true score=90
  - http GET localhost:8081/orderManagements/1
  - 동기호출 서비스 처리순서 (빨간색박스) 

![image](https://user-images.githubusercontent.com/84000893/124388371-9e9fe080-dd1d-11eb-8369-f57ac1926ed6.png)
![image](https://user-images.githubusercontent.com/84000893/124388377-a3fd2b00-dd1d-11eb-861e-e6ad18722a22.png)
![image](https://user-images.githubusercontent.com/84000893/124388383-a9f30c00-dd1d-11eb-8073-ddc5832129d0.png)

4. MyPage 학습현황조회- CQRS

![image](https://user-images.githubusercontent.com/84000893/124389347-9f3a7600-dd21-11eb-8fa2-8f1fb8ed38fe.png)

5. Gateway 8088포트로 진입점 통일

![image](https://user-images.githubusercontent.com/84000893/124390465-bb8ce180-dd26-11eb-83d0-4df1c63c5dfe.png)
![image](https://user-images.githubusercontent.com/84000893/124390461-b760c400-dd26-11eb-906f-b9af9eab7d51.png)
![image](https://user-images.githubusercontent.com/84000893/124390457-b29c1000-dd26-11eb-9819-de8ddc7500ae.png)


## 폴리글랏 퍼시스턴스

(H2DB, HSQLDB 사용) Notification(문자알림) 서비스는 문자알림 이력이 많이 쌓일 수 있으므로 자바로 작성된 관계형 데이터베이스인 HSQLDB를 사용하기로 하였다. 이를 위해 pom.xml 파일에 아래 설정을 추가하였다.

```
# pom.xml
<dependency>
	<groupId>org.hsqldb</groupId>
    	<artifactId>hsqldb</artifactId>
	<scope>runtime</scope>
</dependency>
```
![image](https://user-images.githubusercontent.com/84000893/124394036-78d40500-dd38-11eb-9402-698c89dcd825.png)

- 주문관리, 학습관리, 학습평가 등 나머지 서비스는 H2 DB를 사용한다.
```
<dependency>
	<groupId>com.h2database</groupId>
	<artifactId>h2</artifactId>
	<scope>runtime</scope>
</dependency>
```

## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 심사결과등록(입찰심사)->낙찰자정보등록(입찰관리) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- (동기호출-Req)낙찰자정보 등록 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 
```
# (BiddingExamination) BiddingManagementService.java
package bidding.external;

@FeignClient(name="BiddingManagement", url="http://${api.url.bidding}:8080", fallback=BiddingManagementServiceFallback.class)
public interface BiddingManagementService {

    @RequestMapping(method= RequestMethod.GET, path="/biddingManagements/registSucessBidder")
    public boolean registSucessBidder(@RequestParam("noticeNo") String noticeNo,
    @RequestParam("succBidderNm") String succBidderNm, @RequestParam("phoneNumber") String phoneNumber);

}
```

- (Fallback) 낙찰자정보 등록 서비스가 정상적으로 호출되지 않을 경우 Fallback 처리
```
# (BiddingExamination) BiddingManagementServiceFallback.java
package bidding.external;

import org.springframework.stereotype.Component;

@Component
public class BiddingManagementServiceFallback implements BiddingManagementService{

    @Override
    public boolean registSucessBidder(String noticeNo,String succBidderNm, String phoneNumber){
        System.out.println("★★★★★★★★★★★Circuit breaker has been opened. Fallback returned instead.★★★★★★★★★★★");
        return false;
    }
}
```

```
feign:
  hystrix:
    enabled: true
```

- (동기호출-Res) 낙찰자자정보 등록 서비스 (정상 호출)
```
# (BiddingManagement) BiddingManagementController.java
package bidding;

 @RestController
 public class BiddingManagementController {

    @Autowired
    BiddingManagementRepository biddingManagementRepository;

    @RequestMapping(value = "/biddingManagements/registSucessBidder",
       method = RequestMethod.GET,
       produces = "application/json;charset=UTF-8")
    public boolean registSucessBidder(HttpServletRequest request, HttpServletResponse response) {
       boolean status = false;

       String noticeNo = String.valueOf(request.getParameter("noticeNo"));
       
       BiddingManagement biddingManagement = biddingManagementRepository.findByNoticeNo(noticeNo);

       if(biddingManagement.getDemandOrgNm() == null || "조달청".equals(biddingManagement.getDemandOrgNm()) == false){
            biddingManagement.setSuccBidderNm(request.getParameter("succBidderNm"));
            biddingManagement.setPhoneNumber(request.getParameter("phoneNumber"));

            biddingManagementRepository.save(biddingManagement);

            status = true;
       }

       return status;
    }

 }
```

- (동기호출-PostUpdate) 심사결과가 등록된 직후(@PostUpdate) 낙찰자정보 등록을 요청하도록 처리 (낙찰자가 아닌 경우, 이후 로직 스킵)
```
# BiddingExamination.java (Entity)

    @PostUpdate
    public void onPostUpdate(){
        // 낙찰업체가 아니면 Skip.
        if(getSuccessBidderFlag() == false) return;

        try{
            // mappings goes here
            boolean isUpdated = BiddingExaminationApplication.applicationContext.getBean(bidding.external.BiddingManagementService.class)
            .registSucessBidder(getNoticeNo(), getCompanyNm(), getPhoneNumber());

            if(isUpdated == false){
                throw new Exception("입찰관리 서비스의 입찰공고에 낙찰자 정보가 갱신되지 않음");
            }
        }catch(java.net.ConnectException ce){
            throw new Exception("입찰관리 서비스 연결 실패");
        }catch(Exception e){
            throw new Exception("입찰관리 서비스 처리 실패");
        }
```

- (동기호출-테스트) 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 입찰관리 시스템이 장애가 나면 입찰심사 등록도 못 한다는 것을 확인:

```
# 입찰관리(BiddingManagement) 서비스를 잠시 내려놓음 (ctrl+c)

#심사결과 등록 : Fail
http PATCH http://localhost:8083/biddingExaminations/1 noticeNo=n01 participateNo=p01 successBidderFlag=true

#입찰관리 서비스 재기동
cd BiddingManagement
mvn spring-boot:run

#심사결과 등록 : Success
http PATCH http://localhost:8083/biddingExaminations/1 noticeNo=n01 participateNo=p01 successBidderFlag=true
```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


입찰공고가 등록된 후에 입찰참여 시스템에 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 입찰참여 시스템의 처리를 위하여 입찰공고 트랜잭션이 블로킹 되지 않도록 처리한다.
 
- (Publish) 이를 위하여 입찰공고 기록을 남긴 후에 곧바로 등록 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
@Entity
@Table(name="BiddingManagement_table")
public class BiddingManagement {

 ...
    @PostPersist
    public void onPostPersist(){
        NoticeRegistered noticeRegistered = new NoticeRegistered();
        BeanUtils.copyProperties(this, noticeRegistered);
        noticeRegistered.publishAfterCommit();
    }
```
- (Subscribe-등록) 입찰참여 서비스에서는 입찰공고 등록됨 이벤트를 수신하면 입찰공고 번호를 등록하는 정책을 처리하도록 PolicyHandler를 구현한다:

```
@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverNoticeRegistered_RecieveBiddingNotice(@Payload NoticeRegistered noticeRegistered){

        if(!noticeRegistered.validate()) return;

        if(noticeRegistered.isMe()){
            BiddingParticipation biddingParticipation = new BiddingParticipation();
            biddingParticipation.setNoticeNo(noticeRegistered.getNoticeNo());

            biddingParticipationRepository.save(biddingParticipation);
        }
    }

```
- (Subscribe-취소) 입찰참여 서비스에서는 입찰공고가 취소됨 이벤트를 수신하면 입찰참여 정보를 삭제하는 정책을 처리하도록 PolicyHandler를 구현한다:
  
```
@Service
public class PolicyHandler{
    @Autowired BiddingParticipationRepository biddingParticipationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverNoticeCanceled_CancelBiddingParticipation(@Payload NoticeCanceled noticeCanceled){

        if(!noticeCanceled.validate()) return;

        if(noticeCanceled.isMe()){
            BiddingParticipation biddingParticipation = biddingParticipationRepository.findByNoticeNo(noticeCanceled.getNoticeNo());
            biddingParticipationRepository.delete(biddingParticipation);
        }
            
    }

```

- (장애격리) 입찰관리, 입찰참여 시스템은 입찰심사 시스템과 완전히 분리되어 있으며, 이벤트 수신에 따라 처리되기 때문에, 입찰심사 시스템이 유지보수로 인해 잠시 내려간 상태라도 입찰관리, 입찰참여 서비스에 영향이 없다:
```
# 입찰심사 서비스 (BiddingExamination) 를 잠시 내려놓음 (ctrl+c)

#입찰공고 등록 : Success
http POST localhost:8081/biddingManagements noticeNo=n33 title=title33
#입찰참여 등록 : Success
http PATCH http://localhost:8082/biddingParticipations/2 noticeNo=n33 participateNo=p33 companyNo=c33 companyNm=doremi33 phoneNumber=010-1234-1234

#입찰관리에서 낙찰업체명 갱신 여부 확인
http localhost:8081/biddingManagements/2     # 낙찰업체명 갱신 안 됨 확인

#입찰심사 서비스 기동
cd BiddingExamination
mvn spring-boot:run

#심사결과 등록 : Success
http PATCH http://localhost:8083/biddingExaminations/2 noticeNo=n33 participateNo=p33 successBidderFlag=true

#입찰관리에서 낙찰업체명 갱신 여부 확인
http localhost:8081/biddingManagements/2     # 낙찰업체명 갱신됨 확인
```

# 운영:

컨테이너화된 마이크로서비스의 자동 배포/조정/관리를 위한 쿠버네티스 환경 운영

## Deploy

- GitHub 와 연결 후 로컬빌드를 진행 진행
```
	cd team
	mkdir sourcecode
	cd sourcecode
	git clone --recurse-submodules https://github.com/21-2-1team/bidding03.git
	
	cd bidding
	cd BiddingExamination
	mvn package
	
	cd ../BiddingManagement
	mvn package
	
	cd ../BiddingParticipation
	mvn package
	
	cd ../MyPage
	mvn package
	
	
	cd ../Notification
	mvn package
	
	
	cd ../gateway
        mvn package
```
- namespace 등록 및 변경
```
kubectl config set-context --current --namespace=bidding  --> bidding namespace 로 변경

kubectl create ns bidding
```

- ACR 컨테이너이미지 빌드
```
az acr build --registry user01skccacr --image user01skccacr.azurecr.io/biddingexamination:latest .
```
![image](https://user-images.githubusercontent.com/70736001/122502677-096cce80-d032-11eb-96e7-84a8024ab45d.png)

나머지 서비스에 대해서도 동일하게 등록을 진행함
```
az acr build --registry user01skccacr --image user01skccacr.azurecr.io/biddingmanagement:latest .
az acr build --registry user01skccacr --image user01skccacr.azurecr.io/biddingparticipation:latest .
az acr build --registry user01skccacr --image user01skccacr.azurecr.io/biddingparticipation:latest .
az acr build --registry user01skccacr --image user01skccacr.azurecr.io/mypage:latest  .
az acr build --registry user01skccacr --image user01skccacr.azurecr.io/notification:latest  .
az acr build --registry user01skccacr --image user01skccacr.azurecr.io/gateway:latest .
```

- 배포진행

1.bidding/BiddingExamination/kubernetes/deployment.yml 파일 수정 (BiddingManagement/BiddingParticipation/MyPage/Notification/gateway 동일)

![image](https://user-images.githubusercontent.com/70736001/122512566-011d8f00-d044-11eb-8bd5-91d939f7ab1b.png)

2.bidding/BiddingExamination/kubernetes/service.yaml 파일 수정 (BiddingManagement/BiddingParticipation/MyPage/Notification 동일)

![image](https://user-images.githubusercontent.com/70736001/122512673-26aa9880-d044-11eb-8587-38f8cd261326.png)

3.bidding/gateway/kubernetes/service.yaml 파일 수정

![image](https://user-images.githubusercontent.com/70736001/122503123-da0a9180-d032-11eb-9283-224d7860c9c3.png)

4. 배포작업 수행
``` 
	cd gateway/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	cd ../../BiddingExamination/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	cd ../../BiddingManagement/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	
	cd ../../BiddingParticipation/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	
	cd ../../MyPage/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	
	cd ../../Notification/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
``` 

5. 배포결과 확인
``` 
kubectl get all
``` 
![image](https://user-images.githubusercontent.com/70736001/122503307-2b1a8580-d033-11eb-83fc-63b0f2154e3b.png)

- Kafka 설치
``` 
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh

kubectl --namespace kube-system create sa tiller 
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
helm init --service-account tiller

helm repo add incubator https://charts.helm.sh/incubator
helm repo update

kubectl create ns kafka
helm install --name my-kafka --namespace kafka incubator/kafka

kubectl get all -n kafka
``` 
설치 후 서비스 재기동

## Autoscale (HPA)
앞서 CB(Circuit breaker)는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다.

- 리소스에 대한 사용량 정의(bidding/BiddingManagement/kubernetes/deployment.yml)
![image](https://user-images.githubusercontent.com/70736001/122503960-49cd4c00-d034-11eb-8ab4-b322e7383cc0.png)

- Autoscale 설정 (request값의 20%를 넘어서면 Replica를 10개까지 동적으로 확장)
```
kubectl autoscale deployment biddingmanagement --cpu-percent=20 --min=1 --max=10
```

- siege 생성 (로드제너레이터 설치)
```
kubectl apply -f - <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: siege
  namespace: bidding
spec:
  containers:
  - name: siege
    image: apexacme/siege-nginx
EOF
```
- 부하발생 (50명 동시사용자, 30초간 부하)
```
kubectl exec -it pod/siege  -c siege -n bidding -- /bin/bash
siege -c50 -t30S -v --content-type "application/json" 'http://52.231.8.61:8080/biddingManagements POST {"noticeNo":1,"title":"AAA"}'
```
- 모니터링 (부하증가로 스케일아웃되어지는 과정을 별도 창에서 모니터링)
```
watch kubectl get al
```
- 자동스케일아웃으로 Availablity 100% 결과 확인 (시간이 좀 흐른 후 스케일 아웃이 벌어지는 것을 확인, siege의 로그를 보아도 전체적인 성공률이 높아진 것을 확인함)

1.테스트전

![image](https://user-images.githubusercontent.com/70736001/122504322-0aebc600-d035-11eb-883f-35110d9d0457.png)

2.테스트후

![image](https://user-images.githubusercontent.com/70736001/122504349-1e972c80-d035-11eb-814e-a5ab909215c4.png)

3.부하발생 결과

![image](https://user-images.githubusercontent.com/70736001/122504389-31a9fc80-d035-11eb-976e-f43261d1a8c2.png)

## Config Map
ConfigMap을 사용하여 변경가능성이 있는 설정을 관리

- 입찰심사(BiddingExamination) 서비스에서 동기호출(Req/Res방식)로 연결되는 입찰관리(BiddingManagement) 서비스 url 정보 일부를 ConfigMap을 사용하여 구현

- 파일 수정
  - 입찰심사 소스 (BiddingExamination/src/main/java/bidding/external/BiddingManagementService.java)

![image](https://user-images.githubusercontent.com/70736001/122505096-9dd93000-d036-11eb-91b7-0ec57b6e1b10.png)

- Yaml 파일 수정
  - application.yml (BiddingExamination/src/main/resources/application.yml)
  - deploy yml (BiddingExamination/kubernetes/deployment.yml)

![image](https://user-images.githubusercontent.com/70736001/122505177-c5c89380-d036-11eb-91b3-f399547b50ff.png)

- Config Map 생성 및 생성 확인
```
kubectl create configmap bidding-cm --from-literal=url=BiddingManagement
kubectl get cm
```

![image](https://user-images.githubusercontent.com/70736001/122505221-dc6eea80-d036-11eb-8757-b97f8d75baff.png)

```
kubectl get cm bidding-cm -o yaml
```

![image](https://user-images.githubusercontent.com/70736001/122505270-f6103200-d036-11eb-8c96-513f95448989.png)

```
kubectl get pod
```

![image](https://user-images.githubusercontent.com/70736001/122505313-0fb17980-d037-11eb-9b57-c0d14f468a1c.png)


## Zero-Downtime deploy (Readiness Probe)
쿠버네티스는 각 컨테이너의 상태를 주기적으로 체크(Health Check)해서 문제가 있는 컨테이너는 서비스에서 제외한다.

- deployment.yml에 readinessProbe 설정 후 미설정 상태 테스트를 위해 주석처리함 
```
readinessProbe:
httpGet:
  path: '/biddingManagements'
  port: 8080
initialDelaySeconds: 10
timeoutSeconds: 2
periodSeconds: 5
failureThreshold: 10
```

- deployment.yml에서 readinessProbe 미설정 상태로 siege 부하발생

![image](https://user-images.githubusercontent.com/70736001/122505873-2906f580-d038-11eb-86b8-2f8388f82dd1.png)

```
kubectl exec -it pod/siege  -c siege -n bidding -- /bin/bash
siege -c100 -t5S -v --content-type "application/json" 'http://20.194.120.4:8080/biddingManagements POST {"noticeNo":1,"title":"AAA"}
```
1.부하테스트 전

![image](https://user-images.githubusercontent.com/70736001/122506020-75eacc00-d038-11eb-99df-4a4b90478bc3.png)

2.부하테스트 후

![image](https://user-images.githubusercontent.com/70736001/122506060-84d17e80-d038-11eb-8449-b94b28a0f385.png)

3.생성중인 Pod 에 대한 요청이 들어가 오류발생

![image](https://user-images.githubusercontent.com/70736001/122506129-a03c8980-d038-11eb-8822-5ec57926b900.png)

- 정상 실행중인 biddingmanagement으로의 요청은 성공(201),비정상 적인 요청은 실패(503 - Service Unavailable) 확인

- hpa 설정에 의해 target 지수 초과하여 biddingmanagement scale-out 진행됨

- deployment.yml에 readinessProbe 설정 후 부하발생 및 Availability 100% 확인

![image](https://user-images.githubusercontent.com/70736001/122506358-2527a300-d039-11eb-84cb-62eb09687bda.png)

1.부하테스트 전

![image](https://user-images.githubusercontent.com/70736001/122506400-3c669080-d039-11eb-8e5e-a4f76b0e2956.png)

2.부하테스트 후

![image](https://user-images.githubusercontent.com/70736001/122506421-4be5d980-d039-11eb-92a2-44e7827299bf.png)

3.readiness 정상 적용 후, Availability 100% 확인

![image](https://user-images.githubusercontent.com/70736001/122506471-61f39a00-d039-11eb-9077-608f375e27f3.png)


## Self-healing (Liveness Probe)
쿠버네티스는 각 컨테이너의 상태를 주기적으로 체크(Health Check)해서 문제가 있는 컨테이너는 자동으로재시작한다.

- depolyment.yml 파일의 path 및 port를 잘못된 값으로 변경
  depolyment.yml(BiddingManagement/kubernetes/deployment.yml)
```
 livenessProbe:
    httpGet:
        path: '/biddingmanagement/failed'
        port: 8090
      initialDelaySeconds: 30
      timeoutSeconds: 2
      periodSeconds: 5
      failureThreshold: 5
```




![image](https://user-images.githubusercontent.com/70736001/122506714-d75f6a80-d039-11eb-8bd0-223490797b58.png)

- liveness 설정 적용되어 컨테이너 재시작 되는 것을 확인
  Retry 시도 확인 (pod 생성 "RESTARTS" 숫자가 늘어나는 것을 확인) 

1.배포 전

![image](https://user-images.githubusercontent.com/70736001/122506797-fb22b080-d039-11eb-9a0b-754e0fea45b2.png)

2.배포 후

![image](https://user-images.githubusercontent.com/70736001/122506831-0c6bbd00-d03a-11eb-880c-dc8d3e00798f.png)

## Circuit Breaker
서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함
시나리오는 심사결과등록(입찰심사:BiddingExamination)-->낙찰자정보등록(입찰관리:BiddingManagement) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 낙찰자정보등록이 과도할 경우 CB 를 통하여 장애격리.


- Hystrix 를 설정: 요청처리 쓰레드에서 처리시간이 1000ms가 넘어서기 시작하면 CB 작동하도록 설정

**application.yml (BiddingExamination)**
```
feign:
  hystrix:
    enabled: true

hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 1000
```
![image](https://user-images.githubusercontent.com/70736001/122508631-3a9ecc00-d03d-11eb-9bce-a786225df40f.png)

- 피호출 서비스(입찰관리:biddingmanagement) 의 임의 부하 처리 - 800ms에서 증감 300ms 정도하여 800~1100 ms 사이에서 발생하도록 처리
BiddingManagementController.java
```
req/res를 처리하는 피호출 function에 sleep 추가

	try {
	   Thread.sleep((long) (800 + Math.random() * 300));
	} catch (InterruptedException e) {
	   e.printStackTrace();
	}
```
![image](https://user-images.githubusercontent.com/70736001/122508689-5609d700-d03d-11eb-9e08-8eadc904d391.png)

- req/res 호출하는 위치가 onPostUpdate에 있어 실제로 Data Update가 발생하지 않으면 호출이 되지 않는 문제가 있어 siege를 2개 실행하여 Update가 지속적으로 발생하게 처리 함
```
siege -c2 –t20S  -v --content-type "application/json" 'http://20.194.120.4:8080/biddingExaminations/1 PATCH {"noticeNo":"n01","participateNo":"p01","successBidderFlag":"true"}'
siege -c2 –t20S  -v --content-type "application/json" 'http://20.194.120.4:8080/biddingExaminations/1 PATCH {"noticeNo":"n01","participateNo":"p01","successBidderFlag":"false"}'
```
![image](https://user-images.githubusercontent.com/70736001/122508763-7b96e080-d03d-11eb-90f8-8380277cdc17.png)
