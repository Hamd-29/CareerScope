public class StudentScore {

    private int studentId;
    private String name;
    private double score;

    public StudentScore(
            int studentId,
            String name,
            double score
    ) {
        this.studentId = studentId;
        this.name = name;
        this.score = score;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public double getScore() {
        return score;
    }
}