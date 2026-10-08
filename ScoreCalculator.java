public class ScoreCalculator {

    public static double calculateScore(Student student) {

        double score = 0;

        score += getLevelScore(student.getProgrammingSkills());
        score += getLevelScore(student.getDsaKnowledge());
        score += getLevelScore(student.getCommunicationSkills());
        score += getLevelScore(student.getAptitude());

        score += getProjectScore(student.getProjects());
        score += getCertificationScore(student.getCertifications());
        score += getExperienceScore(student.getWorkExperience());

        // Career Interest score mein count nahi hoga

        return score;
    }

    private static double getLevelScore(String level) {

        if (level == null) {
            return 0;
        }

        switch (level.toLowerCase()) {

            case "basic":
                return 1;

            case "moderate":
                return 2;

            case "high":
                return 3;

            default:
                return 0;
        }
    }

    private static double getProjectScore(String value) {

        if (value == null) {
            return 0;
        }

        switch (value.toLowerCase()) {

            case "none":
                return 0;

            case "basic":
                return 1;

            case "good":
                return 2;

            case "strong":
                return 3;

            default:
                return 0;
        }
    }

    private static double getCertificationScore(String value) {

        if (value == null) {
            return 0;
        }

        switch (value.toLowerCase()) {

            case "none":
                return 0;

            case "1-2":
                return 1;

            case "3+":
                return 2;

            default:
                return 0;
        }
    }

    private static double getExperienceScore(String value) {

        if (value == null) {
            return 0;
        }

        switch (value.toLowerCase()) {

            case "none":
                return 0;

            case "basic":
                return 1;

            case "experienced":
                return 2;

            default:
                return 0;
        }
    }
}