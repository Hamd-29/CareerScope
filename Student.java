public class Student {

    private int id;
    private String name;
    private String email;
    private String password;

    private String programmingSkills;
    private String dsaKnowledge;
    private String communicationSkills;
    private String aptitude;
    private String projects;
    private String certifications;
    private String workExperience;
    private String careerInterest;

    private double score;

    public Student() {
    }

    public Student(int id, String name, String email, String password,
                   String programmingSkills,
                   String dsaKnowledge,
                   String communicationSkills,
                   String aptitude,
                   String projects,
                   String certifications,
                   String workExperience,
                   String careerInterest) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;

        this.programmingSkills = programmingSkills;
        this.dsaKnowledge = dsaKnowledge;
        this.communicationSkills = communicationSkills;
        this.aptitude = aptitude;
        this.projects = projects;
        this.certifications = certifications;
        this.workExperience = workExperience;
        this.careerInterest = careerInterest;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getProgrammingSkills() {
        return programmingSkills;
    }

    public String getDsaKnowledge() {
        return dsaKnowledge;
    }

    public String getCommunicationSkills() {
        return communicationSkills;
    }

    public String getAptitude() {
        return aptitude;
    }

    public String getProjects() {
        return projects;
    }

    public String getCertifications() {
        return certifications;
    }

    public String getWorkExperience() {
        return workExperience;
    }

    public String getCareerInterest() {
        return careerInterest;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}