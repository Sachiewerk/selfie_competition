package ie.wit.witselfiecompetition.model;

/**
 * All courses in WIT 2018 as enum
 * @author Yahya Almardeny
 * @version 22/02/2018
 */

public enum Course {
    Bachelor_of_Business_Hons,
    BA_Hons_in_Accounting,
    BA_Hons_in_Marketing_And_Digital_Media,
    BA_Hons_in_International_Business,
    Bachelor_of_Business,
    BSc_in_Retail_Management,
    Higher_Certificate_in_Business,
    Engineering_Common_Entry,
    BEng_Hons_in_Sustainable_Energy_Engineering,
    BEng_Hons_in_Sustainable_Civil_Engineering,
    BEng_Hons_in_Mechanical_And_Manufacturing_Engineering,
    BEng_Hons_in_Electronic_Engineering,
    BEng_Hons_in_Electrical_Engineering,
    Higher_Certificate_in_Engineering_in_Mechanical_Engineering,
    BEng_in_Mechanical_Engineering,
    BEng_in_Manufacturing_Engineering,
    BSc_Hons_in_Manufacturing_Engineering,
    Higher_Certificate_in_Engineering_in_Electronic_Engineering,
    BEng_in_Electronic_Engineering,
    BEng_in_Electrical_Engineering,
    Higher_Certificate_in_Engineering_in_Building_Services_Engineering,
    BEng_in_Building_Services_Engineering,
    BEng_in_Civil_Engineering,
    BSc_Hons_in_Construction_Management_And_Engineering,
    BSc_Hons_in_Quantity_Surveying,
    Bachelor_of_Architecture_Hons,
    BSc_in_Architectural_Technology,
    BSc_Hons_in_Architectural_And_BIM_Technolog,
    Health_Sciences_Common_Entry,
    BSc_Hons_in_Public_Health_And_Health_Promotion,
    BSc_Hons_in_Applied_Health_Care,
    BSc_in_Applied_Health_Care,
    BSc_Hons_in_General_Nursing,
    BSc_Hons_in_Psychiatric_Nursing,
    BSc_Hons_in_Intellectual_Disability_Nursing,
    Exercise_Sciences_Common_Entry,
    BSc_Hons_in_Sport_And_Exercise_Science,
    BSc_Hons_in_Nutrition_And_Exercise_Science,
    BSc_Hons_in_Health_And_Exercise_Science,
    BSc_Hons_in_Sports_Coaching_And_Performance,
    Bachelor_of_Business_in_Recreation_And_Sport_Management,
    Bachelor_of_Business_Hons_in_Recreation_And_Sport_Management,
    Bachelor_of_Arts_Hons,
    BA_Hons_in_Psychology,
    BA_Hons_in_Social_Science,
    BA_Hons_in_Social_Care_Practice,
    BA_Hons_in_Early_Childhood_Studies,
    BA_in_Applied_Social_Studies_in_Social_Care,
    BA_Hons_in_Applied_Social_Studies_in_Social_Care,
    LLB_Bachelor_of_Laws_Hons,
    BA_Hons_in_Criminal_Justice_Studies,
    Higher_Certificate_in_Arts_in_Legal_Studies,
    BA_in_Legal_Studies_in_International_Trade,
    BA_in_Legal_Studies,
    BA_Hons_in_Legal_Studies_with_Business,
    BA_Hons_in_Hospitality_Management,
    Higher_Certificate_in_Arts_in_Hospitality_Studies,
    BA_Hons_in_Tourism_Marketing,
    Higher_Certificate_in_Business_in_Tourism,
    BA_Hons_in_Arts_in_Culinary_Arts,
    Higher_Certificate_in_Arts_in_Culinary_Arts,
    BA_Hons_in_Music,
    BA_Hons_in_Visual_Art,
    BA_Hons_in_Design_Visual_Communications,
    Science_Common_Entry,
    BSc_Hons_in_Molecular_Biology_with_Biopharmaceutical_Science,
    BSc_Hons_in_Food_Science_and_Innovation,
    BSc_Hons_in_Physics_for_Modern_Technology,
    BSc_in_Science_General,
    BSc_in_Molecular_Biology_with_Biopharmaceutical_Science,
    BSc_in_Food_Science_with_Business,
    BSc_in_Pharmaceutical_Science,
    BSc_Hons_in_Pharmaceutical_Science,
    BSc_Hons_in_Agricultural_Science,
    BSc_in_Agriculture,
    BSc_in_Forestry,
    BSc_in_Horticulture_Kildalton_College,
    BSc_in_Horticulture_National_Botanic_Gardens,
    BSc_Hons_in_Land_Management_in_Agriculture,
    BSc_Hons_in_Land_Management_in_Forestry,
    BSc_Hons_in_Land_Management_in_Horticulture,
    Applied_Computing_Common_Entry,
    BSc_Hons_in_Applied_Computing_Automotive_And_Automation_Systems,
    BSc_Hons_in_Applied_Computing_Cloud_And_Networks,
    BSc_Hons_in_Applied_Computing_Computer_Forensics_And_Security,
    BSc_Hons_in_Applied_Computing_Internet_of_Things,
    BSc_Hons_in_Applied_Computing_Games_Development,
    BSc_Hons_in_Applied_Computing_Media_Development,
    Entertainment_Systems_Common_Entry,
    BSc_Hons_in_Entertainment_Systems_Games_Development,
    BSc_Hons_in_Entertainment_Systems_Media_Development,
    BSc_Hons_in_Computer_Forensics_And_Security,
    BSc_Hons_in_the_Internet_of_Things,
    BSc_in_Software_Systems_Development,
    BSc_Hons_in_Software_Systems_Development,
    BSc_in_Multimedia_Applications_Development,
    BSc_Hons_in_Creative_Computing,
    BSc_in_Information_Technology,
    BSc_Hons_in_Information_Technology_Management;


    @Override
    public String toString() {
        return name().replace("_", " ");
    }


    /**
     * Get all courses as in an array of Strings
     * @return courses
     */
    public static String[] courses() {
        String[] courses = new String[99];
        int i=0;
        for(Course course : Course.values()) {
            courses[i++] = course.toString();
        }
        return courses;
    }


}

