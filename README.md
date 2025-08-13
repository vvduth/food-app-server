# FoodApp - Spring Boot Backend API

A comprehensive food ordering application backend built with Spring Boot, featuring user authentication, menu management, cart functionality, order processing, and integrated payment system.

## 🌐 Live Deployment

- **Backend API**: http://54.157.147.12:8090/api/v1
- **Frontend**: http://foodboot-client-react.s3-website-us-east-1.amazonaws.com

## 🏗️ Architecture Overview

This Spring Boot application follows a clean architecture pattern with modular design, deployed on AWS infrastructure:

- **Database**: PostgreSQL hosted on AWS RDS
- **Backend**: Spring Boot application running on AWS EC2 instance
- **File Storage**: AWS S3 for image storage
- **Payment**: Stripe payment integration
- **Email**: Email notification system

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.4.7
- **Language**: Java 17
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT authentication
- **Payment**: Stripe API
- **Cloud Services**: AWS (EC2, RDS, S3)
- **Email**: Spring Mail
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven
- **Additional Libraries**:
  - ModelMapper for object mapping
  - Lombok for boilerplate code reduction
  - AWS SDK for S3 integration
  - JWT for token-based authentication

## 📚 API Modules

### 🔐 Authentication & Authorization (`auth_users`)
- User registration and login
- JWT token-based authentication
- Role-based access control
- Password encryption

**Endpoints:**
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login

### 👤 User Management (`auth_users`)
- User profile management
- User information updates

### 🏷️ Category Management (`category`)
- Food category CRUD operations
- Category-based menu filtering

### 🍽️ Menu Management (`menu`)
- Menu item CRUD operations
- Image upload to S3
- Menu item search and filtering
- Price and description management

### 🛒 Shopping Cart (`cart`)
- Add/remove items from cart
- Update item quantities
- Cart persistence for logged-in users

### 📦 Order Management (`order`)
- Order creation and processing
- Order history tracking
- Order status updates
- Email notifications

### 💳 Payment Processing (`payment`)
- Stripe payment integration
- Payment intent creation
- Payment confirmation handling
- Payment status tracking

### ⭐ Review System (`review`)
- Customer reviews and ratings
- Review management
- Rating calculations

### 👥 Role Management (`role`)
- User role assignment
- Permission management

### 📧 Email Notifications (`email_notification`)
- Order confirmation emails
- Payment status notifications
- Asynchronous email processing

### ☁️ AWS Integration (`aws`)
- S3 file upload service
- Image storage and retrieval
- AWS configuration management

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/com/phegon/FoodApp/
│   │   ├── auth_users/          # Authentication & User management
│   │   ├── aws/                 # AWS S3 integration
│   │   ├── cart/                # Shopping cart functionality
│   │   ├── category/            # Food categories
│   │   ├── config/              # Application configuration
│   │   ├── email_notification/  # Email service
│   │   ├── enums/               # Application enumerations
│   │   ├── exceptions/          # Custom exception handling
│   │   ├── menu/                # Menu items management
│   │   ├── order/               # Order processing
│   │   ├── payment/             # Payment integration
│   │   ├── response/            # API response wrappers
│   │   ├── review/              # Review system
│   │   ├── role/                # Role management
│   │   └── security/            # Security configuration
│   └── resources/
│       ├── templates/           # Email templates
│       └── application.properties
└── test/                        # Unit and integration tests
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL database
- AWS account (for S3 and RDS)
- Stripe account for payment processing

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd FoodApp
   ```

2. **Configure application properties**
   Create `src/main/resources/application.properties` with:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:postgresql://localhost:5432/foodapp
   spring.datasource.username=your_db_username
   spring.datasource.password=your_db_password
   
   # JPA Configuration
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   
   # JWT Configuration
   jwt.secret=your_jwt_secret_key
   jwt.expiration=86400000
   
   # AWS Configuration
   aws.s3.bucket.name=your_s3_bucket_name
   aws.access.key=your_aws_access_key
   aws.secret.key=your_aws_secret_key
   aws.region=us-east-1
   
   # Stripe Configuration
   stripe.secret.key=your_stripe_secret_key
   
   # Email Configuration
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_app_password
   ```

3. **Build and run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the API**
   The application will be available at `http://localhost:8080/api/v1`

## 📋 API Documentation

### Base URL
- **Local**: `http://localhost:8090/api/v1`
- **Production**: `http://54.157.147.12:8090/api/v1`

### Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

### Key Endpoints

#### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - User login

#### Menu Management
- `GET /menus` - Get all menu items
- `POST /menus` - Create new menu item (Admin only)
- `PUT /menus/{id}` - Update menu item (Admin only)
- `DELETE /menus/{id}` - Delete menu item (Admin only)

#### Cart Operations
- `GET /cart` - Get user's cart
- `POST /cart/add` - Add item to cart
- `PUT /cart/update` - Update cart item quantity
- `DELETE /cart/remove/{itemId}` - Remove item from cart

#### Order Processing
- `POST /orders` - Create new order
- `GET /orders` - Get user's orders
- `GET /orders/{id}` - Get specific order details

#### Payment
- `POST /payment/create-intent` - Create Stripe payment intent
- `POST /payment/confirm` - Confirm payment

## 🔒 Security Features

- **JWT Authentication**: Stateless token-based authentication
- **Role-Based Access Control**: Different permissions for users and admins
- **Password Encryption**: BCrypt password hashing
- **CORS Configuration**: Cross-origin resource sharing setup
- **Input Validation**: Comprehensive request validation

## 🌟 Key Features

- **Scalable Architecture**: Modular design for easy maintenance and scaling
- **Cloud Integration**: Full AWS integration for production deployment
- **Payment Processing**: Secure Stripe payment integration
- **Email Notifications**: Automated email system for order updates
- **Image Management**: S3-based image storage for menu items
- **Comprehensive API**: RESTful API design with proper HTTP status codes
- **Exception Handling**: Centralized error handling and custom exceptions
- **Async Processing**: Asynchronous email processing for better performance

## 🚀 Deployment

The application is deployed on AWS with the following setup:

1. **EC2 Instance**: Backend application server
2. **RDS PostgreSQL**: Managed database service
3. **S3 Bucket**: Static file storage for images
4. **Frontend**: React application hosted on S3 with static website hosting

## 📧 Contact & Support

For issues or questions, please contact the development team or create an issue in the repository.

## 📄 License

This project is licensed under the terms specified in the license file.

---

**Note**: Make sure to configure all environment variables and AWS credentials properly before deploying to production.
