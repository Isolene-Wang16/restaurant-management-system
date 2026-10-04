# Restaurant Kitchen Order Management System
A console-based Java course project for restaurant kitchen order management.

## Project Overview
This modular system enables restaurant staff to create orders, update order status, maintain menu data, query historical records, and generate business reports. It also implements overdue order timeout detection and basic user access control, with local file I/O for persistent data storage.

## Core Features
- Create and submit new customer orders
- Modify and track real-time order status
- Menu information add, edit and maintenance
- Order history filtering and query
- Automatic timeout detection for overdue orders
- User management and permission control
- Generate formatted operational reports
- Persist data to local files

## Tech Stack
- Programming Language: Java
- Storage: Local text file I/O
- Design: Object-oriented modular design, separated business logic and utility functions

## Project Structure
```
restaurant/
├── manager/        # Core business logic modules
│   ├── ReportGenerator.java
│   ├── TimeoutChecker.java
│   └── UserManager.java
└── util/           # Helper and file utility classes
└── FileUtil.java
```

## How to Run
1. Download all `.java` source files
2. Compile source code using `javac`
3. Execute the main class to launch the console program

## Project Note
This is an academic Java programming project.
