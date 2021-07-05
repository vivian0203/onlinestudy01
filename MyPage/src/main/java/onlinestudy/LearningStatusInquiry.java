package onlinestudy;

import java.util.Date;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="LearningStatusInquiry_table")
public class LearningStatusInquiry {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private String orderNo;
        private String courseNo;
        private String phonenum;
        private Date begindate;
        private Date enddate;
        private String score;


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

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        
}
