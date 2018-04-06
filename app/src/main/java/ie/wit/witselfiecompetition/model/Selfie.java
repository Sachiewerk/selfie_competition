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
    private String uId;
    private String image;
    private List<String> likes;
    private String date;

    public Selfie(String uId, String image, List<String> likes, String date) {
        this.uId = uId;
        this.image = image;
        this.likes = likes;
        this.date = date;
    }

    public Selfie(){}


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


    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Selfie{" +
                "uId='" + uId + '\'' +
                ", image='" + image + '\'' +
                ", likes=" + likes +
                ", date='" + date + '\'' +
                '}';
    }


}
