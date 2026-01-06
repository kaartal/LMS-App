package lifemanagmentsystem;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongodbConnection {

    private static final String URI = "mongodb://localhost:27017";
    private static final String DB_NAME = "LMS";
    private static MongoClient client = null;
    public static MongoDatabase getDatabase() {
        if (client == null) {
            client = MongoClients.create(URI);
        }
        return client.getDatabase(DB_NAME);
    }

}
