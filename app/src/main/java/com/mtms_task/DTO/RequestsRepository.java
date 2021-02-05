package com.mtms_task.DTO;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mtms_task.POJO.Request;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class RequestsRepository {

    private Application application;
    private MutableLiveData<ArrayList<Request>> requests;
    private CollectionReference collectionRef;

    public RequestsRepository(Application application) {
        this.application = application;
        this.requests = new MutableLiveData<ArrayList<Request>>();
        collectionRef = FirebaseFirestore.getInstance().collection("Requests");
    }

    public void readRequests(String driverKey) {
        collectionRef.whereArrayContains("drivers", driverKey)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        final ArrayList<Request> requestsList = new ArrayList<>();
                        if (e != null) {
                            Toast.makeText(application, "Listen Failed", Toast.LENGTH_SHORT).show();
                            requests.postValue(requestsList);
                            return;
                        }
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                Request request = snapshot.toObject(Request.class);
                                request.setDocumentId(snapshot.getId());         // store document id
                                requestsList.add(request);
                            }
                        }
                        requests.postValue(requestsList);
                    }
                });
    }

    public MutableLiveData<ArrayList<Request>> getRequests() {
        return requests;
    }


}
