package data_access;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import entity.CommonAnnouncement;
import entity.Project;
import entity.ProjectFactory;
import use_case.add_email.AddEmailDataAccessInterface;
import use_case.create_announcement.CreateAnnouncementDataAccessInterface;
import use_case.create_project.CreateProjectDataAccessInterface;
import use_case.delete_announcement.DeleteAnnouncementDataAccessInterface;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class FirebaseAccessObject implements CreateProjectDataAccessInterface, AddEmailDataAccessInterface, CreateAnnouncementDataAccessInterface, DeleteAnnouncementDataAccessInterface {
    Firestore db;
    ProjectFactory projectFactory;

    private final Map<String, Integer> headers = new LinkedHashMap<>();
    private final Map<String, Project> projects = new HashMap<>();
    // Load Firebase Admin SDK credentials
    public FirebaseAccessObject() {
    FileInputStream serviceAccount;
        try {
            serviceAccount = new FileInputStream("Google_Firebase_SDK.json");
            FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.db = FirestoreClient.getFirestore();
    }

    public void save(Project project) {
            String projectName = project.getProjectName();
            String leaderEmail = project.getLeaderEmail();
            ArrayList<String> memberEmails = project.getMemberEmails();

            DocumentReference docRef = db.collection(projectName).document("projectInfo");
            Map<String, Object> data = new HashMap<>();
            data.put("projectName", projectName);
            data.put("leaderEmail", leaderEmail);
            data.put("memberEmails", memberEmails);
            ApiFuture<WriteResult> result = docRef.set(data);
    }

    Project getProject(String projectName) {
        return projects.get(projectName);
    }

    public void addMemberToProject(String projectName, String email) {
        // TODO: add member to project
    }

    public void removeMemberFromProject(String projectName, String email) {
        // TODO: remove member from project
    }

    public boolean existsByName(String newProjectName) {
        //TODO: add ways to check if newProjectName exists in db collection
        return true;
    }

    public void addAnnouncement(CommonAnnouncement announcement) {
        // Initialize Firestore if not already done
        if (db == null) {
            // Initialize Firestore
        }

        // Generate a unique ID for the announcement
        String announcementId = UUID.randomUUID().toString();

        // Convert LocalDateTime to a Firebase compatible format
        String formattedCreationTime = announcement.getCreationTime()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Create a Map to hold announcement data
        Map<String, Object> announcementData = new HashMap<>();
        announcementData.put("id", announcementId); // Add the generated ID
        announcementData.put("title", announcement.getAnnouncementTitle());
        announcementData.put("message", announcement.getMessage());
        announcementData.put("creationTime", formattedCreationTime);
        announcementData.put("author", announcement.getAuthor());

        // Use the generated ID as the document ID in Firestore
        ApiFuture<WriteResult> addedDocRef = db.collection("announcements").document(announcementId).set(announcementData);
        // Handle completion of the future
    }

    @Override
    public boolean deleteAnnouncement(String announcementId) {
        // Assuming that the Firestore database has already been initialized in the constructor
        // and db is your Firestore instance
        try {
            ApiFuture<WriteResult> writeResult = db.collection("announcements").document(announcementId).delete();
            writeResult.get(); // This line throws InterruptedException or ExecutionException
            return true; // Return true if deletion is successful
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false if an exception occurs
        }
    }

    @Override
    public CommonAnnouncement getAnnouncementById(String announcementId) {
        DocumentReference docRef = db.collection("announcements").document(announcementId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                // Constructing CommonAnnouncement from the document
                String title = document.getString("title");
                String message = document.getString("message");
                String author = document.getString("author");
                String creationTimeString = document.getString("creationTime");

                // Assuming creationTime is stored in ISO_LOCAL_DATE_TIME format
                LocalDateTime creationTime = LocalDateTime.parse(creationTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                return new CommonAnnouncement(title, message, creationTime, author);
            } else {
                // Handle the case where the announcement doesn't exist
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions
            return null;
        }
    }


}
