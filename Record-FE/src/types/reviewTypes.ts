import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RouteProp } from '@react-navigation/native';
import { BaseTicket } from './ticket';

// Define the shape of ticket data

// 티켓 데이터
export interface TicketData extends BaseTicket {}

// 후기 데이터
export interface ReviewData {
  text?: string;
  reviewText?: string; // Alias for text to match different usages
  voiceNote?: string;
  isPublic?: boolean;
  image?: string;
}

// RootStack이 뭐지
export type RootStackParamList = {
  // Add Ticket Flow
  ReviewOptions: { ticketData: TicketData };
  AddReview: { 
    ticketData: TicketData;
    inputMode: 'text' | 'voice';
    initialText?: string;
  };
  ImageOptions: { 
    ticketData: TicketData; 
    reviewData: ReviewData;
  };
    AIImageResults: {
    ticketData: TicketData;
    reviewData: ReviewData;
    images: string[];
    settings: {
      backgroundColor: string;
      includeText: boolean;
      imageStyle: string;
      aspectRatio: string;
    };
  }
  TicketComplete: {
    ticketData: TicketData;
    reviewData: ReviewData;
    images?: string[];
  };
  // Other screens can be added here
};

// Navigation prop types ?
export type ReviewOptionsScreenNavigationProp = NativeStackNavigationProp<
  RootStackParamList,
  'ReviewOptions'
>;

export type AddReviewScreenNavigationProp = NativeStackNavigationProp<
  RootStackParamList,
  'AddReview'
>;

export type ImageOptionsScreenNavigationProp = NativeStackNavigationProp<
  RootStackParamList,
  'ImageOptions'
>;

// Route prop types
export type ReviewOptionsRouteProp = RouteProp<RootStackParamList, 'ReviewOptions'>;
export type AddReviewRouteProp = RouteProp<RootStackParamList, 'AddReview'>;
export type ImageOptionsRouteProp = RouteProp<RootStackParamList, 'ImageOptions'>;

// Component props
export interface ReviewOptionsProps {
  navigation: ReviewOptionsScreenNavigationProp;
  route: ReviewOptionsRouteProp;
  onSelectOption: (option: 'text' | 'voice' | 'ai') => void;
}

export interface AddReviewPageProps {
  navigation: AddReviewScreenNavigationProp;
  route: AddReviewRouteProp;
}

export interface ImageOptionsProps {
  navigation: ImageOptionsScreenNavigationProp;
  route: ImageOptionsRouteProp;
}
