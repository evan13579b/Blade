To get this code working:
If you are reading this, the source code is obviously up in the repository. 

Unfortunately its not clear at this point whether or not building with ant will work outside JME.

For one, it now requires Ant 1.7.1 while the school computers are using 1.7.0;

We are using an SDK to make this project: http://jmonkeyengine.org/downloads/, and sometimes it has failed to compile using a command-line ant invocation. 
Other times it succeeded.

We were going to send distributable executables, but there is one for linux, mac and windows and all three are 10mb large. This server seems to 
upload pretty slowly when we recently tried so I didn't want to force everyone in the class to have to download that on this slow server 
whenever they pull.

Our project is currently developing on another repository on github at github.com/evan13579b/Blade so that we didn't force others to constantly
download our big files if they changed. We copied those project files into the school repository.