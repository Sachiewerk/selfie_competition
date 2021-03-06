package ie.wit.witselfiecompetition.model;


import java.io.Serializable;
import java.util.List;

/**
 * Competition Model Class to represent JSON data
 * in the database of the Selfie Competition
 * It contains the competition information and
 * date, in addition to a link to the submitted selfie
 * Created by yahya Almardeny on 17/03/18.
 */

public class Competition implements Serializable {
    private String cId;
    private String name;
    private String openDate;
    private String closeDate;
    private List<String> selfiesId;

    public Competition(String cId, String name, String openDate, String closeDate, List<String> selfiesId) {
        this.cId = cId;
        this.name = name;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.selfiesId = selfiesId;
    }

    public Competition(){}


    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<String> getSelfiesId() {
        return selfiesId;
    }

    public void setSelfiesId(List<String> selfiesId) {
        this.selfiesId = selfiesId;
    }

    @Override
    public String toString() {
        return "Competition{" +
                "cId='" + cId + '\'' +
                ", name='" + name + '\'' +
                ", openDate='" + openDate + '\'' +
                ", closeDate='" + closeDate + '\'' +
                ", selfiesId=" + selfiesId +
                '}';
    }

}
