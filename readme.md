# Folder Structure Test

This project test evaluates the effect of different folder structure in latency of reading and writing of files


### Setup&Usage

Fork `master` branch into your personal repository. Clone it to local computer. Build fat jar file, you should check to see if there are any errors.

```sh
$ git clone https://github.com/fafal-abnir/Folder-Structure-Test
$ cd [project-name]
$ ./gradlew build
$ cd ./build/libs
$ java -jar fohi-all.jar -p /tmp/test -d 3 -b 256 -s 1024 -c 1000
```
It will output csv file for result of test.(e.g d:3-b:256-s:1024-c:1000.csv)

## Parameters

- --path, -p
    - Directory location for testing
- --depth, -d
    - Depth of directory structure
- --branch, -b
    - Branch size of structure(should be in power of 16)
- --size, -s
    - Size of dummy file for test
- --file-count, -c
    - File count for test
- --help, -h
    - Show options of program

## Contributing

Please feel free to give any feedback for enhancing the project
