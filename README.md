# 💰 Expense Tracker API (Spring Boot)

A secure RESTful Expense Tracker API built using Spring Boot.

This application allows users to register, authenticate using JWT or Google OAUth2, and manage their personal expenses securely. Each user can perform CRUD operations only on their own expenses, with filtering, reporting, and export functionality(CSV & PDF).

Built as part of my backend development and security learning journey.

## Features 
-> Jwt Authentication 
-> Google OAuth2 Login 
-> User-based expense isolation(secure multi-user system)
-> Add, Update, Delete expenses 
-> Filter by category
-> Filter by date range
-> Category-wise expense summary 
-> Total expense calculation within date range 
-> Export expenses as CSV
-> Export expenses as PDF
-> Ownership validation & security checks 


## Tech Stack 
-> Java 17
-> Spring Boot 
-> Spring Security 
-> JWT (Json Web Token)
-> OAuth2 (Google Login)
-> Spring Data JPA 
-> MySQL
-> Maven 


## Project Structure 
-> controller -> REST endpoints
-> service -> Business Logic(PDF/CSV generation)
-> repository -> JPA repositories
-> model -> Entity classes
-> security -> JWT & OAuth2 configuration 


## Authentication Flow 
-> User registers or logs in.
-> Server generates a JWT Token.
-> Token must be included in Authorization header: 
        Authorization: Bearer<token>
-> JwtAuthFilter validtaes token for every request.
-> Users can only access their own expenses.


## API Endpoints

## AUth
POST        /auth/register
POST        /auth/login


## Expenses 
POST    /expenses
GET     /expenses
GET     /expenses/{id}
PUT     /expenses/{id}
PATCH   /expenses/{id}
DELETE  /expenses/{id}


## Reports 
GET /expenses/report/category
GET /expenses/report/total
GET /expenses/report/csv
GET /expenses/report/pdf


## How to Run 

1. Clone the repository 
2. Configure application.properties
3. Run: mcn spring-boot:run
4. Test APIs using Postman 



## Learning Outcomes 
## What I learned 

-> Implementing JWT authentication from scratch
-> Securing REST APIs using Spring Security
-> Handling OAuth2 login integration
-> Designing proper entity relationships (One-to-Many / Many-to-One)
-> Enforcing user-based data isolation
-> Building export functionality (CSV & PDF generation)



## Future Improvements

-> Add pagination
-> Deploy to cloud(AWS / Render)
-> Add Swagger documentation 
-> Build React Frontend
-> Add unit and integration tests 