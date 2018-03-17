package ie.wit.witselfiecompetition.model;

/**
 * Selfie Model Class to represent the JSON information
 * about the captured selfie pictures that are submitted
 * to the Selfie Competition in which a user can submit one and only
 * one Selfie for each open competition.
 * Each selfie counts the likes got from other users.
 * The collection of this class in the databasse link between
 * Users and the Competition.
 * Created by yahya Almardeny on 17/03/18.
 */

public class Selfie {

    private int cId;
    private String userId;
    private String image;
    private int like;

    public Selfie(int cId, String userId, String image, int like) {
        this.cId = cId;
        this.userId = userId;
        this.image = image;
        this.like = like;
    }

    public Selfie(){}


    @Override
    public String toString() {
        return "Selfie{" +
                "cId=" + cId +
                ", userId='" + userId + '\'' +
                ", image='" + image + '\'' +
                ", like=" + like +
                '}';
    }

    public int getcId() {
        return cId;
    }

    public void setcId(int cId) {
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

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }
}
