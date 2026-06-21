# Changelog

## 2 - Database Setup
### Updates:
- Added docker compose config for local MySQL development
- Introduced environment variables
- Successfully started up mysql container and verified connectivity
- Configured Spring Boot datasource and connected application to MySQL
- Created `Photo` entity
- Created `PhotoStatus` enum
- Verified `photos` table creation
- Application starts successfully with db enabled

## 1 - Project Setup
### Updates:
- Created Spring Boot project
- Configured gradle
- Excluded db until needed
- Added HelloWorldController
- Application starts successfully
- Verified GET /hello endpoint