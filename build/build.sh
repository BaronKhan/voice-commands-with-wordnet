#!/bin/bash

PKGNAME="com/khan/baron/vcw"

OUTPUTNAME="vcw.0.0.1.jar"
SOURCEDIR="../src/com/khan/baron/voicerecrpg/system"
LIBDIR="../libs"
LIBCOMMON="$LIBDIR/commons-compress-1.15.jar"
LIBJWI="$LIBDIR/edu.mit.jwi_2.4.0.jar"
LIBPOS="$LIBDIR/stanford-postagger.jar"
LIBWS4J="$LIBDIR/ws4j-1.0.1.jar"

if [ ! -d "../src" ]; then
  echo "execute this script in the build/ dir"
  exit 0
fi

# clean up
printf "cleaning up\n"
rm -Rf *.java *.class *.jar com

mkdir -p $PKGNAME

printf "building jar\n"
javac -cp "../src/.;$LIBCOMON;$LIBJWI;$LIBPOS;$LIBWS4J" ../src/$PKGNAME/*.java && jar cvf $OUTPUTNAME ../src/$PKGNAME/*.class

printf "created jar file: $OUTPUTNAME"

rm -Rf *.java *.class com
