export type Role = 'TENANT' | 'LANDLORD' | 'ADMIN';
export type PropertyType = 'APARTMENT' | 'HOUSE';
export type PropertyStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface AuthResponse {
  token: string;
  id: number;
  name: string;
  email: string;
  role: Role;
}

export interface Property {
  id: number;
  title: string;
  description: string;
  location: string;
  price: number;
  type: PropertyType;
  imageUrl?: string;
  status: PropertyStatus;
  ownerId: number;
  landlordName: string;
  landlordEmail: string;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface RentalApplication {
  id: number;
  userId: number;
  tenantName: string;
  propertyId: number;
  propertyTitle: string;
  status: ApplicationStatus;
  income: number;
  notes?: string;
  createdAt: string;
}

export interface UserSummary {
  id: number;
  name: string;
  email: string;
  role: Role;
}

export interface DashboardStats {
  totalUsers: number;
  totalProperties: number;
  pendingApprovals: number;
}
