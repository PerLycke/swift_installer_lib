package com.brit.swiftinstaller;

import android.support.annotation.Keep;

import com.brit.swiftinstaller.utils.ShellUtilsKt;

public class Installer {

    @Keep
    public static void main(String[] args) {
        System.out.println("TESTING");
        ShellUtilsKt.runCommand("cmd overlay list");
    }
}
