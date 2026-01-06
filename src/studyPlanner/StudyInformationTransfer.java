package studyPlanner;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import lifemanagmentsystem.MongodbConnection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.Map;

public class StudyInformationTransfer {
    private final MongoCollection<Document> collection;

    public StudyInformationTransfer() {
        MongoDatabase db = MongodbConnection.getDatabase();
        collection = db.getCollection("schoolTracker");
    }

    public void addOrUpdateRecord(StudyRecord record) {
        Document doc = new Document("studentEmail", record.getStudentEmailAddress());
        for (Map.Entry<String, ArrayList<Integer>> entry : record.getStudentSubjectGrades().entrySet()) {
            doc.append(entry.getKey(), entry.getValue());
        }
        collection.replaceOne(
                new Document("studentEmail", record.getStudentEmailAddress()),
                doc,
                new com.mongodb.client.model.ReplaceOptions().upsert(true)
        );
    }


   // public void addOrUpdateRecord(StudyRecord record) {
       // Document doc = new Document("studentEmail", record.getStudentEmailAddress());
        //for (Map.Entry<String, ArrayList<Integer>> entry : record.getStudentSubjectGrades().entrySet()) {
           // doc.append(entry.getKey(), entry.getValue());
        //}
       // collection.replaceOne(
              //  new Document("studentEmail", record.getStudentEmailAddress()),
              //  doc,
              //  new com.mongodb.client.model.ReplaceOptions().upsert(true)
        //);
    //}

    public StudyRecord getRecord(String studentEmail) {
        FindIterable<Document> docs = collection.find(new Document("studentEmail", studentEmail));
        Document doc = docs.first();
        StudyRecord record = new StudyRecord(studentEmail);

        if (doc != null) {
            for (String subject : record.getStudentSubjectGrades().keySet()) {
                @SuppressWarnings("unchecked")
                ArrayList<Integer> gradesFromDB = (ArrayList<Integer>) doc.get(subject);
                if (gradesFromDB != null) {
                    record.getStudentSubjectGrades().put(subject, gradesFromDB);
                }
            }
        }
        return record;
    }
}