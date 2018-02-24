package ie.wit.witselfiecompetition;

/**
 * Created by yahya on 21/02/18.
 */

public class User {
    public enum Gender {MALE, FEMALE};
    private String fName;
    private String lName;
    private Gender gender;
    private Course course;
    private String aboutMet;
    private String image;

    public User(){

    }

    public User(String fName, String lName, Gender gender) {
        this.fName = fName;
        this.lName = lName;
        this.gender = gender;
    }

    public User(String fName, String lName, Gender gender, Course course, String aboutMet, String image) {
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
                ", gender=" + gender.toString().toLowerCase()+
                ", course=" + course.toString().toLowerCase() +
                ", aboutMet='" + aboutMet + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    public String profileSetup(){
        return "User{" +
                "fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                ", gender=" + gender +
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
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
