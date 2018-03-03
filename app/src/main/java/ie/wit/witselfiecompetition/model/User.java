package ie.wit.witselfiecompetition.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yahya on 21/02/18.
 */

public class User {
    private String fName;
    private String lName;
    private String gender;
    private String course;
    private String aboutMet;
    private String image;

    public User(){}

    public User(String fName, String lName, String gender, String course, String aboutMet, String image) {
        this.fName = fName;
        this.lName = lName;
        this.gender = gender;
        this.course = course;
        this.aboutMet = aboutMet;
        this.image = image;
    }

    @Override
    public String toString() {
        return "User{" +
                "fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                ", gender=" + gender+
                ", course=" + course +
                ", aboutMet='" + aboutMet + '\'' +
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

    public String getAboutMet() {
        return aboutMet;
    }

    public void setAboutMet(String aboutMet) {
        this.aboutMet = aboutMet;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
