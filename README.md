# <tt>txt2wav</tt>: A MaryTTS example project

An example project demonstrating use of MaryTTS in a deliberately standalone application

## Build instructions

Build with [Gradle](http://gradle.org/):
```
$ ./gradlew shadowJar
```

## Usage

```
$ java -jar build/libs/txt2wav-1.0-SNAPSHOT.jar
usage: txt2wav [-i <FILE>] -o <FILE>
 -i,--input <FILE>    Read input from FILE
                      (otherwise, read from command line argument)
 -o,--output <FILE>   Write output to FILE
```

## Source code

This project is implemented in different languages/build tools, please refer to the corresponding git branches:

* [Java/Maven](https://github.com/marytts/marytts-txt2wav/tree/maven)
* **Java/Gradle**
