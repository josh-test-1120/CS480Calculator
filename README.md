Declarative approach to Calculator Application

Installing this requires Android Studio to compile the Kotlin and java dependencies. Please ensure
that you are running Android Studio Iguana | 2023.2.1 RC 2 for compatibility purposes (Latest versions
might not work properly with all libraries).

Android Studio Versions:
[Mac OSx Silicon](https://redirector.gvt1.com/edgedl/android/studio/install/2023.2.1.22/android-studio-2023.2.1.22-mac_arm.dmg)
[Mac OSx Intel](https://redirector.gvt1.com/edgedl/android/studio/install/2023.2.1.22/android-studio-2023.2.1.22-mac.dmg)
[Windows Intel](https://redirector.gvt1.com/edgedl/android/studio/install/2023.2.1.22/android-studio-2023.2.1.22-windows.exe)

Do not auto-update the version of Android studio once you launch it, even though it asks you to. This 
is needed to ensure compatibility with the build and implementation I used for this project.

Once the Android Studio is installed, please ensure you are using gradle version 8.4 for compilation, 
and that you have configured API 34 (UpsideDownCake Android 14.0) as the android API to build against. 
Once these are configured, if not already configured, Android studio should download all the libraries 
and build the application.

For optimal results, please ensure you are using at least the Medium Phone VM device for emulation. 
Larger images will work, but that is what this was built and tested against.
