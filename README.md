# CVP Utils: CvpZipActivityLogs
## Utility to zip activity log files of every CVP application and minimize the problem of running out of space in CVP VXML Servers


Dillinger is a cloud-enabled, mobile-ready, offline-storage compatible,


## Features

- Import a HTML file and 

![alt text](images/Example-beforeExecution.png "Before execution")

![alt text](images/Example-execution.png "Before execution")

![alt text](images/Example-afterExecution.png "Before execution")


## Tech

 uses a number of open source projects to work properly:

- [AngularJS] - HTML enhanced for web apps!


And of course  itself is open source with a [public repository][dill]
 on GitHub.

## Installation

CvpZipActivityLogs requires JRE 1.5 or later, usually the JRE inside CVP VXML Server

Install the dependencies and devDependencies and start the server.

```sh
cd 
npm i
node app
```

For production environments...

```sh
npm install --production
NODE_ENV=production node app
```


#### Building from source code

For production release:

```sh
gulp build --prod
```

Generating pre-built zip archives for distribution:

```sh
gulp build dist --prod
```

## License

MIT
