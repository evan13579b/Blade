To get this code working:
If you are reading this, the source code is obviously up in the repository. 

Unfortunately its not clear at this point whether or not building with ant will work outside JME.

For one, it now requires Ant 1.7.1 while the school computers are using 1.7.0;

We are using an SDK to make this project: http://jmonkeyengine.org/downloads/, and sometimes it has failed to compile using a command-line ant invocation. 
Other times it succeeded.

We have included 3 zip files for distribution and testing. Note that due to some error its not possible to run two instances of the project on one machine. It can be ran as server or client. Choose server on one machine and choose client on another setting the server's ip address when the client popup asks.

Our project is currently developing on another repository on github at github.com/evan13579b/Blade so that we didn't force others to constantly
download our big files if they changed. We copied those project files into the school repository.

When running this, give the server time to load up. Takes about 10 seconds. Sometimes there might be a client error on startup. If that
happens try reloading.

There also is sometimes a memory limit problem. It showed up on one of our computers. If that happens please let us know. It was fixed by setting a higher
memory limit in the JVM opetions but we don't know how to set options for the distributed version.

The networking code needs a lot of work. It can be jerky when there is significant lag.
