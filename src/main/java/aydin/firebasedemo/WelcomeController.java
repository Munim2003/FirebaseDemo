package aydin.firebasedemo;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class WelcomeController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    // (a) REGISTER
    @FXML
    private void onRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Email and password required.");
            return;
        }

        // 1) create user in Firebase Auth
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setEmailVerified(false)
                .setDisabled(false);

        try {
            UserRecord userRecord = DemoApp.fauth.createUser(request);
            System.out.println("Created user: " + userRecord.getUid());

            // 2) ALSO save the password in Firestore (because Admin SDK cannot check password later)
            DocumentReference docRef = DemoApp.fstore.collection("Users").document(email);
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("password", password);
            docRef.set(data);   // async is fine here

            statusLabel.setText("Registered ✅. Now Sign In.");
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            statusLabel.setText("Register failed: " + e.getMessage());
        }
    }

    // (a) SIGN IN
    @FXML
    private void onSignIn() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Enter email + password.");
            return;
        }

        // We can't "auth.signInWithEmailAndPassword" in Admin SDK. So:
        // we verify against Firestore "Users" collection we just wrote.
        DocumentReference docRef = DemoApp.fstore.collection("Users").document(email);
        ApiFuture<DocumentSnapshot> future = docRef.get();

        try {
            DocumentSnapshot snap = future.get();
            if (!snap.exists()) {
                statusLabel.setText("User not found.");
                return;
            }
            String savedPass = snap.getString("password");
            if (savedPass != null && savedPass.equals(password)) {
                // password correct → go to data access screen
                DemoApp.setRoot("primary");
            } else {
                statusLabel.setText("Wrong password.");
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            statusLabel.setText("Sign in error.");
        }
    }
}
