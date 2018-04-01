package ie.wit.witselfiecompetition.model;

import java.io.Serializable;

/**
 * User Model Class to represent User Collection
 * in Database
 * Created by yahya on 21/02/18.
 */

public class User implements Serializable{
    private String fName;
    private String lName;
    private String gender;
    private String course;
    private String aboutMe;
    private String image;

    public User(){}

    /**
     * Constructor for creating new User
     * @param fName
     * @param lName
     * @param gender
     * @param course
     * @param aboutMe
     * @param image
     */
    public User(String fName, String lName, String gender, String course, String aboutMe, String image) {
        this.fName = fName;
        this.lName = lName;
        this.gender = gender;
        this.course = course;
        this.aboutMe = aboutMe;
        this.image = image;
    }

    @Override
    public String toString() {
        return "User{" +
                "fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                ", gender=" + gender+
                ", course=" + course +
                ", aboutMet='" + aboutMe + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
