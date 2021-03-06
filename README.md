![image](https://user-images.githubusercontent.com/84000893/124534491-5a146200-de4f-11eb-8fb9-45c27d87fbfe.png)

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

![image](https://user-images.githubusercontent.com/84000893/124521447-77830500-de2a-11eb-9969-fdfa6fdd35ff.png)


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

분석단계에서의 조건 중 하나로 평가점수등록(학습평가)->평가등록(주문관리) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- (동기호출-Req)평가등록 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 
```
# (learningEvaluation) learningManagementService.java
package onlinestudy.external;

@FeignClient(name="orderManagement", url="http://localhost:8081", fallback=OrderManagementServiceFallback.class)
public interface OrderManagementService {
    @RequestMapping(method= RequestMethod.GET, path="/orderManagements/registerEvaluation")
    public boolean registerEvaluation(@RequestParam("orderNo") String orderNo,
    @RequestParam("score") String score);
}
```

- (Fallback) 평가등록 서비스가 정상적으로 호출되지 않을 경우 Fallback 처리
```
# (learningEvaluation) learningManagementServiceFallback.java
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
```

```
feign:
  hystrix:
    enabled: true
```

- (동기호출-Res) 평가등록 서비스 (정상 호출)
```
# (oderManagement) orderManagementController.java
package onlinestudy;

@RestController
public class OrderManagementController {

   @Autowired
   OrderManagementRepository orderManagementRepository;

   @RequestMapping(value = "/orderManagements/registerEvaluation",
      method = RequestMethod.GET,
      produces = "application/json;charset=UTF-8")
   
  
      public boolean registerEvaluation(HttpServletRequest request, HttpServletResponse response) {
      boolean status = false;

      String orderNo = String.valueOf(request.getParameter("orderNo"));
      
      System.out.println("@@@@@@@@@@@@@@@@@evaluation orderNo@" + request.getParameter("orderNo"));
      System.out.println("@@@@@@@@@@@@@@@@@evaluation score@" + request.getParameter("score"));
      
      OrderManagement orderManagement = orderManagementRepository.findByOrderNo(orderNo);

     // if(orderManagement.orderNo()){
           orderManagement.setScore(request.getParameter("score"));
           orderManagementRepository.save(orderManagement);
           status = true;
     // }

      return status;
   }

}
```

- (동기호출-PostUpdate) 평가결과가 등록된 직후(@PostUpdate) 평가등록을 요청하도록 처리 (평가등록이 아닌 경우, 이후 로직 스킵)
```
# learningEvaluation.java (Entity)
package onlinestudy;

    @PreUpdate
    public void onPreUpdate() throws Exception{
        EvaluationScoreRegistered evaluationScoreRegistered = new EvaluationScoreRegistered();
        BeanUtils.copyProperties(this, evaluationScoreRegistered);
        evaluationScoreRegistered.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        // 평가점수가 없으면 Skip.
        if(getEndFlag() == false) return;

        try{
            // mappings goes here
            boolean isUpdated = LearningEvaluationApplication.applicationContext.getBean(onlinestudy.external.OrderManagementService.class)
            .registerEvaluation(getOrderNo(), getScore());

            if(isUpdated == false){
                throw new Exception("주문관리 서비스의 주문관리에 평가점수가 갱신되지 않음");
            }
        }catch(java.net.ConnectException ce){
            throw new Exception("주문관리 서비스 연결 실패");
        }catch(Exception e){
            throw new Exception("주문관리 서비스 처리 실패");
        }
        
      //onlinestudy.external.OrderManagement orderManagement = new onlinestudy.external.OrderManagement();
    
        
    }
    private boolean getEndFlag() {
        return endFlg;
    }
```

- (동기호출-테스트) 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 주문관리 시스템이 장애가 나면 학습평가 등록도 못 한다는 것을 확인:

```
# 주문관리(orderManagement) 서비스를 잠시 내려놓음 (ctrl+c)

#평가점수 등록 : Fail
http PATCH http://localhost:8083/learningEvaluations/1 orderNo=01 score=90

#주문관리 서비스 재기동
cd orderManagement
mvn spring-boot:run

#평가점수 등록 : Success
http PATCH http://localhost:8083/learningEvaluations/1 orderNo=01 score=90
```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


주문관리가 등록된 후에 학습관리 시스템에 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 학습관리 시스템의 처리를 위하여 주문관리 트랜잭션이 블로킹 되지 않도록 처리한다.
 
- (Publish) 이를 위하여 주문관리 기록을 남긴 후에 곧바로 등록 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
@Entity
@Table(name="OrderManagement_table")
public class OrderManagement {

    @PostPersist
    public void onPostPersist(){
        CourseOrdered courseOrdered = new CourseOrdered();
        BeanUtils.copyProperties(this, courseOrdered);
        courseOrdered.publishAfterCommit();

    }
```
- (Subscribe-등록) 학습관리 서비스에서는 주문관리 등록됨 이벤트를 수신하면 주문관리 번호를 등록하는 정책을 처리하도록 PolicyHandler를 구현한다:

```
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


```
- (Subscribe-취소) 학습관리 서비스에서는 주문관리가 취소됨 이벤트를 수신하면 학습관리 정보를 삭제하는 정책을 처리하도록 PolicyHandler를 구현한다:
  
```
@StreamListener(KafkaProcessor.INPUT)
    public void wheneverCourseOrderCanceled_CancelLearningManagement(@Payload CourseOrderCanceled courseOrderCanceled){

        if(!courseOrderCanceled.validate()) return;
        
           LearningManagement learningManagement = learningManagementRepository.findByOrderNo(courseOrderCanceled.getOrderNo());
           learningManagementRepository.delete(learningManagement);

        
        System.out.println("\n\n##### listener CancelLearningManagement : " + courseOrderCanceled.toJson() + "\n\n");
    }

```

- (장애격리) 주문관리, 학습관리 시스템은 학습평가 시스템과 완전히 분리되어 있으며, 이벤트 수신에 따라 처리되기 때문에, 학습평가 시스템이 유지보수로 인해 잠시 내려간 상태라도 주문관리, 학습관리 서비스에 영향이 없다:
```
# 학습평가 서비스 (learningEvaluation) 를 잠시 내려놓음 (ctrl+c)

# 주문관리 등록 : Success
http POST localhost:8081/orderManagements orderNo=01 courseNo=01
# 학습관리 등록 : Success
http PATCH http://localhost:8082/learningManagement/2 orderNo=01 courseNo=01

# 주문관리에서 평가점수 갱신 여부 확인
http localhost:8081/orderManagements/2     # 평가점수 갱신 안 됨 확인

#학습평가 서비스 기동
cd learningEvaluation
mvn spring-boot:run

#평가점수 등록 : Success
http PATCH http://localhost:8083/learningEvaluations/2 orderNo=01 score=99 

#주문관리에서 평가점수 갱신 여부 확인
http localhost:8081/orderManagements/2     # 평가점수 갱신됨 확인
```

# 운영:

컨테이너화된 마이크로서비스의 자동 배포/조정/관리를 위한 쿠버네티스 환경 운영

## Deploy

- GitHub 와 연결 후 로컬빌드를 진행 진행
```
	
	mkdir onlinestudy01
	cd onlinesstudy01
	git clone --recurse-submodules https://github.com/vivian0203/onlinestudy01.git
	

	cd ../orderManagement
	mvn package
	
	cd ../learningManagement
	mvn package
	
	cd ../learningEvaluation
	mvn package
		
	cd ../notification
	mvn package

	cd ../mypage
	mvn package

	cd ../gateway
        mvn package
```
- namespace 등록 및 변경
```
kubectl config set-context --current --namespace=onlinestudy  --> onlinestudy namespace 로 변경

kubectl create ns onlinestudy
```

- ACR 컨테이너이미지 빌드
```
az acr build --registry user17skccacr --image user17skccacr.azurecr.io/ordermanagement:v1.0 .
```
![image](https://user-images.githubusercontent.com/84000893/124527928-dbfd8e80-de41-11eb-8e6e-a2e7536902b7.png)
![image](https://user-images.githubusercontent.com/84000893/124528014-2121c080-de42-11eb-8fac-fbc45a65e7c7.png)

나머지 서비스에 대해서도 동일하게 등록을 진행함
```
az acr build --registry user17skccacr --image user17skccacr.azurecr.io/ordermanagemt:v1.0 .
az acr build --registry user17skccacr --image user17skccacr.azurecr.io/learningmanagement:v1.0 .
az acr build --registry user17skccacr --image user17skccacr.azurecr.io/learningevaluation:v1.0 .
az acr build --registry user17skccacr --image user17skccacr.azurecr.io/mypage:v1.0  .
az acr build --registry user17skccacr --image user17skccacr.azurecr.io/notification:v1.0  .
az acr build --registry user17skccacr --image user17skccacr.azurecr.io/gateway:v1.0 .
```

- 배포진행

1.onlinestudy01/orderManagement/kubernetes/deployment.yml 파일 수정 (learninmanagement/learningevaluation/mypage/notification/gateway 동일)

![image](https://user-images.githubusercontent.com/84000893/124528214-9e4d3580-de42-11eb-89e3-e0d5696cf856.png)

2.onlinestudy01/ordermanagement/kubernetes/service.yaml 파일 수정 (ordermanagement/learninmanagement/mypage/notification 동일)

![image](https://user-images.githubusercontent.com/84000893/124528228-a4dbad00-de42-11eb-82a2-5b961e0d7433.png)

3.onlinestudy01/gateway/kubernetes/service.yaml 파일 수정

![image](https://user-images.githubusercontent.com/84000893/124528238-ac02bb00-de42-11eb-8bfa-f4f7939fb838.png)

4. 배포작업 수행
``` 
	cd gateway/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	cd ../../orderManagement/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	cd ../../learningManagement/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	
	cd ../../learningEvaluation/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	
	cd ../../mypage/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	
	cd ../../notification/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
``` 

5. 배포결과 확인
``` 
kubectl get all
``` 
![image](https://user-images.githubusercontent.com/84000893/124528481-3814e280-de43-11eb-8d88-86355d32361c.png)

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

- 리소스에 대한 사용량 정의(onlinestudy01/orderManagement/kubernetes/deployment.yml)
![image](https://user-images.githubusercontent.com/84000893/124528604-888c4000-de43-11eb-9773-7953db614ff3.png)

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

![image](https://user-images.githubusercontent.com/84000893/124528696-d4d78000-de43-11eb-9e06-67c71b3f89e5.png)


2.테스트후

![image](https://user-images.githubusercontent.com/84000893/124528830-1ec06600-de44-11eb-9ea0-a4cadef99d89.png)

3.부하발생 결과

![image](https://user-images.githubusercontent.com/84000893/124528951-7363e100-de44-11eb-88bd-231d648fda70.png)

## Config Map

ConfigMap을 사용하여 변경가능성이 있는 설정을 관리

- 학습평가(LearningEvaluation) 서비스에서 동기호출(Req/Res방식)로 연결되는 주문관리(OrderManagement) 서비스 url 정보 일부를 ConfigMap을 사용하여 구현

- 파일 수정
  - 학습평가 소스 (LearningEvaluation/src/main/java/onlinestudy01/external/OrderManagementService.java)
![image](https://user-images.githubusercontent.com/84000893/124547920-e599ed00-de67-11eb-8bd1-2b18c051023a.png)


- Yaml 파일 수정
  - application.yml (LearningEvaluation/src/main/resources/application.yml)
  - deploy yml (LearningEvaluation/kubernetes/deployment.yml)

![image](https://user-images.githubusercontent.com/84000893/124547939-eb8fce00-de67-11eb-86cf-180be07ffe6a.png)

![image](https://user-images.githubusercontent.com/84000893/124547955-f21e4580-de67-11eb-9f9f-462be4d00f79.png)

- Config Map 생성 및 생성 확인
```
kubectl create configmap onlinestudy-cm --from-literal=url=LearningEvaluation
kubectl get cm
```
![image](https://user-images.githubusercontent.com/84000893/124549134-b3898a80-de69-11eb-9541-2cb3c7123248.png)

```
kubectl get cm onlinestudy-cm -o yaml
```

![image](https://user-images.githubusercontent.com/84000893/124547982-ff3b3480-de67-11eb-8ff4-1bbb00b5a85a.png)

```
kubectl get pod
```
![image](https://user-images.githubusercontent.com/84000893/124547990-04987f00-de68-11eb-8dbc-a3e9e46dde10.png)



## Zero-Downtime deploy (Readiness Probe)
쿠버네티스는 각 컨테이너의 상태를 주기적으로 체크(Health Check)해서 문제가 있는 컨테이너는 서비스에서 제외한다.

- deployment.yml에 readinessProbe 설정 후 부하발생 및 Availability 100% 확인

![image](https://user-images.githubusercontent.com/70736001/122506358-2527a300-d039-11eb-84cb-62eb09687bda.png)

1.부하테스트 전

![image](https://user-images.githubusercontent.com/84000893/124542035-9a7adc80-de5d-11eb-8aff-388166d55fe8.png)

2.부하테스트 후

![image](https://user-images.githubusercontent.com/84000893/124542065-a36bae00-de5d-11eb-8bc5-014ec2ab2112.png)

3.readiness 정상 적용 후, Availability 100% 확인

![image](https://user-images.githubusercontent.com/84000893/124542077-aa92bc00-de5d-11eb-86e7-ed04959761f7.png)


## Self-healing (Liveness Probe)
쿠버네티스는 각 컨테이너의 상태를 주기적으로 체크(Health Check)해서 문제가 있는 컨테이너는 자동으로재시작한다.

- depolyment.yml 파일의 path 및 port를 잘못된 값으로 변경
  depolyment.yml(BiddingManagement/kubernetes/deployment.yml)
```
 livenessProbe:
    httpGet:
        path: '/ordergmanagement/failed'
        port: 8090
      initialDelaySeconds: 120
      timeoutSeconds: 2
      periodSeconds: 5
      failureThreshold: 5
```




![image](https://user-images.githubusercontent.com/84000893/124544676-7ff73200-de62-11eb-977c-92d2c4732670.png)

- liveness 설정 적용되어 컨테이너 재시작 되는 것을 확인
  Retry 시도 확인 (pod 생성 "RESTARTS" 숫자가 늘어나는 것을 확인) 

1.배포 전

![image](https://user-images.githubusercontent.com/84000893/124544694-8ab1c700-de62-11eb-9b09-4b470cda4da8.png)

2.배포 후

![image](https://user-images.githubusercontent.com/84000893/124544716-93a29880-de62-11eb-895c-3d429d3cc99b.png)


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
