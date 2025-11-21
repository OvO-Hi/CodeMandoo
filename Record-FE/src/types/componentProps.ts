import { NativeStackNavigationProp, NativeStackScreenProps } from '@react-navigation/native-stack';
import { RouteProp } from '@react-navigation/native';
import { TicketData, ReviewData, RootStackParamList } from './reviewTypes';
import { Ticket } from './ticket';


// 티켓 생성 플로우 props
// 후기작성 방법 props
export type ReviewOptionsScreenProps = NativeStackScreenProps<RootStackParamList, 'ReviewOptions'>;
// 후기 추가 props
export type AddReviewScreenProps = NativeStackScreenProps<RootStackParamList, 'AddReview'>;
// 이미지 추가 방법 props
export type ImageOptionsScreenProps = NativeStackScreenProps<RootStackParamList, 'ImageOptions'>;
// AI 이미지 결과 props
export type AIImageResultsScreenProps = NativeStackScreenProps<RootStackParamList, 'AIImageResults'>;
// 티켓 완성 props
export type TicketCompleteScreenProps = NativeStackScreenProps<RootStackParamList, 'TicketComplete'>;



// 캘린더 props
// 캘린더 헤더 props
export interface CalendarHeaderProps {
  currentDate: Date;
  onPrevMonth: () => void;
  onNextMonth: () => void;
  onDateSelect?: (date: Date) => void;
}
// CustomCalenderProps
export interface CustomCalendarProps {
  selectedDate?: Date;
  onDateSelect: (date: Date) => void;
  tickets?: Ticket[];
  markedDates?: { [key: string]: any };
}

// 리스트 티켓 컴포넌트 props (아마도)
export interface EventCardProps {
  ticket: Ticket;
  onPress: (ticket: Ticket) => void;
  style?: any;
  showDate?: boolean;
  isMine?: boolean;
}

// 티켓 모달 props 
export interface TicketDetailModalProps {
  visible: boolean;
  ticket: Ticket | null;
  onClose: () => void;
  onDelete?: (ticketId: string) => void;
  onShare?: (ticket: Ticket) => void;
  isMine?: boolean; // Controls whether delete button is shown
  onTicketChanged?: () => void; // 마이페이지 등 부모가 서버 데이터를 다시 불러와야 할 때 사용
}

// 친구 관련 컴포넌트 props 
export interface FriendItemProps {
  friend: {
    id: string;
    name: string;
    profileImage?: string;
    mutualFriends?: number;
  };
  onPress?: () => void;
  showMutualFriends?: boolean;
}
// 친구 요청 props
export interface FriendRequestItemProps {
  request: {
    id: string;
    name: string;
    profileImage?: string;
    requestDate: Date;
  };
  onAccept: (requestId: string) => void;
  onReject: (requestId: string) => void;
}

// 설정 컴포넌트 props
// 설정 아이템이 뭐드라 ;; 
export interface SettingsItemProps {
  title: string;
  icon?: string;
  onPress: () => void;
  showArrow?: boolean;
  textColor?: string;
}
// 개인정보 props
export interface PersonalInfoFieldProps {
  label: string;
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
  secureTextEntry?: boolean;
  editable?: boolean;
}

// 음성녹음 props
export interface VoiceRecorderProps {
  isRecording: boolean;
  onStartRecording: () => void;
  onStopRecording: () => void;
  recordingText?: string;
}

// 장르 탭 컴포넌트 props
export interface FilterButtonProps {
  title: string;
  isSelected: boolean;
  onPress: () => void;
}

// 히스토리 탭 컴포넌트 props
export interface HistoryFilterProps {
  selectedFilter: 'all' | 'week' | 'month' | 'year';
  onFilterChange: (filter: 'all' | 'week' | 'month' | 'year') => void;
}

// Form 컴포넌트 props
export interface FormInputProps {
  label: string;
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
  multiline?: boolean;
  numberOfLines?: number;
  maxLength?: number;
  keyboardType?: 'default' | 'email-address' | 'numeric' | 'phone-pad';
  secureTextEntry?: boolean;
  error?: string;
}

// 폼 버튼 props (버튼 누르면 props로 전송)
export interface FormButtonProps {
  title: string;
  onPress: () => void;
  disabled?: boolean;
  loading?: boolean;
  variant?: 'primary' | 'secondary' | 'danger';
  style?: any;
}

// 이미지 컴포넌트
// 이미지 선택 props
export interface ImagePickerProps {
  onImageSelected: (imageUri: string) => void;
  onCancel?: () => void;
  allowMultiple?: boolean;
  maxImages?: number;
}
// 이미지 미리보기 props
export interface ImagePreviewProps {
  imageUri: string;
  onRemove?: () => void;
  onEdit?: () => void;
  style?: any;
}

// 검색 컴포넌트 props 
export interface SearchBarProps {
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
  onSubmit?: () => void;
  showVoiceButton?: boolean;
  onVoicePress?: () => void;
}

// 로딩 컴포넌트 props
// 로딩 빙글빙글 도는거 props
export interface LoadingSpinnerProps {
  size?: 'small' | 'large';
  color?: string;
  text?: string;
}
// Empty State Props... 이게 뭐지 
export interface EmptyStateProps {
  title: string;
  message?: string;
  icon?: string;
  actionText?: string;
  onActionPress?: () => void;
}

// Modal Components
// CONFIRM props
export interface ConfirmModalProps {
  visible: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel: () => void;
  variant?: 'default' | 'danger';
}

// 이게 뭐더라 
export interface ActionSheetProps {
  visible: boolean;
  options: Array<{
    title: string;
    onPress: () => void;
    destructive?: boolean;
  }>;
  onCancel: () => void;
  title?: string;
}
