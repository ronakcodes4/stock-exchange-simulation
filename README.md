# DePaul Stock Exchange: An Object-Oriented Trading Simulation

This project is a comprehensive, multi-threaded stock exchange simulation developed for my SE 450: Object-Oriented Software Development course at DePaul University. The application demonstrates advanced software architecture and the practical implementation of key object-oriented design patterns to solve complex engineering problems.

## Core Features

* **Real-time Trading:** Simulates a live market environment where multiple users can submit buy/sell orders and quotes for various stock products.
* [cite_start]**Product and User Management:** Utilizes Façade and Singleton patterns through a `ProductManager` and `UserManager` to provide a centralized and controlled interface for managing all products and users in the system. [cite: 84, 62]
* [cite_start]**Dynamic Market Data:** Implements the Observer pattern, where a `CurrentMarketPublisher` notifies subscribed users of real-time changes to the top-of-book prices and volumes. [cite: 765, 799]
* [cite_start]**Efficient Resource Management:** Leverages the Flyweight pattern in a `PriceFactory` to minimize memory usage by ensuring that only one instance of any given price object is created and shared throughout the application. [cite: 27]

## Purpose & Skills Demonstrated

This project showcases my ability to:
* Design and architect complex, multi-component systems.
* Apply fundamental and advanced object-oriented design patterns to create maintainable and scalable software.
* Develop robust, multi-threaded applications capable of handling concurrent operations.
* Manage the full lifecycle of a software project from requirements to implementation.
