# 4/20 Flight seat reservation app
✅ Use Case
Bullet proof flight seat reservation system. Each seat should be reserved at most once, even if failures, crashes, and retries happen.

# Blog post link


# Youtube Video Link


## Features
- Send emails via post request
- Temporal

## Technologies
- Java + Spring Boot
- Temporal
- [MailHog](https://github.com/mailhog/MailHog)

## Running locally:
```
# setup temporal server:
git clone https://github.com/temporalio/docker-compose.git
cd docker-compose
docker-compose up

# run this app:
git clone https://github.com/michael-oswald/20-temporal-apps.git
cd 20-apps/app-3-bulk-email-send
./mvnw spring-boot:run

# MailHog:
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# testing with curl:
curl --location 'localhost:8081/api/email/batch' \
--header 'Content-Type: application/json' \
--data-raw '{
  "uniqueEmailBatchId": "uniqueId1234" ,
  "emails": [
    {
      "to": "user1@example.com",
      "subject": "Subject 1",
      "body": "Hello user 1"
    }
  ]
}
```
