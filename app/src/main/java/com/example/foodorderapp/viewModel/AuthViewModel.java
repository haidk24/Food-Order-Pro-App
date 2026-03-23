package com.example.foodorderapp.viewModel;

import com.example.foodorderapp.data.model.User;
import com.example.foodorderapp.data.repository.AuthRepository;
import com.google.android.gms.tasks.Task;

public class AuthViewModel {
private AuthRepository authRepository;

    public AuthViewModel() {
        authRepository = new AuthRepository();
    }

   public Task<Void> register(String email, String password, User user) {
        return authRepository.register(email, password, user);
    }

    public Task<User> login(String email, String password) {
        return authRepository.login(email, password);
    }
}
