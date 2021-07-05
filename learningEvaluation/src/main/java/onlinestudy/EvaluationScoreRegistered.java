package onlinestudy;

public class EvaluationScoreRegistered extends AbstractEvent {

    private Long id;
    private String orderNo;
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

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

}
