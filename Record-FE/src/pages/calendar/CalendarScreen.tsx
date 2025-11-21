import React, { useState, useRef, useEffect } from 'react';
import { View, StyleSheet, StatusBar, ScrollView, Animated } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useNavigation, useFocusEffect } from '@react-navigation/native';
import { useAtom } from 'jotai';
import { ticketsAtom } from '../../atoms';
import { Ticket } from '../../types/ticket';
import TicketDetailModal from '../../components/TicketDetailModal';
import CalendarHeader from '../../components/CalendarHeader';
import CustomCalendar from '../../components/CustomCalendar';
import WeeklyCalendar from '../../components/WeeklyCalendar';
import EventsList from '../../components/EventsList';
import { Colors } from '../../styles/designSystem';
import { useCallback } from 'react';

// 캘린더 화면 Props 타입 정의
interface CalendarProps {
  navigation: any;
}

const CalendarScreen = () => {
  const navigation = useNavigation();
  // 선택된 날짜 상태 (오늘 날짜로 초기화)
  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0],
  );
  const [currentMonth, setCurrentMonth] = useState(
    new Date().toISOString().slice(0, 7), // YYYY-MM 형식
  );
  const [tickets] = useAtom(ticketsAtom); // 전체 티켓 목록
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null); // 선택된 티켓
  const [modalVisible, setModalVisible] = useState(false); // 모달 표시 여부
  
  // 스크롤 애니메이션을 위한 Animated Value
  const scrollY = useRef(new Animated.Value(0)).current;
  const scrollViewRef = useRef<ScrollView>(null);
  
  // 월간/주간 캘린더 전환을 위한 상태
  const [isWeeklyView, setIsWeeklyView] = useState(false);

  // useFocusEffect를 사용하여 화면에 포커스될 때마다 뷰를 월간으로 초기화
  useFocusEffect(
    useCallback(() => {
      setIsWeeklyView(false);
      // 스크롤 위치도 함께 초기화
      if (scrollViewRef.current) {
        scrollViewRef.current.scrollTo({ y: 0, animated: false });
      }
    }, [])
  );

  // 날짜를 YYYY-MM-DD 형식으로 포맷팅
  const formatDate = (date: Date) => date.toISOString().split('T')[0];

  // 선택된 날짜의 공연 목록 필터링
  const selectedEvents = tickets.filter(
    ticket => formatDate(new Date(ticket.performedAt)) === selectedDate,
  );

  // 캘린더에서 날짜 선택 처리 (월간 캘린더용)
  const handleDayPress = (day: { dateString: string }) => {
    setSelectedDate(day.dateString);
  };

  // 주간 캘린더에서 날짜 선택 처리
  const handleWeeklyDayPress = (dateString: string) => {
    setSelectedDate(dateString);
  };

  // 월 변경 처리
  const handleMonthChange = (month: { dateString: string }) => {
    const newMonth = month.dateString.slice(0, 7); // YYYY-MM 형식
    
    // 월이 변경된 경우에만 처리
    if (newMonth !== currentMonth) {
      setCurrentMonth(newMonth);
      
      // 해당 월의 1일로 선택 날짜 변경
      const firstDayOfMonth = `${newMonth}-01`;
      setSelectedDate(firstDayOfMonth);
    }
  };

  // 스크롤 이벤트 처리 - 스크롤 위치에 따라 캘린더 뷰 전환
  const handleScroll = Animated.event(
    [{ nativeEvent: { contentOffset: { y: scrollY } } }],
    {
      useNativeDriver: false,
      listener: (event: any) => {
        const offsetY = event.nativeEvent.contentOffset.y;
        // 스크롤이 100px 이상 내려가면 주간 뷰로 전환
        if (offsetY > 100 && !isWeeklyView) {
          setIsWeeklyView(true);
        } else if (offsetY <= 50 && isWeeklyView) {
          setIsWeeklyView(false);
        }
      },
    },
  );

  // 티켓 카드 클릭 시 상세 모달 열기
  const handleTicketPress = (ticket: Ticket) => {
    setSelectedTicket(ticket);
    setModalVisible(true);
  };

  // 티켓 상세 모달 닫기
  const handleCloseModal = () => {
    setModalVisible(false);
    setSelectedTicket(null);
  };

  // 전체 티켓 개수 계산
  const totalTickets = tickets.length;

  // 월간 캘린더 애니메이션 스타일
  const monthlyCalendarStyle = {
    opacity: scrollY.interpolate({
      inputRange: [0, 100],
      outputRange: [1, 0],
      extrapolate: 'clamp',
    }),
    transform: [
      {
        translateY: scrollY.interpolate({
          inputRange: [0, 100],
          outputRange: [0, -50],
          extrapolate: 'clamp',
        }),
      },
    ],
  };

  // 주간 캘린더 애니메이션 스타일
  const weeklyCalendarStyle = {
    opacity: scrollY.interpolate({
      inputRange: [50, 100],
      outputRange: [0, 1],
      extrapolate: 'clamp',
    }),
    transform: [
      {
        translateY: scrollY.interpolate({
          inputRange: [50, 100],
          outputRange: [50, 0],
          extrapolate: 'clamp',
        }),
      },
    ],
  };

  return (
    <SafeAreaView style={styles.container}>
            <StatusBar barStyle="dark-content" />
      
      <CalendarHeader totalTickets={totalTickets} />
      
      {isWeeklyView && (
        <Animated.View style={[styles.weeklyCalendarContainer, weeklyCalendarStyle]}>
          <WeeklyCalendar
            selectedDate={selectedDate}
            tickets={tickets}
            onDayPress={handleWeeklyDayPress}
          />
        </Animated.View>
      )}
      
      <ScrollView
        ref={scrollViewRef}
        style={styles.scrollView}
        onScroll={handleScroll}
        scrollEventThrottle={16}
        showsVerticalScrollIndicator={false}
      >
        {/* 월별 캘린더 컴포넌트 (스크롤 시 사라짐) */}
        <Animated.View style={monthlyCalendarStyle}>
          <CustomCalendar
            selectedDate={selectedDate}
            tickets={tickets}
            onDayPress={handleDayPress}
            onMonthChange={handleMonthChange}
          />
        </Animated.View>
        
        {/* 선택된 날짜의 공연 목록 */}
        <View style={styles.eventsContainer}>
          <EventsList
            selectedEvents={selectedEvents}
            onTicketPress={handleTicketPress}
          />
        </View>
      </ScrollView>

      {/* 티켓 상세 모달 */}
      {selectedTicket && (
        <TicketDetailModal
          visible={modalVisible}
          ticket={selectedTicket}
          onClose={handleCloseModal}
          isMine={true}
        />
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.systemBackground,
  },
  weeklyCalendarContainer: {
    backgroundColor: Colors.systemBackground,
    zIndex: 10,
  },
  scrollView: {
    flex: 1,
  },
  eventsContainer: {
    flex: 1,
    minHeight: 450,
  },
});

export default CalendarScreen;