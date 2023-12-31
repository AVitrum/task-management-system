package com.vitrum.api.services.implementations;

import com.vitrum.api.data.models.User;
import com.vitrum.api.repositories.UserRepository;
import com.vitrum.api.data.request.ChangePasswordRequest;
import com.vitrum.api.data.request.ResetPasswordRequest;
import com.vitrum.api.data.submodels.Recoverycode;
import com.vitrum.api.repositories.RecoverycodeRepository;
import com.vitrum.api.services.interfaces.PasswordService;
import com.vitrum.api.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final RecoverycodeRepository recoverycodeRepository;
    private final MessageUtil messageUtil;

    @Override
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {
        User user = User.getUserFromPrincipal(connectedUser);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password!");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalStateException("Password is the same as the current one!");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Passwords do not match!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Wrong email!"));
        Recoverycode recoverycode;
        try {
            recoverycode = user.getRecoverycode().get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException("Recovery code not found");
        }


        if (recoverycode.isExpired()) {
            recoverycodeRepository.delete(recoverycode);
            throw new IllegalStateException("Code is expired");
        }
        if (!recoverycode.getCode().equals(request.getCode())) {
            throw new IllegalStateException("The codes do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalStateException("Password is the same as the current one!");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Passwords do not match!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
        recoverycodeRepository.delete(recoverycode);
    }

    @Override
    public void getRecoverycode(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Wrong email!"));
        Recoverycode recoverycode = generateRecoverycode(user);

        messageUtil.sendRecoverycodeByEmail(user, recoverycode);
    }

    private Recoverycode generateRecoverycode(User user) {
        if (!user.getRecoverycode().isEmpty()) {
            Recoverycode recoverycode = user.getRecoverycode().get(0);
            recoverycodeRepository.delete(recoverycode);
        }

        long code = new Random().nextLong(999999 - 100000 + 1) + 100000;
        Recoverycode recoverycode = Recoverycode.builder()
                .code(code)
                .creationTime(LocalTime.now())
                .user(user)
                .build();
        recoverycodeRepository.save(recoverycode);
        return recoverycode;
    }
}
