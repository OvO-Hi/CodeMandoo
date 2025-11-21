/**
 * 주간 캘린더 컴포넌트
 * 월간 캘린더에서 스크롤 시 나타나는 주간 뷰
 * 선택된 주의 7일을 가로로 표시하며 각 날짜의 이벤트 표시
 * 좌우 스와이프로 주 단위 네비게이션 지원
 */
import React, { useState, useRef, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Dimensions, PanResponder } from 'react-native';
import { Ticket } from '../types/ticket';

interface WeeklyCalendarProps {
  selectedDate: string;
  tickets: Ticket[];
  onDayPress: (dateString: string) => void;
}

const WeeklyCalendar: React.FC<WeeklyCalendarProps> = ({
  selectedDate,
  tickets,
  onDayPress,
}) => {
  // 현재 표시할 주의 기준 날짜 상태
  const [currentWeekDate, setCurrentWeekDate] = useState(selectedDate);

  // selectedDate가 변경될 때 currentWeekDate도 업데이트
  useEffect(() => {
    setCurrentWeekDate(selectedDate);
  }, [selectedDate]);

  // 선택된 날짜를 기준으로 해당 주의 시작일(일요일) 계산
  const getWeekStart = (dateString: string) => {
    const date = new Date(dateString);
    const day = date.getDay(); // 0: 일요일, 1: 월요일, ...
    const diff = date.getDate() - day;
    return new Date(date.setDate(diff));
  };

  // 주간 날짜 배열 생성 (일요일부터 토요일까지)
  const getWeekDates = (startDate: Date) => {
    const dates = [];
    for (let i = 0; i < 7; i++) {
      const date = new Date(startDate);
      date.setDate(startDate.getDate() + i);
      dates.push(date);
    }
    return dates;
  };

  // 날짜를 YYYY-MM-DD 형식으로 포맷팅
  const formatDate = (date: Date) => date.toISOString().split('T')[0];

  // 해당 날짜에 티켓이 있는지 확인
  const hasTicket = (date: Date) => {
    const dateString = formatDate(date);
    return tickets.some(ticket => formatDate(new Date(ticket.performedAt)) === dateString);
  };

  // 해당 날짜의 티켓 개수 계산
  const getTicketCount = (date: Date) => {
    const dateString = formatDate(date);
    return tickets.filter(ticket => formatDate(new Date(ticket.performedAt)) === dateString).length;
  };

  // 이전 주로 이동 (토요일 선택)
  const goToPreviousWeek = () => {
    const currentDate = new Date(currentWeekDate);
    currentDate.setDate(currentDate.getDate() - 7);
    const newWeekDate = formatDate(currentDate);
    setCurrentWeekDate(newWeekDate);
    
    // 이전 주로 이동 시 토요일(6번째 인덱스) 선택
    const weekStart = getWeekStart(newWeekDate);
    const saturdayDate = new Date(weekStart);
    saturdayDate.setDate(weekStart.getDate() + 6); // 토요일
    onDayPress(formatDate(saturdayDate));
  };

  // 다음 주로 이동 (일요일 선택)
  const goToNextWeek = () => {
    const currentDate = new Date(currentWeekDate);
    currentDate.setDate(currentDate.getDate() + 7);
    const newWeekDate = formatDate(currentDate);
    setCurrentWeekDate(newWeekDate);
    
    // 다음 주로 이동 시 일요일(0번째 인덱스) 선택
    const weekStart = getWeekStart(newWeekDate);
    onDayPress(formatDate(weekStart));
  };

  // 스와이프 제스처 처리
  const panResponder = PanResponder.create({
    onMoveShouldSetPanResponder: (evt, gestureState) => {
      // 수평 스와이프만 감지 (최소 30px 이동, 수직 이동은 수평 이동의 절반 이하)
      return Math.abs(gestureState.dx) > 30 && Math.abs(gestureState.dy) < Math.abs(gestureState.dx) / 2;
    },
    onPanResponderMove: () => {},
    onPanResponderRelease: (evt, gestureState) => {
      // 오른쪽 스와이프 (이전 주)
      if (gestureState.dx > 50) {
        goToPreviousWeek();
      }
      // 왼쪽 스와이프 (다음 주)
      else if (gestureState.dx < -50) {
        goToNextWeek();
      }
    },
  });

  const weekStart = getWeekStart(currentWeekDate);
  const weekDates = getWeekDates(weekStart);
  const dayNames = ['일', '월', '화', '수', '목', '금', '토'];

  // 오늘 날짜
  const today = formatDate(new Date());

  // 현재 주의 월 정보 (주가 두 달에 걸쳐있을 수 있으므로 선택된 날짜 기준)
  const currentMonth = new Date(currentWeekDate).getMonth() + 1; // 0-based이므로 +1
  const currentYear = new Date(currentWeekDate).getFullYear();

  // 화면 너비 계산
  const screenWidth = Dimensions.get('window').width;
  const dayWidth = (screenWidth - 80) / 7; // 좌우 패딩과 네비게이션 버튼 공간 고려

  return (
    <View style={styles.container} {...panResponder.panHandlers}>
      {/* 월 표시 헤더와 네비게이션 */}
      <View style={styles.headerContainer}>
        <TouchableOpacity style={styles.navButton} onPress={goToPreviousWeek}>
          <Text style={styles.navButtonText}>‹</Text>
        </TouchableOpacity>
        
        <Text style={styles.monthText}>{currentYear}년 {currentMonth}월</Text>
        
        <TouchableOpacity style={styles.navButton} onPress={goToNextWeek}>
          <Text style={styles.navButtonText}>›</Text>
        </TouchableOpacity>
      </View>
      
      {/* 주간 날짜 표시 */}
      <View style={styles.weekContainer}>
        {weekDates.map((date, index) => {
          const dateString = formatDate(date);
          const isSelected = dateString === selectedDate;
          const isToday = dateString === today;
          const hasEvent = hasTicket(date);
          const ticketCount = getTicketCount(date);

          return (
            <TouchableOpacity
              key={dateString}
              style={[
                styles.dayContainer,
                { width: dayWidth },
                isSelected && styles.selectedDay,
                isToday && !isSelected && styles.todayDay,
              ]}
              onPress={() => onDayPress(dateString)}
            >
              {/* 요일 표시 */}
              <Text style={[
                styles.dayName,
                isSelected && styles.selectedText,
                isToday && !isSelected && styles.todayText,
              ]}>
                {dayNames[index]}
              </Text>
              
              {/* 날짜 표시 */}
              <Text style={[
                styles.dayNumber,
                isSelected && styles.selectedText,
                isToday && !isSelected && styles.todayText,
              ]}>
                {date.getDate()}
              </Text>
              
              {/* 이벤트 표시 */}
              {hasEvent && (
                <View style={[
                  styles.eventIndicator,
                  isSelected && styles.selectedEventIndicator,
                ]}>
                  {ticketCount > 1 && (
                    <Text style={[
                      styles.eventCount,
                      isSelected && styles.selectedEventCount,
                    ]}>
                      {ticketCount}
                    </Text>
                  )}
                </View>
              )}
            </TouchableOpacity>
          );
        })}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    marginHorizontal: 20,
    marginBottom: 20,
    paddingVertical: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  headerContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingBottom: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
    marginBottom: 12,
  },
  navButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#F8F9FA',
    justifyContent: 'center',
    alignItems: 'center',
  },
  navButtonText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#B11515',
  },
  monthText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#2C3E50',
    flex: 1,
    textAlign: 'center',
  },
  weekContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 8,
  },
  dayContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 12,
    paddingHorizontal: 4,
    borderRadius: 12,
    position: 'relative',
  },
  selectedDay: {
    backgroundColor: '#B11515',
  },
  todayDay: {
    backgroundColor: '#F8F9FA',
    borderWidth: 1,
    borderColor: '#B11515',
  },
  dayName: {
    fontSize: 12,
    fontWeight: '500',
    color: '#6C757D',
    marginBottom: 4,
  },
  dayNumber: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2C3E50',
  },
  selectedText: {
    color: '#FFFFFF',
  },
  todayText: {
    color: '#B11515',
  },
  eventIndicator: {
    position: 'absolute',
    bottom: 4,
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#B11515',
  },
  selectedEventIndicator: {
    backgroundColor: '#FFFFFF',
  },
  eventCount: {
    fontSize: 8,
    fontWeight: 'bold',
    color: '#FFFFFF',
    textAlign: 'center',
    lineHeight: 6,
  },
  selectedEventCount: {
    color: '#B11515',
  },
});

export default WeeklyCalendar;
