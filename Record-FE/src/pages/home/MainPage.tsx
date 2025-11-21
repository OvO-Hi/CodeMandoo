import React, { useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Image,
  Dimensions,
  Animated,
  PanResponder,
  TouchableWithoutFeedback,
} from 'react-native';
import {
  SafeAreaView,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';
import { useAtom } from 'jotai';
import { ticketsAtom, TicketStatus } from '../../atoms';
import { Ticket } from '../../types/ticket';
import TicketDetailModal from '../../components/TicketDetailModal';
import GNB from '../../components/GNB';
import { isPlaceholderTicket } from '../../utils/isPlaceholder';
import {
  Colors,
  Typography,
  Spacing,
  BorderRadius,
  Shadows,
} from '../../styles/designSystem';

const { width } = Dimensions.get('window');

interface MainPageProps {
  navigation: any;
}

const MainPage: React.FC<MainPageProps> = ({ navigation }) => {
  const insets = useSafeAreaInsets();
  const [tickets] = useAtom(ticketsAtom);
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedFilter, setSelectedFilter] = useState<
    '전체' | '밴드' | '연극/뮤지컬'
  >('전체');
  const [showFilterDropdown, setShowFilterDropdown] = useState(false);
  const [currentTicketIndex, setCurrentTicketIndex] = useState(0);

  const pan = useRef(new Animated.ValueXY()).current;
  const opacity = useRef(new Animated.Value(1)).current;

  // 🔹 인디케이터용 애니메이션 값
  const scrollX = useRef(new Animated.Value(0)).current;

  const currentTicketIndexRef = useRef(0);
  useEffect(() => {
    currentTicketIndexRef.current = currentTicketIndex;
  }, [currentTicketIndex]);

  const resetCardPosition = () => {
    Animated.parallel([
      Animated.spring(pan, {
        toValue: { x: 0, y: 0 },
        useNativeDriver: false,
        tension: 100,
        friction: 8,
      }),
      Animated.spring(opacity, { toValue: 1, useNativeDriver: false }),
    ]).start();
  };

  const createBounceEffect = (direction: 'left' | 'right') => {
    const bounceDistance = direction === 'left' ? -30 : 30;
    Animated.sequence([
      Animated.timing(pan, {
        toValue: { x: bounceDistance, y: 0 },
        duration: 150,
        useNativeDriver: false,
      }),
      Animated.spring(pan, {
        toValue: { x: 0, y: 0 },
        tension: 300,
        friction: 8,
        useNativeDriver: false,
      }),
    ]).start();
  };

  const handleTicketPress = (ticket: Ticket) => {
    if (!ticket.id || !ticket.performedAt) return;
    setSelectedTicket(ticket);
    setModalVisible(true);
  };

  const handleCloseModal = () => {
    setModalVisible(false);
    setSelectedTicket(null);
  };

  const getCurrentMonth = () => {
    const now = new Date();
    return `${now.getMonth() + 1}월`;
  };

  const formatDate = (date?: Date) => {
    if (!date) return '';
    return `${date.getMonth() + 1}월 ${date.getDate()}일`;
  };

  const handleFilterSelect = (filter: '전체' | '밴드' | '연극/뮤지컬') => {
    setSelectedFilter(filter);
    setShowFilterDropdown(false);
  };

  const getCurrentMonthNumber = () => new Date().getMonth();
  const getCurrentYear = () => new Date().getFullYear();

  const realTickets = tickets.filter(ticket => !isPlaceholderTicket(ticket));

  const currentMonthTickets = realTickets
    .filter(ticket => {
      const ticketDate = new Date(ticket.performedAt);
      const isCurrentMonth =
        ticketDate.getMonth() === getCurrentMonthNumber() &&
        ticketDate.getFullYear() === getCurrentYear();
      const matchesGenre =
        selectedFilter === '전체' ||
        ticket.genre === selectedFilter ||
        !ticket.genre;
      return isCurrentMonth && matchesGenre;
    })
    .sort(
      (a, b) =>
        new Date(a.performedAt).getTime() - new Date(b.performedAt).getTime(),
    );

  const displayTickets: Ticket[] =
    currentMonthTickets.length > 0
      ? currentMonthTickets
      : [
          {
            id: '',
            title: '',
            artist: '',
            place: '',
            performedAt: undefined as any,
            bookingSite: '',
            genre: '',
            status: TicketStatus.PUBLIC,
            userId: 'placeholder',
            images: [],
            review: undefined,
            createdAt: new Date(),
            isPlaceholder: true,
          },
        ];

  useEffect(() => {
    if (currentTicketIndex >= displayTickets.length) setCurrentTicketIndex(0);
  }, [displayTickets.length, currentTicketIndex]);

  useEffect(() => {
    setCurrentTicketIndex(0);
    resetCardPosition();
  }, [selectedFilter]);

  const currentTicket = displayTickets[currentTicketIndex] || displayTickets[0];
  const isPlaceholder = isPlaceholderTicket(currentTicket);

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: (_, gestureState) =>
        Math.abs(gestureState.dx) > 10,
      onPanResponderMove: (_, gestureState) =>
        pan.setValue({ x: gestureState.dx, y: 0 }),
      onPanResponderRelease: (_, gestureState) => {
        const swipeThreshold = 80;
        const velocityThreshold = 0.3;
        const totalCards = displayTickets.length;
        const currentIndex = currentTicketIndexRef.current;

        const shouldSwipeRight =
          gestureState.dx > swipeThreshold ||
          (gestureState.dx > 30 && gestureState.vx > velocityThreshold);
        const shouldSwipeLeft =
          gestureState.dx < -swipeThreshold ||
          (gestureState.dx < -30 && gestureState.vx < -velocityThreshold);

        if (shouldSwipeRight) {
          if (currentIndex === 0) createBounceEffect('left');
          else {
            const newIndex = currentIndex - 1;
            setCurrentTicketIndex(newIndex);
            Animated.timing(scrollX, {
              toValue: newIndex * width,
              duration: 200,
              useNativeDriver: false,
            }).start();
            resetCardPosition();
          }
        } else if (shouldSwipeLeft) {
          if (currentIndex === totalCards - 1) createBounceEffect('right');
          else {
            const newIndex = currentIndex + 1;
            setCurrentTicketIndex(newIndex);
            Animated.timing(scrollX, {
              toValue: newIndex * width,
              duration: 200,
              useNativeDriver: false,
            }).start();
            resetCardPosition();
          }
        } else resetCardPosition();
      },
    }),
  ).current;

  return (
    <View style={styles.container}>
      <SafeAreaView style={styles.safeArea} edges={['top']}>
        {/* 헤더 */}
        <GNB
          rightContent={
            <View style={styles.headerRight}>
              <TouchableOpacity
                style={styles.filterButton}
                onPress={e => {
                  e.stopPropagation();
                  setShowFilterDropdown(!showFilterDropdown);
                }}
              >
                <Text style={styles.filterButtonText}>{selectedFilter}</Text>
                <Text style={styles.filterArrow}>▼</Text>
              </TouchableOpacity>
              {showFilterDropdown && (
                <View style={styles.filterDropdown}>
                  {['전체', '밴드', '연극/뮤지컬'].map(option => (
                    <TouchableOpacity
                      key={option}
                      style={[
                        styles.filterOption,
                        selectedFilter === option && {
                          backgroundColor: Colors.secondarySystemBackground,
                        },
                      ]}
                      onPress={() => handleFilterSelect(option as any)}
                    >
                      <Text
                        style={[
                          styles.filterOptionText,
                          selectedFilter === option && {
                            color: Colors.primary,
                            fontWeight: '600',
                          },
                        ]}
                      >
                        {option}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </View>
              )}
            </View>
          }
        />

        {/* 서브 헤더 */}
        <View style={styles.subHeader}>
          <Text style={styles.monthTitle}>
            {getCurrentMonth()}에 관람한 공연
          </Text>
          <Text style={styles.monthSubtitle}>
            한 달의 기록, 옆으로 넘기며 다시 만나보세요 ( ♪˶´・‎ᴗ・`˶♪ )
          </Text>
        </View>

        {/* 콘텐츠 */}
        <TouchableWithoutFeedback onPress={() => setShowFilterDropdown(false)}>
          <View style={styles.contentContainer}>
            <View style={styles.cardContainer}>
              {/* Animated 점 인디케이터 */}
              <View style={styles.dots}>
                {displayTickets.map((_, i) => {
                  const inputRange = [
                    (i - 1) * width,
                    i * width,
                    (i + 1) * width,
                  ];
                  const dotWidth = scrollX.interpolate({
                    inputRange,
                    outputRange: [6, 12, 6],
                    extrapolate: 'clamp',
                  });
                  const dotColor = scrollX.interpolate({
                    inputRange,
                    outputRange: ['#BDC3C7', '#2C3E50', '#BDC3C7'],
                    extrapolate: 'clamp',
                  });
                  return (
                    <Animated.View
                      key={i}
                      style={[
                        styles.dot,
                        { width: dotWidth, backgroundColor: dotColor },
                      ]}
                    />
                  );
                })}
              </View>

              {/* Animated 티켓 */}
              <Animated.View
                style={[
                  styles.animatedCard,
                  { transform: pan.getTranslateTransform(), opacity },
                ]}
                {...panResponder.panHandlers}
              >
                <TouchableOpacity
                  disabled={isPlaceholder}
                  style={[
                    styles.mainTicketCard,
                    isPlaceholder && styles.disabledCard,
                    !isPlaceholder &&
                      (!currentTicket.images ||
                        currentTicket.images.length === 0) &&
                      styles.mainTicketCardNoImage,
                  ]}
                  onPress={() => handleTicketPress(currentTicket)}
                  activeOpacity={isPlaceholder ? 1 : 0.7}
                >
                  {currentTicket.images && currentTicket.images.length > 0 ? (
                    <Image
                      source={{ uri: currentTicket.images[0] }}
                      style={styles.mainTicketImage}
                    />
                  ) : (
                    <View style={styles.mainTicketPlaceholder}>
                      <Text style={styles.placeholderText}>
                        {isPlaceholder
                          ? '새 티켓을 추가해보세요!'
                          : '이미지 없음'}
                      </Text>
                    </View>
                  )}
                </TouchableOpacity>
              </Animated.View>
              
              {/* 하단 date 버튼 */}
              {!isPlaceholder && (
                <View style={styles.dateButtonContainer}>
                  <TouchableOpacity style={styles.dateButton}>
                    <Text style={styles.dateButtonText}>
                      {formatDate(currentTicket.performedAt)}
                    </Text>
                  </TouchableOpacity>
                </View>
              )}
            </View>

          </View>
        </TouchableWithoutFeedback>

        {selectedTicket && (
          <TicketDetailModal
            visible={modalVisible}
            ticket={selectedTicket}
            onClose={handleCloseModal}
            isMine={true}
          />
        )}
      </SafeAreaView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.systemBackground },
  safeArea: { flex: 1 },

  headerRight: { position: 'relative' },

  // 헤더 티켓 필터링
  filterButton: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.xs,
    borderRadius: BorderRadius.lg,
    borderWidth: 0.5,
    borderColor: Colors.systemGray5,
    transform: [{ translateY: 10 }],
  },
  filterButtonText: {
    ...Typography.subheadline,
    color: Colors.secondaryLabel,
    marginRight: Spacing.xs,
  },
  filterArrow: { fontSize: 10, color: Colors.secondaryLabel },
  filterDropdown: {
    position: 'absolute',
    top: 38,
    right: 0,
    backgroundColor: Colors.systemBackground,
    borderRadius: BorderRadius.xl,
    minWidth: 110,
    borderWidth: 0.5,
    borderColor: Colors.systemGray5,
    ...Shadows.large,
    zIndex: 1000,
  },
  filterOption: { paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md },
  filterOptionText: {
    ...Typography.subheadline,
    color: Colors.label,
    textAlign: 'center',
  },

  // 서브 헤더
  subHeader: {
    paddingHorizontal: Spacing.screenPadding,
    paddingTop: Spacing.xl,
    paddingBottom: Spacing.lg,
    backgroundColor: Colors.systemBackground,
  },
  monthTitle: {
    ...Typography.title1,
    fontWeight: '600',
    color: Colors.label,
    marginBottom: Spacing.sm,
  },
  monthSubtitle: {
    ...Typography.subheadline,
    color: Colors.secondaryLabel,
    lineHeight: 20,
  },

  // 콘텐츠
  contentContainer: {
    flex: 1,
    paddingTop: 16,
    paddingHorizontal: Spacing.screenPadding,
  },
  cardContainer: { alignItems: 'center', flex: 1 },
  
  // 인디케이터
  dots: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: Spacing.md,
  },
  dot: { height: 6, borderRadius: 3, marginHorizontal: Spacing.xs },

  // 애니메이션 카드
  animatedCard: { alignItems: 'center' },
  mainTicketCard: {
    width: (width - 80) * 1.05,
    height: (width - 80) * 1.3 * 1.05,
    borderRadius: BorderRadius.xxl,
    overflow: 'hidden',
    backgroundColor: Colors.systemGray6,
    shadowColor: Colors.label,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.15,
    shadowRadius: 20,
  },
  mainTicketCardNoImage: {
  },

  disabledCard: { opacity: 0.75 },

  mainTicketImage: { width: '100%', height: '100%', resizeMode: 'cover' },
  
  mainTicketPlaceholder: {
    flex: 1,
    backgroundColor: Colors.systemGray6,
    justifyContent: 'center',
    alignItems: 'center',
    width: '100%',
    height: '100%',
  },
  placeholderText: {
    ...Typography.callout,
    color: Colors.tertiaryLabel,
    fontWeight: '400',
  },

  // 하단 date 버튼
  dateButtonContainer: { marginTop: 12, alignItems: 'center' },
  dateButton: {
    backgroundColor: Colors.secondarySystemBackground,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: BorderRadius.xl,
    borderWidth: 1,
    borderColor: Colors.systemGray5,
    ...Shadows.small,
  },
  dateButtonText: {
    ...Typography.footnote,
    color: Colors.label,
    fontWeight: '500',
  },
});

export default MainPage;
