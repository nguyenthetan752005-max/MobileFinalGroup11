package hcmute.edu.vn.nguyenthetan;

import android.app.Application;

import hcmute.edu.vn.nguyenthetan.core.di.AppContainer;

public class TungTungApplication extends Application {

    private final Object appContainerLock = new Object();
    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public AppContainer getAppContainer() {
        if (appContainer == null) {
            synchronized (appContainerLock) {
                if (appContainer == null) {
                    appContainer = new AppContainer(this);
                }
            }
        }
        return appContainer;
    }
}
