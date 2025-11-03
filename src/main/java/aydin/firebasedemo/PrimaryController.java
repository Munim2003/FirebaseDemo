package aydin.firebasedemo;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class PrimaryController {

    @FXML
    private TextField ageTextField;

    @FXML
    private TextField nameTextField;

    // 👇 NEW
    @FXML
    private TextField phoneTextField;

    @FXML
    private TextArea outputTextArea;

    @FXML
    private Button readButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button switchSecondaryViewButton;

    @FXML
    private Button writeButton;

    private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;

    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }

    // 👇 this must be @FXML so JavaFX calls it
    @FXML
    public void initialize() {
        AccessDataView accessDataViewModel = new AccessDataView();
        nameTextField.textProperty().bindBidirectional(accessDataViewModel.personNameProperty());
        // if you also want to bind phone, update AccessDataView too
        writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());
    }

    @FXML
    void readButtonClicked(ActionEvent event) {
        readFirebase();
    }

    @FXML
    void registerButtonClicked(ActionEvent event) {
        registerUser();
    }

    @FXML
    void writeButtonClicked(ActionEvent event) {
        addData();
    }

    @FXML
    private void switchToSecondary() throws IOException {
        DemoApp.setRoot("secondary");
    }

    public boolean readFirebase() {
        key = false;

        ApiFuture<QuerySnapshot> future = DemoApp.fstore.collection("Persons").get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            outputTextArea.clear();
            listOfUsers.clear();

            for (QueryDocumentSnapshot document : documents) {
                String name = (String) document.getData().get("Name");
                Long age = (Long) document.getData().get("Age");
                String phone = (String) document.getData().get("Phone");

                outputTextArea.appendText(name + " , Age: " + age +
                        (phone != null ? (", Phone: " + phone) : "") + "\n");

                person = new Person(name,
                        age != null ? age.intValue() : 0,
                        phone);
                listOfUsers.add(person);
            }
            key = true;
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
        return key;
    }

    public boolean registerUser() {
        // this button is still here for backward compatibility
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail("user222@example.com")
                .setEmailVerified(false)
                .setPassword("secretPassword")
                .setPhoneNumber("+11234567890")
                .setDisplayName("John Doe")
                .setDisabled(false);

        try {
            UserRecord userRecord = DemoApp.fauth.createUser(request);
            System.out.println("Successfully created new user with Firebase Uid: " + userRecord.getUid());
            return true;
        } catch (FirebaseAuthException ex) {
            System.out.println("Error creating a new user in the firebase");
            return false;
        }
    }

    public void addData() {
        DocumentReference docRef = DemoApp.fstore
                .collection("Persons")
                .document(UUID.randomUUID().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("Name", nameTextField.getText());
        data.put("Age", Integer.parseInt(ageTextField.getText()));

        // 👇 NEW
        data.put("Phone", phoneTextField.getText());

        ApiFuture<WriteResult> result = docRef.set(data);
    }
}
