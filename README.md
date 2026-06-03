# Simple-Nintendo-Consoles-Homebrew-Java-Updater
A simple program made in Java to download some of the nintendo console homebrew apps.
# How to use
Just execute the .jar provided and it will list the apps directly from github releases, you can download everything or select some apps from the table to only download those. All files will be downloaded to /Downloads/NxAppDownloader/
# How to add new apps
You can add them directly in Logic.AppsManagement with NxApp Constructor, only the repo's name and url is needed, since it checks the rest of the info from github
# Required Dependencies
For building/running this program at least is required the following dependencies:
- Java Development Kit 23 (JDK 23)
- Jackson Core, Annotations, databinds and datatypes 1.28.3 fro JSON works, be aware that usign other versions may cause version dependant errors
- github-api for connecting to github at least 1.327 since 2.0 is a release candidate (rc), this version was using instead
- commons-lang3 3.18/0 and commons-io 2.22.0.
In case of building by your own, you can provide a github token to improve loading versions and downloading files speed. *DO NOT SHARE THIS TOKEN*
# Thanks
Many thanks to every developer that made all of these amazing homebrew apps for each of the included consoles, and thanks to the developers of the aforementioned dependencies for their work
# Why use this instead of other apps like the hb-appstore or insert any other app like this?
This app is made as a hobby and to learn to connect github and https requests to java, and also for personal, use, however if anyone needs this application they are free to use it or modify it. Also because i like to keep my apps stored for any case, any kind of feedback or help is always welcome.
