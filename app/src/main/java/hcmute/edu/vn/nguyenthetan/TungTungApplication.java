package hcmute.edu.vn.nguyenthetan;

import android.app.Application;

import hcmute.edu.vn.nguyenthetan.core.di.AppContainer;

public class TungTungApplication extends Application {

    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }
}
