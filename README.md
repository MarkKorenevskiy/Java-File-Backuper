# Java File Backuper
Can be used to create secure folder backups.

### General use
```
java -jar /path/to/source /path/to/destination
```
**Or compile with native-images and use as java binary file**
```
backuper /path/to/source /path/to/destination
```

### Working process
To make the copying process faster program creates `.registry.ser` file in 
destination directory. This file contains serialized `Map<String, String>` where **KEY** is destination filename and 
**VALUE** is MD5 checksum of the stored file. This allows program to pass file from source directory, 
if its contents has not been changed. If there is no `.registry.ser` file in destination folder, program will just initialize 
registry as empty Map.

### Native-image
`backuper.jar` contains all files necessary for native image compilation. If you'd like to compile 
a binary for your system, you should just call `native-image [options] -jar jarfile [imagename]`.
