import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BottomTabScreenProps } from '@react-navigation/bottom-tabs';
import { CompositeScreenProps } from '@react-navigation/native';
import { Friend } from './friend';
import { TicketData, ReviewData } from './reviewTypes';

// Root Stack Navigator Types
export type RootStackParamList = {
  Login: undefined;
  Signup: undefined;
  MainTabs: undefined;
  AddTicket: undefined;
  AddTicketMusical: undefined;
  ReviewOptions: { ticketData: TicketData };
  AddReview: { ticketData: TicketData; inputMode?: 'text' | 'voice' };
  ImageOptions: { ticketData: TicketData; reviewData: ReviewData };
  AIImageSettings: { ticketData: TicketData; reviewData: ReviewData };
  AIImageResults: { ticketData: TicketData; reviewData: ReviewData };
  TicketComplete: { ticketData: TicketData; reviewData: ReviewData };
  Settings: undefined;
  PersonalInfoEdit: undefined;
  History: undefined;
  FriendsList: undefined;
  AddFriend: undefined;
  SentRequests: undefined;
  FriendProfile: { friend: Friend };
};

// Tab Navigator Types
export type TabParamList = {
  Home: undefined;
  AddTicket: undefined;
  Calendar: undefined;
  MyPage: undefined;
};

// Screen Props Types
export type RootStackScreenProps<T extends keyof RootStackParamList> = 
  NativeStackScreenProps<RootStackParamList, T>;

export type TabScreenProps<T extends keyof TabParamList> = 
  CompositeScreenProps<
    BottomTabScreenProps<TabParamList, T>,
    NativeStackScreenProps<RootStackParamList>
  >;

// Specific Screen Props
export type FriendProfileScreenProps = RootStackScreenProps<'FriendProfile'>;
export type AddReviewScreenProps = RootStackScreenProps<'AddReview'>;
