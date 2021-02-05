package com.mtms_task.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.mtms_task.DTO.RequestsRepository;
import com.mtms_task.POJO.Request;

import java.util.ArrayList;

public class RequestsViewModel extends AndroidViewModel {
    private RequestsRepository repository;
    private MutableLiveData<ArrayList<Request>> requests;

    public RequestsViewModel(@NonNull Application application) {
        super(application);
        repository = new RequestsRepository(application);
        requests = repository.getRequests();
    }

    public MutableLiveData<ArrayList<Request>> readRequests(String driverKey) {
        repository.readRequests(driverKey);
        return requests;
    }

}
