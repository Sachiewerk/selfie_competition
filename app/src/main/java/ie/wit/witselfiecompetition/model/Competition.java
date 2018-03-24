package ie.wit.witselfiecompetition.model;


/**
 * Competition Model Class to represent JSON data
 * in the database of the Selfie Competition
 * It contains the competition information and
 * date, in addition to a link to the submitted selfie
 * Created by yahya Almardeny on 17/03/18.
 */

public class Competition {
    private String cId;
    private String name;
    private String openDate;
    private String closeDate;

    public Competition(String cId, String name, String openDate, String closeDate) {
        this.cId = cId;
        this.name = name;
        this.openDate = openDate;
        this.closeDate = closeDate;
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


    @Override
    public String toString() {
        return "Competition{" +
                "cId=" + cId +
                ", name='" + name + '\'' +
                ", openDate=" + openDate +
                ", closeDate=" + closeDate +
                '}';
    }
}
