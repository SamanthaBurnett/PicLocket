# PicLocket

PicLocket is a photo backup system built to explore:
- Direct S3 uploads using presigned URLs
- Metadata persistence
- Upload state management
- Event-driven processing with SQS

## Current Status
🚧 In Development

## Architecture
```mermaid

flowchart LR

    Client[Client / Postman] --> API[Spring Boot API]



    API --> MySQL[(MySQL<br/>Photo metadata)]

    API --> S3[AWS S3<br/>Presigned URL generation]



    Client --> S3Upload[AWS S3<br/>Direct upload/download]



    S3Upload --> S3

```

## Upload Flow

```mermaid

sequenceDiagram

    participant Client

    participant API as Spring Boot API

    participant DB as MySQL

    participant S3 as AWS S3



    Client->>API: POST /v1/photos/upload-request

    API->>DB: Create PENDING_UPLOAD metadata row

    API->>S3: Generate presigned PUT URL

    API-->>Client: Return photoId + uploadUrl



    Client->>S3: PUT image using uploadUrl

    Client->>API: POST /v1/photos/{photoId}/complete

    API->>S3: HeadObject using stored s3Key

    API->>DB: Mark photo as UPLOADED

    API-->>Client: 204 No Content

```

## Download Flow

```mermaid

sequenceDiagram

    participant Client

    participant API as Spring Boot API

    participant DB as MySQL

    participant S3 as AWS S3



    Client->>API: GET /v1/photos

    API->>DB: Find UPLOADED photos

    API->>S3: Generate presigned GET URLs

    API-->>Client: Return photo metadata + download URLs

    Client->>S3: Download image using downloadUrl

```

## MVP Goals
- Upload photos directly to S3
- Store metadata in a relational database
- Retrieve uploaded photos
- Generate presigned download URLS

## Future Roadmap
- SQS event processing
- Background worker
- Upload quotas
- Automatic photo expiration
- Batch Uploads
- Duplicate deletion

## End-to-End Testing
A postman collection is included under

```text
postman/PicLocket.postman_collection.json
```

Workflow:

1. Create Upload Request
2. Upload File to S3
3. Complete Upload
4. Retrieve Uploaded Photos
