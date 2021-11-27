### Prepare
The sample code contains client and server code. First import the source code through Android Studio
Ensure that the apk file is compiled and installed.At the same time, we need to ensure that the 
network between the devices is normal. If there is no wifi available, I suggest installing two apks 
on the same phone.
 

### How to use
Before using it, we need to start the server apk. It has a blank page. When we see the page, 
the service has been started. If the client and server applications are installed on the same phone
we can click the HOME button to exit the server to the background.Then, click on the client on the 
desktop to start the client program, and we will first see the login page.

On the login page, we need to enter the ip address of the device installed by the server application.
If server and client apks are installed on the same device, we can fill in 127.0.0.1, which is also
the default host ip address.If it is installed on a different mobile phone, we need to check the ip address 
of the device where the server apk is located, and then enter it.In order to facilitate testing, 
we recommend that you install the two apk in the same phone.

In the assets directory of the server, we have added a test account test to the configuration file server.xml, 
and we can use this account for login access.If everything is normal, we can enter the main page

On the main page we can see the basic function buttons of client，Such as uploading, downloading files or folders
And you can view the list of downloaded files。

### Precautions
Since it is not possible to select an available folder, when uploading the folder, we first need to push a 
folder named **ftp_upload_folder**  in the external storage, we can use the adb command to complete。
Before pushing, save the file in the ftp_upload_folder directory.

```bash
adb push ftp_upload_folder /sdcard/
```

note: You can also use the file manager to create this directory.Then import the test file to this directory。

2. If the client and server apk are installed on the same mobile phone, we need to complete the operation 
on the client as soon as possible, because the server program may be killed by the system in the background 
for a period of time, which will cause the client service to be unavailable or unavailable. response. 
If the service is unavailable, we can kill the two applications and start using the operation again.

### permission 

During use, the application will apply for storage access, please be sure to select Allow, because we 
need to access external files to upload.








