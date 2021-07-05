package onlinestudy;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="LearningManagement_table")
public class LearningManagement {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String orderNo;
    private String courseNo;
    private String phonenum;
    private Date orderdate;
    private Date begindate;
    private Date enddate;
    private String managername;

    @PostUpdate
    public void onPostUpdate(){
        LearningManagementStarted learningManagementStarted = new LearningManagementStarted();
        BeanUtils.copyProperties(this, learningManagementStarted);
        learningManagementStarted.publishAfterCommit();

    }
    @PostRemove
    public void onPostRemove(){
        LearningManagementCanceled learningManagementCanceled = new LearningManagementCanceled();
        BeanUtils.copyProperties(this, learningManagementCanceled);
        learningManagementCanceled.publishAfterCommit();

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
    
    public String getManagername() {
        return managername;
    }
    public void setManagerString(String managername) {
        this.managername = managername;
    }

}
