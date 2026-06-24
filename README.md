# HomeHunt

HomeHunt is a rental property management web application for tenants, landlords, and admins.

## Main Functionalities

### Guest

- View approved rental properties
- Search properties by location
- Filter properties by price range
- Filter properties by property type
- Sort properties by newest or price
- View basic property details
- Login/register before showing interest or applying

### Tenant

- Register and login
- View approved properties
- View full property details
- Show interest in a property
- Remove interested properties
- View interested properties
- Apply for a rental property
- View submitted applications
- Update profile details
- Change password

### Landlord

- Register and login
- Add new rental properties
- Upload property images
- Edit own properties
- Delete own properties
- View property approval status
- View applications received for own properties
- Filter applications by property
- Accept or reject tenant applications
- Update profile details
- Change password

### Admin

- Login as admin
- View platform summary
- View all users
- Change user roles
- Delete users
- View submitted properties
- View property details before approval
- Approve property listings
- Reject property listings
- Delete inappropriate properties
- Update profile details
- Change password

## Security Functionalities

- JWT-based authentication
- BCrypt password hashing
- Role-based access control
- Protected tenant, landlord, and admin routes
- Users can access only their allowed data
- Landlords can manage only their own properties
- Tenants can apply only once per property

## Backend

- Java version: Java 21
- Java concepts used: Java 8+ standard concepts
- Framework: Spring Boot
- Security: Spring Security + JWT
- Database: PostgreSQL
- ORM: Spring Data JPA / Hibernate
- Build tool: Maven

## Frontend

- Framework: Angular
- Component-based structure
- Separate `.ts`, `.html`, `.scss`, and `.spec.ts` files
- Route guards
- JWT interceptor
- Form validation
- API error handling

## Database Tables

- users
- properties
- applications
- favorites

## Included Setup File

The project includes:

```text
database/schema.sql