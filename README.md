<img src="doc/logo.png" align="right" width="180px"/>

# Chipper Toolbox
![Current version](https://img.shields.io/maven-metadata/v?label=current%20version&metadataUrl=https%3A%2F%2Frepo.unascribed.com%2Fcom%2Fplaysawdust%2Ftoolbox%2Fmaven-metadata.xml&style=flat-square)

A somewhat opinionated collection of assorted utilities for Java, building upon Google Guava.
Toolbox has a long and storied history being carried around between various projects, with the first
inklings of it appearing in 2012, before assuming its final form as Toolbox under the Chipper
project in 2019.

## Installation
Toolbox is distributed on Una's personal Maven for now, repo.unascribed.com. Here's how you can use
it in Gradle:

```gradle
repositories {
	maven {
		url "https://repo.unascribed.com"
	}
}
```

And you can add Toolbox as a dependency as such:

```gradle
dependencies {
	implementation "com.playsawdust:toolbox:2.1.2"
}
```

## Extra Dependencies
If you want to use `toolbox.io.Directories`, more specifically the platform autodetection (i.e.
without calling `setProvider`) or the WindowsDirectoryProvider implementation, you need JNA and JNA
Platform on the classpath so that `SHGetKnownFolderPath` can be invoked, as well as to perform
reliable platform detection. This dependency is *not* pulled in transitively by default because it's
a large library.

You can pull in the required JNA libraries in Gradle like so:
```gradle
dependencies {
	implementation 'net.java.dev.jna:jna:5.0.0'
	implementation 'net.java.dev.jna:jna-platform:5.0.0'
}
```
