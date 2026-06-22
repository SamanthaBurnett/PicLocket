# Changelog

## 6 - Implement Photo Retrieval
### Updates:
- Update repository to return photos
- Added `PhotoResponse`
- Updated service for photo retrievals
- Added `GET /v1/photos` endpoint
- Added unit test coverage

## 5 - Implement Upload Verification
### Updates:
- Add S3 object verification
- Implement upload completion workflow
- Added temp completion endpoint
- Added unit test
- Verified with Postman

## 4 - S3 Presigned Upload Integration
### Updates:
- Added AWS SDK and S3 config properties
- Created `S3PresignedUrlService`
- Replaced mock with real presigned URL generation
- Updated unit test
- Verified flow with Postman

## 3 - Mock Photo Metadata Creation Endpoint
### Updates:
- added dtos
- added `PhotoRepository`
- added `PhotoService`
- added `PhotoController` -> `POST /v1/photos/upload-request`
- Verified full flow using postman
- Added unit tests

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