# libmergea4toa3

Thank you for coming by, this is a library for merging DIN A4 scanned fragments to obtain a single DIN A3 image, very useful when you don't have an DIN A3 specific scanner, but a DIN A4 one.

This contains the required classes, methods and functions needed to:

 * Merge images from left to right, with low angle deviation and big x/y offset.
 * Usage by another GUI program, including execution on thread and status report to GUI.
 
If looking for the actual GUI (user-friendly) program using this library, please go to [https://github.com/davovoid/mergea4toa3](https://github.com/davovoid/mergea4toa3).

### How to compile the project

The project is based on Java and Maven, so a Maven-friendly environment would be desired.

* Download the project from this GitHub repository or execute the following command using git:

```
git clone https://github.com/davovoid/libmergea4toa3.git
```

* Execute the `clean install` goal from your favorite Maven-friendly IDE or using the following Maven command:

```
mvn clean install
```

* An A4 to A3 merging test will be performed (it may take some minutes). After that, the Maven command line should build successfully.

* Now you are ready to go!