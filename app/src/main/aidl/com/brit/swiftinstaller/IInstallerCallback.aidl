// IInstallerCallback.aidl
package com.brit.swiftinstaller;

// Declare any non-default types here with import statements

interface IInstallerCallback {
    void installStarted();
    void progressUpdate(String label, int progress, int max, boolean uninstall);
    void installComplete(boolean uninstall);
    void installFailed(int reason);
}
