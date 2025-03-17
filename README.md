Online Cab Booking App - Backend
This is the backend repository for the Online Cab Booking App, a web-based application that allows users to book cabs online. The backend is built using modern technologies to ensure scalability, security, and performance.

Features
User authentication and authorization (JWT-based)

Cab search and booking functionality

Real-time cab availability tracking

Ride history and user profile management

Admin panel for managing cabs, drivers, and bookings

RESTful API design for seamless integration with frontend

Technologies Used
Programming Language: Java (Spring Boot)

Database: MySQL (or any relational database)

Authentication: JSON Web Tokens (JWT)

API Documentation: Swagger/OpenAPI

Build Tool: Maven

Version Control: Git

Prerequisites
Before running the project, ensure you have the following installed:

Java Development Kit (JDK) 17 or higher

MySQL or any compatible relational database

Maven (for dependency management)

Postman or any API testing tool (for testing endpoints)

Setup Instructions
Clone the Repository:

bash
Copy
git clone https://github.com/Uvindu28/Online-Cab-Booking-App-Backend.git
cd Online-Cab-Booking-App-Backend
Configure the Database:

Create a MySQL database (e.g., cab_booking_db).

Update the application.properties file with your database credentials:

properties
Copy
spring.datasource.url=jdbc:mysql://localhost:3306/cab_booking_db
spring.datasource.username=your_username
spring.datasource.password=your_password
Build the Project:

bash
Copy
mvn clean install
Run the Application:

bash
Copy
mvn spring-boot:run
Access the API:

The backend will start running on http://localhost:8080.

Use Swagger UI to explore the API endpoints: http://localhost:8080/swagger-ui.html.

API Endpoints
Here are some of the key API endpoints:

User Authentication:

POST /api/auth/signup - Register a new user.

POST /api/auth/login - Authenticate and generate a JWT token.

Cab Booking:

GET /api/cabs - Get available cabs.

POST /api/bookings - Book a cab.

GET /api/bookings/{id} - Get booking details.

Admin:

POST /api/admin/cabs - Add a new cab (Admin only).

DELETE /api/admin/cabs/{id} - Remove a cab (Admin only).

For a complete list of endpoints, refer to the Swagger documentation.

Contributing
Contributions are welcome! If you'd like to contribute to this project, please follow these steps:

Fork the repository.

Create a new branch for your feature or bugfix.

Commit your changes and push to the branch.

Submit a pull request with a detailed description of your changes.

License
This project is licensed under the MIT License. See the LICENSE file for details.

Contact
For any questions or feedback, feel free to reach out:

Uvindu28 - GitHub Profile

Email: your-email@example.com
