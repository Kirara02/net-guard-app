# NetGuard API Documentation

## 📡 Complete API Reference

This document provides detailed information about all NetGuard Backend API endpoints, including request/response formats, authentication requirements, and usage examples.

## 🔐 Authentication Endpoints

### **POST /api/auth/register**

Register new user account

**Request Body:**

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "division": "IT",
  "phone": "08123456789",
  "role": "USER"
}
```

**Response (201):**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "uuid",
      "name": "John Doe",
      "email": "john@example.com",
      "division": "IT",
      "phone": "08123456789",
      "role": "USER",
      "is_active": true,
      "created_at": "2024-01-01T00:00:00Z"
    }
  }
}
```

### **POST /api/auth/login**

User authentication

**Request Body:**

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "uuid",
      "name": "John Doe",
      "email": "john@example.com",
      "role": "USER"
    }
  }
}
```

### **GET /api/auth/me**

Get current user profile

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Response (200):**

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "John Doe",
    "email": "john@example.com",
    "division": "IT",
    "phone": "08123456789",
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

### **PUT /api/auth/profile**

Update current user profile

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Request Body:**

```json
{
  "name": "John Doe Updated",
  "division": "Senior IT",
  "phone": "081234567890"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": "uuid",
    "name": "John Doe Updated",
    "email": "john@example.com",
    "division": "Senior IT",
    "phone": "081234567890",
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

### **PUT /api/auth/change-password**

Change current user password

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Request Body:**

```json
{
  "current_password": "oldpassword123",
  "new_password": "newpassword123"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null
}
```

## 🌐 Server Management Endpoints

### **POST /api/servers**

Create new server

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Request Body:**

```json
{
  "name": "API Server",
  "url": "https://api.company.com"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Server created successfully",
  "data": {
    "id": "uuid",
    "name": "API Server",
    "url": "https://api.company.com",
    "created_by": "user-uuid",
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

### **GET /api/servers**

Get all servers from all users

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Response (200):**

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "API Server",
      "url": "https://api.company.com",
      "created_by": "user-uuid",
      "created_at": "2024-01-01T00:00:00Z"
    },
    {
      "id": "uuid-2",
      "name": "Database Server",
      "url": "https://db.company.com",
      "created_by": "user-uuid-2",
      "created_at": "2024-01-02T00:00:00Z"
    }
  ]
}
```

### **GET /api/servers/:id**

Get specific server by ID

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Response (200):**

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "API Server",
    "url": "https://api.company.com",
    "created_by": "user-uuid",
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

### **PUT /api/servers/:id**

Update server information

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Request Body:**

```json
{
  "name": "Updated API Server",
  "url": "https://api-v2.company.com"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Server updated successfully",
  "data": {
    "id": "uuid",
    "name": "Updated API Server",
    "url": "https://api-v2.company.com",
    "created_by": "user-uuid",
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

### **DELETE /api/servers/:id**

Delete server

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Response (200):**

```json
{
  "success": true,
  "message": "Server deleted successfully",
  "data": null
}
```

### **PATCH /api/servers/:id/status**

Update server status (Mobile App Endpoint)

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Request Body:**

```json
{
  "status": "DOWN",
  "response_time": 5000
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Server status updated successfully",
  "data": {
    "id": "uuid",
    "name": "API Server",
    "url": "https://api.company.com",
    "created_by": "user-uuid",
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

## 🚨 Incident Management (History) Endpoints

### **POST /api/history** *(REMOVED - Auto-created by server status updates)*

**Note:** History records are now automatically created when server status is updated to "DOWN" via `PATCH /api/servers/:id/status`. Manual history creation is no longer supported.

### **GET /api/history**

Get all incident history from all users

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Query Parameters:**

- `server_id` (optional): Filter by server
- `limit` (optional): Limit results (default: 50, max: 1000)

**Response (200):**

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "server_id": "server-uuid",
      "server_name": "API Server",
      "url": "https://api.company.com",
      "status": "DOWN",
      "timestamp": "2024-01-01T00:00:00Z",
      "created_by": "John Doe",
      "resolved_by": null,
      "resolved_at": null,
      "resolve_note": null,
      "description": null
    },
    {
      "id": "uuid-2",
      "server_id": "server-uuid-2",
      "server_name": "Database Server",
      "url": "https://db.company.com",
      "status": "RESOLVED",
      "timestamp": "2024-01-02T00:00:00Z",
      "created_by": "Bob Wilson",
      "resolved_by": "Alice Johnson",
      "resolved_at": "2024-01-02T01:30:00Z",
      "resolve_note": "Database connection restored",
      "description": null
    }
  ]
}
```

### **PATCH /api/history/:id/resolve**

Resolve incident

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Request Body:**

```json
{
  "resolve_note": "Server restarted, issue resolved"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "History record resolved successfully",
  "data": {
    "id": "uuid",
    "server_id": "server-uuid",
    "server_name": "API Server",
    "url": "https://api.company.com",
    "status": "RESOLVED",
    "timestamp": "2024-01-01T00:00:00Z",
    "created_by": "user-name",
    "resolved_by": "user-name",
    "resolved_at": "2024-01-01T01:00:00Z",
    "resolve_note": "Server restarted, issue resolved"
  }
}
```

### **GET /api/report**

Get history records with advanced filtering

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Query Parameters:**

- `status` (optional): Filter by status (DOWN, RESOLVED)
- `server_name` (optional): Filter by server name (case-insensitive partial match)
- `start_date` (optional): Filter by start date (YYYY-MM-DD format)
- `end_date` (optional): Filter by end date (YYYY-MM-DD format)
- `limit` (optional): Limit results (default: 50, max: 1000)

**Response (200):**

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "server_id": "server-uuid",
      "server_name": "API Server",
      "url": "https://api.company.com",
      "status": "DOWN",
      "timestamp": "2024-01-01T00:00:00Z",
      "created_by": "John Doe",
      "resolved_by": null,
      "resolved_at": null,
      "resolve_note": null,
      "description": null
    },
    {
      "id": "uuid-2",
      "server_id": "server-uuid-2",
      "server_name": "Database Server",
      "url": "https://db.company.com",
      "status": "RESOLVED",
      "timestamp": "2024-01-02T00:00:00Z",
      "created_by": "Bob Wilson",
      "resolved_by": "Alice Johnson",
      "resolved_at": "2024-01-02T01:30:00Z",
      "resolve_note": "Database connection restored",
      "description": null
    }
  ]
}
```

