package com.english.learning.service.user;

public interface UserLifecycleManagementService {
    void softDeleteUser(Long id);

    void hardDeleteUser(Long id) throws Exception;

    void restoreUser(Long id);
}

