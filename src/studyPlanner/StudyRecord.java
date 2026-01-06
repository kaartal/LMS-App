package studyPlanner;

import java.util.*;

public class StudyRecord {
    private String studentEmailAddress;
    private Map<String, ArrayList<Integer>> studentSubjectGrades;

    public StudyRecord(String studentEmailAddress) {
        this.studentEmailAddress = studentEmailAddress;
        this.studentSubjectGrades = new HashMap<>();

        String[] schoolSubjectList = {
                "Matematika", "Bosanski", "Engleski", "Fizika",
                "Hemija", "Biologija", "Historija", "Geografija",
                "Informatika", "Likovno"
        };

        for (String currentSubjectName : schoolSubjectList) {
            studentSubjectGrades.put(currentSubjectName, new ArrayList<>());
        }
    }

    public String getStudentEmailAddress() {
        return studentEmailAddress;
    }

    public Map<String, ArrayList<Integer>> getStudentSubjectGrades() {
        return studentSubjectGrades;
    }

    public void addGradeToSubject(String targetSubjectName, int newGradeValue) {
        if (studentSubjectGrades.containsKey(targetSubjectName) && newGradeValue >= 1 && newGradeValue <= 5) {
            studentSubjectGrades.get(targetSubjectName).add(newGradeValue);
        }
    }

    public double calculateSubjectAverage(String targetSubjectName) {
        ArrayList<Integer> specificSubjectGradesList = studentSubjectGrades.get(targetSubjectName);
        if (specificSubjectGradesList == null || specificSubjectGradesList.isEmpty()) return 0.0;

        int totalSumOfGrades = 0;
        for (int individualGradeValue : specificSubjectGradesList) {
            totalSumOfGrades += individualGradeValue;
        }
        return (double) totalSumOfGrades / specificSubjectGradesList.size();
    }

    public double calculateOverallGradeAverage() {
        double totalSumOfAllGrades = 0;
        int totalNumberOfGradesCount = 0;

        for (String currentSubjectKey : studentSubjectGrades.keySet()) {
            ArrayList<Integer> currentSubjectGradesList = studentSubjectGrades.get(currentSubjectKey);
            for (int individualGradeValue : currentSubjectGradesList) {
                totalSumOfAllGrades += individualGradeValue;
                totalNumberOfGradesCount++;
            }
        }
        return totalNumberOfGradesCount == 0 ? 0.0 : totalSumOfAllGrades / totalNumberOfGradesCount;
    }
}