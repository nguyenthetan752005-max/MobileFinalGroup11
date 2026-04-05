package hcmute.edu.vn.nguyenthetan.domain.usecase.profile;

import hcmute.edu.vn.nguyenthetan.domain.model.profile.ProfileData;
import hcmute.edu.vn.nguyenthetan.domain.repository.ProfileRepository;

public class GetProfileUseCase {

    private final ProfileRepository repository;

    public GetProfileUseCase(ProfileRepository repository) {
        this.repository = repository;
    }

    public ProfileData execute() {
        return repository.getProfile();
    }
}
