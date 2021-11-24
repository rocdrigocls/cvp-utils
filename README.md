# CVP Utils: CvpZipActivityLogs
## Utility to zip activity log files of every CVP application and minimize the problem of running out of space in CVP VXML Servers

CVP applications generate activity log files, depending on the number of users, nodes of the application, etc activity logs may grow quickly, after 100MB a new file is generated, if you don't do regular clean-up of these logs your server may run out of space.

This utility can help you to minimize the effort to maintain the server with space, you can set an automated task for example to run daily at 4 am to do the clean-up of all the applications running in your server, the script creates a zip file of each file (a file with 100MB  becomes a new one with < 9MB after compressing),  in case you need to open an old log file just unzip the file.


## Screenshots


![alt text](images/Example-beforeExecution.png "Before execution")

![alt text](images/Example-execution.png "During execution")

![alt text](images/Example-afterExecution.png "After execution")


## Build from the source code

CvpZipActivityLogs requires JRE 1.5 or later, usually the JRE inside CVP VXML Server

If you use eclipse

clone the repository

import in eclipse as "Existing Maven Projects"

right clic in the project's folder, maven update 

to build the executable jar just right clic in the folder's project: run Maven install

in the tarjet directory will be the executable jar: CvpZipActivityLogs-0.0.1.jar


```sh
cd 
npm i
node app
```




#### Running the script

Inside the install directory there are the required files to run the script:

Executable jar. CvpZipActivityLogs-0.0.1.jar (if you wish you can build it by importing the source code)

Library folder. Folder with the log4j jar needed to run

Configuration file. File with parameters needed to locate the CVP applications folder

To run the script (Open cmd console from inside install folder):

java -jar CvpZipActivityLogs-0.0.1.jar CvpZipActivityLogs.properties


###### Configuration file: 

#config.cvpAppsRootPath - The folder path on which CVP applications are stored
config.cvpAppsRootPath = C:\\Cisco\\CVP\\VXMLServer\\applications\\

#config.daysThatHaveToPassBeforeLogBecomeObsolete = Integer number that 
#determines the amount of days that have to pass for the last entry of the 
#log file to become 'Obsolete'
config.daysThatHaveToPassBeforeLogBecomeObsolete = 15

#logFile.namePattern = Regular expression pattern that will be used to match 
#and determine log files
logFile.namePattern = activity_log\\d{4}(-\\d{2}){5}\\.txt

#logFile.logEntry.date.pattern - Regular expression pattern that will be used 
#to match and determine the timestamp of the log entry
logFile.logEntry.date.pattern = .*,((\\d{2}/){2}\\d{4} \\d{2}(:\\d{2}){2}\\.\\d{3}),.*

#logFIle.logEntry.date.patternGroup - Matching group number of the above 
#regular expression that delimits the timestamp
logFile.logEntry.date.pattern.group = 1

#logFIle.logEntry.dateFormat - Date Format pattern that will be used to parse 
#back into valid dates the log entry timestamp
logFile.logEntry.date.format = MM/dd/yyyy hh:mm:ss.SSS


```sh
gulp build --prod
```

Generating pre-built zip archives for distribution:

```sh
gulp build dist --prod
```

## License

MIT
