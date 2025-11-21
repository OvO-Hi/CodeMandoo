export interface TicketCreateRequest {
  title: string;
  venue?: string;
  genre?: string;
  performedAt: string; // ISO 8601 문자열
  imageUrl?: string;
  reviewText?: string;
  isPublic?: boolean;
  seat?: string;
  bookingSite?: string;
}

export interface TicketUpdateRequest {
  title?: string;
  venue?: string;
  genre?: string;
  performedAt?: string;
  imageUrl?: string | null;
  reviewText?: string | null;
  isPublic?: boolean;
}

export interface TicketResponse {
  id: number;
  title: string;
  venue?: string | null;
  genre?: string | null;
  viewDate: string;
  imageUrl?: string | null;
  reviewText?: string | null;
  isPublic: boolean;
  createdAt: string;
}

