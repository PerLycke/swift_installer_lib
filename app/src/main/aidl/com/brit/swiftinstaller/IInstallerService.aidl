// IInstallerService.aidl
package com.brit.swiftinstaller;

// Declare any non-default types here with import statements
import com.brit.swiftinstaller.IInstallerCallback;
import java.util.List;
interface IInstallerService {
    void setCallback(IInstallerCallback callback);
    void startInstall(inout List<String> apps);
    void startUninstall(String app);
    void updateApp(String packageName);
}
