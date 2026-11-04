# FinanceDash

A finance dashboard to view transactions and monthly budgets.

**Project still in development.**

## Features

- Add transactions via `.csv` file import
- Add custom categories and budgets
- Automatically categorise transactions on import, with a manual categorisation dialog for unknown transactions
- Filter and search transactions
- View reports on spending and budgeting

## Requirements

- **Java 27 JDK** or newer (check with `java -version`)
- Git

## Build and run

**macOS / Linux (Terminal):**

```
git clone https://github.com/LukeWatterson/FinanceDash.git
cd FinanceDash
./mvnw package
java -jar target/myapp-1.0.jar
```

**Windows, Command Prompt:**

```
git clone https://github.com/LukeWatterson/FinanceDash.git
cd FinanceDash
mvnw.cmd package
java -jar target\myapp-1.0.jar
```

**Windows, PowerShell:**

```
git clone https://github.com/LukeWatterson/FinanceDash.git
cd FinanceDash
.\mvnw.cmd package
java -jar target\myapp-1.0.jar
```

The app creates a `finance.db` file in the folder you run it from.