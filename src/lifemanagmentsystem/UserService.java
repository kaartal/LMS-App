package lifemanagmentsystem;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class UserService {

    private final MongoCollection<Document> users;

    public UserService() {
        users = MongodbConnection.getDatabase().getCollection("usersLMS");
    }

    public boolean registerUser(String name, String lastname, String email, String password, String theme) {
        if (users.find(new Document("email", email)).first() != null) return false;

        Document user = new Document("name", name)
                .append("lastname", lastname)
                .append("email", email)
                .append("password", password)
                .append("theme", theme);
        users.insertOne(user);
        return true;
    }


    public boolean updateUserTheme(String email, String name, String lastname, String password, String theme) {
        Document update = new Document("name", name)
                .append("lastname", lastname)
                .append("password", password)
                .append("theme", theme);


        return users.updateOne(
                new Document("email", email),
                new Document("$set", update)

        ).getModifiedCount() > 0;
    }

    public String getUserTheme(String email) {
        Document user = getUserByEmail(email);
        if (user.getString("theme") != null) {
            return user.getString("theme");
        }
        return "Zelena";
    }



    // LOGIN USER
    public boolean loginUser(String email, String password) {
        Document user = users.find(new Document("email", email)).first();
        if (user != null && user.getString("password").equals(password)) {
            return true;
        }
        return false; // WRONG EMAIL OR PASSWORD
    }

    public Document getUserByEmail(String email) {

        return users.find(new Document("email", email)).first();
    }

    public boolean updateUser(String email, String name, String lastname, String password, String theme) {
        Document update = new Document("name", name)
                .append("lastname", lastname)
                .append("password", password);

        return users.updateOne(
                new Document("email", email),
                new Document("$set", update)
        ).getModifiedCount() > 0;
    }

}
