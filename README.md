# CityFix

CityFix lets residents report small problems in their city, such as potholes, broken streetlights, overflowing garbage bins, graffiti and water leaks. Other residents can upvote a report to show that it affects them too. City staff change the status of a report while they work on it.

This is a small learning project. It is meant for students who want to practice fixing issues and opening pull requests on GitHub.

## Tech used

- Java 21
- Spring Boot 4 (Web MVC, Data JPA, Validation) with Maven
- H2 in-memory database by default, MySQL as an option
- Plain HTML, CSS and JavaScript for the frontend
- JUnit 5, Mockito and AssertJ for tests

## Quick start

1. Install Java 21.
2. Clone this repository.
3. Start the app:
   - Windows (PowerShell): `.\mvnw.cmd spring-boot:run` (in Command Prompt, use `mvnw.cmd spring-boot:run`)
   - Mac and Linux: `./mvnw spring-boot:run`
4. Open http://localhost:8080 in your browser.

No database needs to be installed. The app uses an H2 database in memory and adds a few sample reports when it starts. The data is lost when the app stops.

To look at the tables, open http://localhost:8080/h2-console. Use the JDBC URL `jdbc:h2:mem:cityfix`, the user name `sa` and an empty password.

## Using MySQL (optional)

H2 is the default and needs no setup. Use MySQL only if you want your data to stay after the app stops.

This option has not been fully tested yet. If you hit a problem, please open an issue.

1. Install MySQL 8 or newer and make sure it is running.
2. The `cityfix` database is created automatically the first time you run the app.
3. Set your MySQL password and start the app with the `mysql` profile:
   - PowerShell:

     ```
     $env:DB_PASSWORD="your-password"
     .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
     ```

   - Mac and Linux:

     ```
     DB_PASSWORD=your-password ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
     ```

4. If you do not use the user `root`, set `DB_USERNAME` in the same way.

The settings are in `src/main/resources/application-mysql.properties`. Never write your password in that file. The app reads it from `DB_PASSWORD`.

## Troubleshooting MySQL

| Error | What to do |
|-------|------------|
| `Communications link failure` | MySQL is not running. Start the MySQL service. |
| `Access denied for user 'root'@'localhost'` | The user name or password is wrong. Set `DB_USERNAME` and `DB_PASSWORD`. |
| Errors after pulling new code | The tables may be out of date. Drop the database with `DROP DATABASE cityfix;` and start the app again. |

## How to run tests

- Windows (PowerShell): `.\mvnw.cmd test`
- Mac and Linux: `./mvnw test`

The tests use an H2 database, so nothing else needs to be installed.

## API

Base path: `/api/reports`

| Method | Path | What it does | Success |
|--------|------|--------------|---------|
| POST | `/api/reports` | Create a report | 201 |
| GET | `/api/reports` | List reports. Optional query parameters: `area`, `status`, `category`, `sort` (`newest` or `votes`) | 200 |
| GET | `/api/reports/{id}` | Get one report | 200 |
| PATCH | `/api/reports/{id}/status` | Change the status. Body: `{ "status": "IN_PROGRESS" }` | 200 |
| POST | `/api/reports/{id}/upvotes` | Upvote a report. Body: `{ "voterEmail": "ben@example.com" }` | 201 |
| GET | `/api/reports/stats` | Number of reports per status and per category | 200 |

Errors look like this:

```json
{ "status": 404, "message": "Report with id 99 not found" }
```

The request classes in the `dto` package are Java records, a short way to write a class that only holds data.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md).
