package ie.wit.witselfiecompetition.model;


import java.util.Date;

/**
 * Competition Model Class to represent JSON data
 * in the database of the Selfie Competition
 * It contains the competition information and
 * date, in addition to a link to the submitted selfie
 * Created by yahya Almardeny on 17/03/18.
 */

public class Competition {
    private int cId;
    private String name;
    private String desc;
    private String openDate;
    private String closeDate;
    private String userId;

    public Competition(int cId, String name, String desc, String openDate, String closeDate, String userId) {
        this.cId = cId;
        this.name = name;
        this.desc = desc;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.userId = userId;
    }

    public Competition(){}


    public int getcId() {
        return cId;
    }

    public void setcId(int cId) {
        this.cId = cId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public String getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(String closeDate) {
        this.closeDate = closeDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Competition{" +
                "cId=" + cId +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", openDate=" + openDate +
                ", closeDate=" + closeDate +
                ", userId='" + userId + '\'' +
                '}';
    }
}
