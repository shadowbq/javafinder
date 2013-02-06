JavaFinder
---------

Author: Java Finder by petrucio@stackoverflow(828681)

*Does anyone know how to programatically get all JVMs installed (not the default one) using Java?*

This is a windows solution that reads 32 bit and 64 registry keys to find installed versions of JRE & JDK.

## Example Run


```dos
c:\> cd lib
c:\lib\> java JavaFinder

C:\Program Files (x86)\Java\jre6\bin\java.exe:
  Version: 1.6.0_31
  Bitness: 32-bits

C:\Program Files\Java\jre6\bin\java.exe:
  Version: 1.6.0_31
  Bitness: 64-bits

D:\Dev\Java\jdk1.6.0_31\bin\java.exe:
  Version: 1.6.0_31
  Bitness: 64-bits

C:\Windows\system32\java.exe:
  Version: 1.6.0_31
  Bitness: 64-bits

C:\Windows\SysWOW64\java.exe:
  Version: 1.6.0_31
  Bitness: 32-bits
```

Development Notes
---------

Compilation, packaging and listing of the jar file contents

```shell
$> cd ./src/
$> javac JavaFinder.java 

$> jar cf javafinder ./lib/ 
JavaFinder.class       JavaInfo.class         RuntimeStreamer.class  WinRegistry.class 

$> jar tf javafinder 
META-INF/
META-INF/MANIFEST.MF
lib/JavaFinder.class
#*.class
lib/JavaInfo.class
lib/RuntimeStreamer.class
lib/WinRegistry.class
```

Invoking particular Java 
---------
You can invoke particular java in this manner on windows

```shell
java -version:1.6 -version
```
