package com.example.suresh.mychattapplication.Models;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.suresh.mychattapplication.Controllers.HomePage;
import com.example.suresh.mychattapplication.Controllers.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.concurrent.Executor;

import static android.content.ContentValues.TAG;

public class FirebaseDAO implements Serializable{

    // objects for firebase Realtime database
    private FirebaseDatabase connectionObject;
    private DatabaseReference dbReference;
    private boolean flag;
    //objects for firebase user authentication
    private FirebaseAuth authenticationObject;
    private FirebaseUser firebaseUser;

    //objects for firebase storage
    private FirebaseStorage storageObject;
    private StorageReference storageReference;

    //static firebaseDAO object and accor to the all the objects specified above
    private static FirebaseDAO firebaseDAOObject;



    private FirebaseDAO(){
        //link to database url
            connectionObject = FirebaseDatabase.getInstance();

        //link to parent node or root node in database
        dbReference=connectionObject.getReference();

        //object for handling user authentication
        authenticationObject=FirebaseAuth.getInstance();

        //current user object logged into the database
        firebaseUser=authenticationObject.getCurrentUser();

        //obtaining storage object
        storageObject=FirebaseStorage.getInstance();

        //obtaining root storage reference
        storageReference=storageObject.getReference();


    }

    public FirebaseStorage getStorageObject() {
        return storageObject;
    }

    public void setStorageObject(FirebaseStorage storageObject) {
        this.storageObject = storageObject;
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }

    public void setStorageReference(StorageReference storageReference) {
        this.storageReference = storageReference;
    }

    public FirebaseDatabase getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(FirebaseDatabase connectionObject) {
        this.connectionObject = connectionObject;
    }

    public DatabaseReference getDbReference() {
        return dbReference;
    }

    public void setDbReference(DatabaseReference dbReference) {
        this.dbReference = dbReference;
    }

    public FirebaseAuth getAuthenticationObject() {
        return authenticationObject;
    }

    public void setAuthenticationObject(FirebaseAuth authenticationObject) {
        this.authenticationObject = authenticationObject;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public void setFirebasUser(FirebaseUser firebasUser){
        this.firebaseUser=firebasUser;
    }

    private void setFirebaseUser(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    public static FirebaseDAO getFirebaseDAOObject() {

        if(firebaseDAOObject==null)
            return firebaseDAOObject= new FirebaseDAO();
        else
            return firebaseDAOObject;
    }

    private void insertUser(User userInstance){
        getDbReference().child("users").child(userInstance.getUserID()).child("fName").setValue(userInstance.getFirstName());
        getDbReference().child("users").child(userInstance.getUserID()).child("lName").setValue(userInstance.getLastName());
        getDbReference().child("users").child(userInstance.getUserID()).child("DOB").setValue(userInstance.getDOB());
        getDbReference().child("users").child(userInstance.getUserID()).child("country").setValue(userInstance.getCountry());
        getDbReference().child("users").child(userInstance.getUserID()).child("state").setValue(userInstance.getState());
        getDbReference().child("users").child(userInstance.getUserID()).child("homeAddress").setValue(userInstance.getHomeAddress());
        getDbReference().child("users").child(userInstance.getUserID()).child("phone").setValue(userInstance.getPhone());
        getDbReference().child("users").child(userInstance.getUserID()).child("email").setValue(userInstance.getEmail());
        getDbReference().child("users").child(userInstance.getUserID()).child("password").setValue(userInstance.getPassword());
        getDbReference().child("users").child(userInstance.getUserID()).child("username").setValue(userInstance.getUsername());
        getDbReference().child("users").child(userInstance.getUserID()).child("status").setValue("Hello All! I am using Mychat App!");
        getDbReference().child("users").child(userInstance.getUserID()).child("pp_path").setValue("");

    }

    public boolean userLogin(final User userInstance){

        getAuthenticationObject().signInWithEmailAndPassword(userInstance.getEmail(), userInstance.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            setFirebaseUser(authenticationObject.getCurrentUser());
                            flag=true;

                        }
                        else {
                            flag=false;
                        }
                    }
                });
        Log.d("*____________FLAG_____*",""+flag);
        return flag;

    }

    public void userLogOut() throws FirebaseException{
        authenticationObject.signOut();
    }

    public boolean userSignup(final User userInstance){

        authenticationObject.createUserWithEmailAndPassword(userInstance.getEmail(), userInstance.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            setFirebaseUser(authenticationObject.getCurrentUser());
                            userInstance.setUserID(getFirebaseUser().getUid());
                            // Sign in success, update UI with the signed-in user's information
                            insertUser(userInstance);

                            flag=true;
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w("failure", task.getException());
                            flag=false;
                        }

                        // ...
                    }
                });

        return flag;
    }


}
