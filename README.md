[![Maven Central](https://img.shields.io/maven-central/v/name.mjw/fortranformat.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/name.mjw/fortranformat/versions)
[![Javadoc](https://javadoc.io/badge/name.mjw/fortranformat.svg)](https://javadoc.io/doc/name.mjw/fortranformat)
[![License](https://img.shields.io/badge/License-BSD_3--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)
[![Build Status](https://github.com/mjw99/fortranformat/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/mjw99/fortranformat/actions)


## Fortranformat
A Java library for parsing and writing Fortran formats.


```
String format = "(A6,I5,1X,A4,A1,A3,1X,A1,I4,A1,3X,3F8.3,2F6.2,10X,A2,A2)";
String line = "ATOM   4314    O ARG B 132      71.840  41.784  53.751  0.62  5.36           O 0";

for (Object item: FortranFormat.read(line, format)) {
  System.out.println(item.getClass() + " : " + item);
}

//class java.lang.String : ATOM
//class java.lang.Integer : 4314
//class java.lang.String : O
//class java.lang.String : 
//class java.lang.String : ARG
//class java.lang.String : B
//class java.lang.Integer : 132
//class java.lang.String : 
//class java.lang.Double : 71.84
//class java.lang.Double : 41.784
//class java.lang.Double : 53.751
//class java.lang.Double : 0.62
//class java.lang.Double : 5.36
//class java.lang.String : O
//class java.lang.String : 0
```