### **GET /api/report/export**

Export history records to Excel or PDF format

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Query Parameters:**

- `status` (optional): Filter by status (DOWN, RESOLVED)
- `server_name` (optional): Filter by server name (case-insensitive partial match)
- `start_date` (optional): Filter by start date (YYYY-MM-DD format)
- `end_date` (optional): Filter by end date (YYYY-MM-DD format)
- `format` (optional): Export format - 'excel' or 'pdf' (default: excel)

**Response:**
- Excel: Downloads .xlsx file with report data in spreadsheet format
- PDF: Downloads .pdf file with report data in table format (landscape A4)

## 📊 Error Response Format

**All error responses follow this format:**

```json
{
  "success": false,
  "error": "Error message description"
}
```

**Common HTTP Status Codes:**

- `200` - Success
- `201` - Created
- `400` - Bad Request (validation error)
- `401` - Unauthorized (invalid/missing token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `409` - Conflict (duplicate data)
- `500` - Internal Server Error

## 🔑 Authentication

All protected endpoints require JWT token in Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 📱 Mobile App Integration

### Status Update Flow:

1. Mobile app detects server DOWN
2. Call `PATCH /api/servers/:id/status` with status "DOWN"
3. Backend automatically creates history record
4. FCM notification sent to all users
5. Users can resolve incidents via mobile app

### Profile Management Flow:

1. User opens profile settings in mobile app
2. Call `GET /api/auth/me` to get current profile
3. User updates profile information
4. Call `PUT /api/auth/profile` to save changes
5. Mobile app receives updated profile data

### FCM Notification Payload:

```json
{
  "notification": {
    "title": "Server DOWN: API Server",
    "body": "https://api.company.com"
  },
  "data": {
    "server_id": "uuid",
    "status": "DOWN"
  }
}
```

## 🧪 Testing Examples

### Register User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### Create Server

```bash
curl -X POST http://localhost:8080/api/servers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "API Server",
    "url": "https://api.company.com"
  }'
```

### Update Profile

```bash
curl -X PUT http://localhost:8080/api/auth/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Updated Name",
    "division": "Senior Developer",
    "phone": "081234567890"
  }'
```

### Change Password

```bash
curl -X PUT http://localhost:8080/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "current_password": "oldpassword123",
    "new_password": "newpassword123"
  }'
```

### Update Server Status (Mobile App)

```bash
curl -X PATCH http://localhost:8080/api/servers/SERVER_ID/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "status": "DOWN",
    "response_time": 5000
  }'
```

### Resolve Incident

```bash
curl -X PATCH http://localhost:8080/api/history/HISTORY_ID/resolve \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "resolve_note": "Server restarted successfully"
  }'
```

### Get Report with Filters

```bash
# Get all reports
curl -X GET "http://localhost:8080/api/report" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Filter by status
curl -X GET "http://localhost:8080/api/report?status=DOWN" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Filter by server name
curl -X GET "http://localhost:8080/api/report?server_name=API" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Filter by date range
curl -X GET "http://localhost:8080/api/report?start_date=2024-01-01&end_date=2024-01-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Combined filters with limit
curl -X GET "http://localhost:8080/api/report?status=RESOLVED&server_name=server&start_date=2024-01-01&end_date=2024-01-31&limit=100" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Export Report to Excel/PDF

```bash
# Export all reports to Excel
curl -X GET "http://localhost:8080/api/report/export" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o report.xlsx

# Export filtered reports to Excel
curl -X GET "http://localhost:8080/api/report/export?status=DOWN&start_date=2024-01-01&end_date=2024-01-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o filtered_report.xlsx

# Export to PDF
curl -X GET "http://localhost:8080/api/report/export?format=pdf" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o report.pdf

# Export filtered reports to PDF
curl -X GET "http://localhost:8080/api/report/export?format=pdf&status=RESOLVED&server_name=API" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o filtered_report.pdf
```

---

**Last Updated:** October 28, 2025
**Version:** 1.2.0
**New Features:** Advanced Report Filtering, Date Range Filters, Server Name Search
