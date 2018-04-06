package ie.wit.witselfiecompetition.model;

/**
 * Model class to represent the Gallery Image and its details
 * for the user
 * Created by yahya Almardeny on 04/04/18.
 */
public class Gallery {

    private String likes;
    private String date;
    private String image;
    private String compName;

    public Gallery(String date, String likes, String image, String compName) {
        this.likes = likes;
        this.date = date;
        this.image = image;
        this.compName = compName;
    }

    public Gallery() { }



    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    @Override
    public String toString() {
        return "Gallery{" +
                "likes='" + likes + '\'' +
                ", date='" + date + '\'' +
                ", image='" + image + '\'' +
                ", compName='" + compName + '\'' +
                '}';
    }
}
