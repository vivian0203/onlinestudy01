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
