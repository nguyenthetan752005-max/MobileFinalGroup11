package hcmute.edu.vn.nguyenthetan.domain.usecase.home;

import hcmute.edu.vn.nguyenthetan.domain.model.home.HomeDashboard;
import hcmute.edu.vn.nguyenthetan.domain.repository.HomeRepository;

public class GetHomeDashboardUseCase {

    private final HomeRepository repository;

    public GetHomeDashboardUseCase(HomeRepository repository) {
        this.repository = repository;
    }

    public HomeDashboard execute() {
        return repository.getHomeDashboard();
    }
}
