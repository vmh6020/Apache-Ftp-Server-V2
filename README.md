# Apache-Ftp-Server-V2

## 1. Introduction

**Apache-Ftp-Server-V2** is a lightweight, web-based file management system (Web File Explorer) built on the Java Web platform. It integrates an embedded FTP server engine (Apache FtpServer) with a dynamic web interface (JSP/Servlet) and a PostgreSQL database to handle user authentication and remote file operations seamlessly.

The project strictly adheres to the 3-tier architecture pattern (Controller-Service-Repository) to ensure a clean separation of concerns and maintainable business logic.

## 2. Tech Stack

* **Core Language:** Java


* **Web Technologies:** Java Servlets, JSP (JavaServer Pages)


* **FTP Engine:** Embedded Apache FtpServer


* **Database:** PostgreSQL


* **Build & Dependency Management:** Maven


* **Logging Framework:** Logback



## 3. Key Features

### Authentication & Security

* **User Registration & Login:** Allows users to create new accounts and authenticates active sessions to grant access to dedicated storage spaces.


* **Password Protection:** Securely hashes user passwords using a dedicated utility (`PasswordUtil`) before persisting them into the PostgreSQL database.


* **Session Management:** Secure logout handling to invalidate active user sessions safely.



### Remote File Operations

* **File Browser:** Visually displays remote files and folder hierarchies directly on the web interface.


* **File Transfer:** Supports seamless uploading of files to the server and downloading files to the local machine.


* **Data Manipulation:** Enables direct text content editing via the web UI, file/folder renaming, and permanent deletion.



## 4. Architecture & Project Structure

The codebase is structured into cleanly separated, independent layers:

```text
src/main/java/com/kev/ftpserver/
├── controller/               # Handles HTTP requests and manages UI routing 
│   ├── auth/                 # LoginServlet, LogoutServlet, RegisterServlet 
│   └── file/                 # FileBrowser, Upload, Download, Edit, Rename, Delete Servlets 
├── model/                    # Data entities (FTPAccount, FileItem, FileObject, FolderObject) 
├── repository/               # Low-level data access for PostgreSQL and FTP setup (UserRepository, FtpRepository) 
├── service/                  # Core business logic processing (AuthService, FileService, ConfigService) 
│   └── dto/                  # Data Transfer Objects (LoginResult, RegisterRequest) 
└── util/                     # Helper utilities for encryption and hashing (PasswordUtil) 

```

Web assets and configuration resources are organized as follows:

```text
src/main/
├── resources/                # App properties (config.properties) and log configurations (logback.xml)
└── webapp/                   # Web application root directory
    ├── WEB-INF/              # Deployment descriptors and configuration blocks (web.xml, beans.xml)
    └── jsp/                  # Dynamic UI views (index.jsp, files.jsp, register.jsp, edit.jsp)

```

## 5. Getting Started

### Prerequisites

* Java Development Kit (JDK) 8 or higher.
* PostgreSQL database server.
* Maven build tool (or use the included Maven wrapper `mvnw`).



### Installation & Setup

1. **Database Configuration:**
* Create a new database instance in PostgreSQL.


* Open `src/main/resources/config.properties` and update the database credentials (JDBC URL, username, password) to match your local setup.




2. **Build the Project:**
Clean and compile the project dependencies using the Maven wrapper:


```bash
./mvnw clean install

```



```

3.  **Deployment:**
    Deploy the generated `.war` file to a servlet container (such as Apache Tomcat) or run it using integrated server plugins defined in your `pom.xml`
```
