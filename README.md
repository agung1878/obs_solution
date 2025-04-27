## Inventory Management System (Spring Boot)
A simple Inventory Management System built with Spring Boot showcasing CRUD operations, stock management logic (Top Up and Withdrawal), proper validation, exception handling, and clean API design.

## Features
Manage Items, Inventories, and Orders.

Pagination for listing endpoints.

Manual ID assignment (no auto-generated IDs).

Validation on all incoming requests.

Global error handling (400, 404, 500).

Standardized API response structure with BaseResponseDto.

Clear use of @Valid, @ExceptionHandler

JUnit + Mockito unit tests.

## Tech Stack
Java 21

Spring Boot 3

Spring Data JPA

H2 In-Memory Database

Lombok

Maven

JUnit 5 & Mockito

## API Endpoints
All responses use the standard format:

`{
"responseCode": "00",
"responseMessage": "Success",
"data": { ... }
}`

### Item Endpoints

| Method | Endpoint          | Request Body                                | Success Response | Notes |
|:------:|:------------------|:--------------------------------------------|:-----------------|:-----:|
| GET    | `/api/items`       | -                                           | 200 OK + paginated data | Get all items |
| GET    | `/api/items/{id}`  | -                                           | 200 OK + item data | Get item by ID |
| POST   | `/api/items`       | `{ "id", 1, "name": "Pen", "price": 5 }`    | 201 Created | Create new item |
| POST   | `/api/items?id=1`  | `{ "id", 1, "name": "Pen X", "price": 10 }` | 200 OK | Update existing item |
| DELETE | `/api/items/delete?id=1` | -                                           | 204 No Content | Delete item by ID |

### Inventory Endpoints

| Method | Endpoint                       | Request Body                                       | Success Response | Notes |
|:------:|:-------------------------------|:---------------------------------------------------|:-----------------|:-----:|
| GET    | `/api/inventories`             | -                                                  | 200 OK + paginated data | Get all inventory records |
| GET    | `/api/inventories/{id}`        | -                                                  | 200 OK + inventory data | Get inventory by ID |
| POST   | `/api/inventories`             | `{ "id": 1, "itemId": 1, "qty": 10, "type": "T" }` | 201 Created | Create new inventory record (Top-Up or Withdrawal) |
| POST   | `/api/inventories?id=5`        | `{ "id": 1, "itemId": 1, "qty": 5, "type": "W" }`  | 200 OK | Update existing inventory record |
| DELETE | `/api/inventories/delete?id=1` | -                                                  | 204 No Content | Delete inventory record by ID |

### Order Endpoints

| Method | Endpoint                        | Request Body                                             | Success Response | Notes |
|:------:|:--------------------------------|:---------------------------------------------------------|:-----------------|:-----:|
| GET    | `/api/orders`                   | -                                                        | 200 OK + paginated data | Get all orders |
| GET    | `/api/orders/{orderId}`         | -                                                        | 200 OK + order data | Get order by ID |
| POST   | `/api/orders`                   | `{ "orderNo": "O1", "itemId": 1, "qty": 2, "price": 5 }` | 201 Created | Create new order |
| POST   | `/api/orders?orderId=O1         | `{ "orderNo": "O1", "itemId": 1, "qty": 4, "price": 5 }` | 200 OK | Update existing order |
| DELETE | `/api/orders/delete?orderNo=O1` | -                                                        | 204 No Content | Delete order by order number |

## Example Successful Response Pagination
`"responseCode": "00",
    "responseMessage": "success",
    "data": {
        "content": [
            {
                "id": 1,
                "name": "Pen",
                "price": 5,
                "stock": 0
            }
        ],
        "pageable": {
            "pageNumber": 0,
            "pageSize": 10,
            "sort": {
                "empty": true,
                "sorted": false,
                "unsorted": true
            },
            "offset": 0,
            "paged": true,
            "unpaged": false
        },
        "last": true,
        "totalElements": 1,
        "totalPages": 1,
        "size": 10,
        "number": 0,
        "sort": {
            "empty": true,
            "sorted": false,
            "unsorted": true
        },
        "first": true,
        "numberOfElements": 1,
        "empty": false
    }
}`

## Example Error Response
`{
    "responseCode": "404",
    "responseMessage": "Order not found with ID: O1"
}`
