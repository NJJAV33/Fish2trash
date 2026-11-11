#!/bin/sh
# Finds a JDK 17 or newer and sets JAVAC and JAVA.
# Not run directly: build.sh, run.sh and test.sh load it with ". scripts/jdk.sh".

# True if the JDK in the given folder can compile for Java 17.
is_jdk17() {
    [ -x "$1/bin/javac" ] && "$1/bin/javac" --release 17 -version >/dev/null 2>&1
}

JDK=""
for candidate in "$JAVA_HOME" /usr/lib/jvm/java-21-openjdk /usr/lib/jvm/java-17-openjdk; do
    if [ -n "$candidate" ] && is_jdk17 "$candidate"; then
        JDK="$candidate"
        break
    fi
done

if [ -n "$JDK" ]; then
    JAVAC="$JDK/bin/javac"
    JAVA="$JDK/bin/java"
elif command -v javac >/dev/null 2>&1 && javac --release 17 -version >/dev/null 2>&1; then
    # Fall back to whatever JDK is on the PATH.
    JAVAC=javac
    JAVA=java
else
    echo "Need JDK 17+. Install one, or point JAVA_HOME at it." >&2
    exit 1
fi
