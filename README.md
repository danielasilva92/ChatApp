💬 ChatApp

En enkel chattapplikation byggd i Java, med JavaFX-gränssnitt, MySQL/H2-databas, DAO-lager, och säkra hashade lösenord via BCrypt.
✨ Koden är tydligt och väl kommenterad för att vara lätt att förstå.

📚 Projektet är ett skolprojekt som jag byggt vidare på från min lärares grundkod.

⚠️OBS! Du behöver själv skapa din databas-config innan projektet kan köras.(användarnamn & lösenord i application.properties)

🚀 Funktioner:

🔐 Säker inloggning med hashade lösenord (BCrypt)

🗄️ Databas via JDBC (MySQL i produktion, H2 för tester)

🧩 Tydlig lagerarkitektur (Model → DAO → Service → UI)

🖼️ JavaFX-gränssnitt med separat Launcher-klass

🧪 Enhetstester som körs mot egen in-memory databas

==============================ENGLISH=========================================
💬 ChatApp

A simple chat application built in Java, featuring a JavaFX interface, MySQL/H2 database, DAO architecture, and secure hashed passwords using BCrypt.
✨ The code is clearly structured and well-commented to make it easy to understand(swedish comments).

📚 This is a school project, extended from base code originally provided by my teacher.

⚠️ Note: You must create your own database configuration before running the project (username & password in application.properties).

🚀 Features

🔐 Secure login with hashed passwords (BCrypt)

🗄️ Database access via JDBC (MySQL in production, H2 for testing)

🧩 Clear layered architecture (Model → DAO → Service → UI)

🖼️ JavaFX interface with a dedicated launcher class

🧪 Unit tests running on an isolated in-memory test database
