# Java Unix Shell

Lightweight Unix-like command-line shell in Java. Purpose: understand shell internals, core utilities (`echo`, `cat`)

## Features

- Interactive prompt.
- Custom Unix utilities (`echo`, `cat`).
- I/O redirection (`>`, `>>`).

## Requirements

- Java 21 with --enable-preview
- Java 22+
- Maven 3.8+

## How to run

To run this program you can do either:

### Use run.sh file
```bash
./run.sh
```


### Or run manually

Navigate to the project root

```bash
mvn -q -B package
```

```bash
java -cp target/MySh-1.0-SNAPSHOT.jar MySh.Main
```
