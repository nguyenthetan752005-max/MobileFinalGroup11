package com.english.learning.dto;

import com.english.learning.entity.User;
import com.english.learning.entity.UserProgress;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminUserProfileViewDTO {
    private User userDetail;
    private List<UserProgress> recentProgress;
    private long progressCompleted;
    private long progressInProgress;
    private long progressSkipped;
    private int topScore;
    private String avgScore;
    private boolean isOnline;
}
