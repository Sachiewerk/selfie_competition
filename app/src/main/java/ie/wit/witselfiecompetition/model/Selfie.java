package ie.wit.witselfiecompetition.model;


import java.util.List;

/**
 * Selfie Model Class to represent the JSON information
 * about the captured selfie pictures that are submitted
 * to the Selfie Competition in which a user can submit one and only
 * one Selfie for each open competition.
 * Each selfie counts the likes got from other users.
 * The collection of this class in the database link between
 * Users and the Competition.
 * Created by yahya Almardeny on 17/03/18.
 */

public class Selfie {
    private String cId;
    private String userId;
    private String image;
    private List<String> likes;

    public Selfie(String cId, String userId, String image, List<String> likes) {
        this.cId = cId;
        this.userId = userId;
        this.image = image;
        this.likes = likes;
    }

    public Selfie(){}



    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return "Selfie{" +
                "cId='" + cId + '\'' +
                ", userId='" + userId + '\'' +
                ", image='" + image + '\'' +
                ", likes=" + likes +
                '}';
    }
}
