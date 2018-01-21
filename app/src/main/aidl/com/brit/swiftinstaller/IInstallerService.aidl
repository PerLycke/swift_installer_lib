// IInstallerService.aidl
package com.brit.swiftinstaller;

// Declare any non-default types here with import statements
import com.brit.swiftinstaller.IInstallerCallback;
interface IInstallerService {
    void setCallback(IInstallerCallback callback);
    void startInstall(String app);
    void startUninstall(String app);
    void updateApp(String packageName);
}
