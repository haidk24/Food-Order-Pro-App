package com.example.foodorderapp.data.repository;

import com.example.foodorderapp.data.dataSource.firebase.AuthFirebaseDataSource;
import com.example.foodorderapp.data.model.User;
import com.google.android.gms.tasks.Task;

public class AuthRepository {
    private AuthFirebaseDataSource authFirebaseDataSource;
    public AuthRepository() {
        authFirebaseDataSource = new AuthFirebaseDataSource();
    }
    public Task<Void> register (String email, String password, User user){// tại sao lại là task void ?
        return authFirebaseDataSource.register(email,password,user);
    }
    public Task<User> login(String email, String password){
        return authFirebaseDataSource.login(email,password);

    }

}
