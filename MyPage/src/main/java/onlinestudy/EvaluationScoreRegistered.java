package onlinestudy;

import java.util.Date;

public class EvaluationScoreRegistered extends AbstractEvent {

    private Long id;
    private String orderNo;
    private Date beginDate;
    private Date endDate;
    private String score;

    public EvaluationScoreRegistered(){
        super();
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

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getScore() {
        return score;
    }
    public void setScore(String score) {
        this.score = score ; 
    }

}
