package onlinestudy;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="LearningEvaluation_table")
public class LearningEvaluation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String orderNo;
    private String courseNo;
    private String phonenum;
    private Date begindate;
    private Date enddate;
    private Boolean endFlg;
    private String score;

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
    @PostUpdate
    public void onPostUpdate(){
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
    public Boolean getEndFlg() {
        return endFlg;
    }

    public void setEndFlg(Boolean endFlg) {
        this.endFlg = endFlg;
    }
    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }




}
