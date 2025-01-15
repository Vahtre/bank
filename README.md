# Bank Application

## Description
This is a Spring Boot-based bank application that provides various banking services such as account creation, balance inquiry, transaction history, money deposit, money debit, and currency exchange.

## Setup Instructions

### Prerequisites
- Java 17
- Docker
- Gradle

### Steps to Set Up and Run the Application

1. **Clone the repository**:
   ```sh
   git clone <repository-url>
   cd <repository-directory>
   ```
   

### Steps to Set Up and Run the Application

2. **Build the JAR file**:
   ```sh
   ./gradlew build
   ```

3. **Build the Docker image**:
   ```sh
   docker build -t bank-application .
   ```

4. **Run the Docker container**:
   ```sh
   docker run -p 8080:8080 bank-application
   ```

5. **Access the application**:
   Open your browser and navigate to `http://localhost:8080`.

## API Documentation

### Endpoints

1. **Get Account Balance**
    - **URL**: `/account/{accountId}/balance`
    - **Method**: `GET`
    - **Response**:
      ```json
      {
        "accountId": 1,
        "balances": {
          "EUR": 0.00,
          "USD": 0.00,
          "SEK": 0.00,
          "RUB": 0.00
        }
      }
      ```

2. **Get Transaction History**
    - **URL**: `/account/{accountId}/transactions`
    - **Method**: `GET`
    - **Response**:
      ```json
      [
        {
          "id": 1,
          "accountId":1,
          "currency": "USD",
          "amount": 100.00,
          "timestamp": "2023-10-01T10:00:00Z",
          "type": "DEPOSIT",
        }
      ]
      ```

3. **Create Account**
    - **URL**: `/account`
    - **Method**: `POST`
    - **Request**:
      ```json
      {
        "accountNumber": "123456"
      }
      ```
    - **Response**:
      ```json
      {
        "id":1,
        "accountNumber": "123456",
        "balances": {
          "EUR": 0.00,
          "USD": 0.00,
          "SEK": 0.00,
          "RUB": 0.00
        }
      }
      ```

4. **Add Money**
    - **URL**: `/account/{accountId}/deposit`
    - **Method**: `POST`
    - **Request**:
      ```json
      {
        "currency": "USD",
        "amount": 100.00
      }
      ```
    - **Response**:
      ```json
      {
        "id":1,
        "accountNumber": "123456",
        "balances": {
          "EUR": 0.00,
          "USD": 100.00,
          "SEK": 0.00,
          "RUB": 0.00
        }
      }
      ```

5. **Debit Money**
    - **URL**: `/account/{accountId}/debit`
    - **Method**: `POST`
    - **Request**:
      ```json
      {
        "currency": "USD",
        "amount": 50.0
      }
      ```
    - **Response**:
      ```json
      {
        "id":1,
        "accountNumber": "123456",
        "balances": {
          "EUR": 0.00,
          "USD": 50.00,
          "SEK": 0.00,
          "RUB": 0.00
        }
      }
      ```

6. **Exchange Currency**
    - **URL**: `/account/{accountId}/exchange`
    - **Method**: `POST`
    - **Request**:
      ```json
      {
        "fromCurrency": "USD",
        "toCurrency": "EUR",
        "amount": 50.0
      }
      ```
    - **Response**:
      ```json
      {
        "id":1,
        "accountNumber": "123456",
        "balances": {
          "EUR": 49.00,
          "USD": 0.00,
          "SEK": 0.00,
          "RUB": 0.00
        }
      }
      ```

### Note
- The account balance cannot be negative. Any operation that would result in a negative balance will be rejected.