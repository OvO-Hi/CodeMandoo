import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Calendar, LocaleConfig } from 'react-native-calendars';
import { Ticket } from '../types/ticket';

interface CustomCalendarProps {
  selectedDate: string;
  tickets: Ticket[];
  onDayPress: (day: { dateString: string }) => void;
  onMonthChange?: (month: { dateString: string }) => void;
}

// Configure calendar locale
LocaleConfig.locales['ko'] = {
  monthNames: [
    '1월',
    '2월',
    '3월',
    '4월',
    '5월',
    '6월',
    '7월',
    '8월',
    '9월',
    '10월',
    '11월',
    '12월',
  ],
  monthNamesShort: [
    '1월',
    '2월',
    '3월',
    '4월',
    '5월',
    '6월',
    '7월',
    '8월',
    '9월',
    '10월',
    '11월',
    '12월',
  ],
  dayNames: [
    '일요일',
    '월요일',
    '화요일',
    '수요일',
    '목요일',
    '금요일',
    '토요일',
  ],
  dayNamesShort: ['일', '월', '화', '수', '목', '금', '토'],
  today: '오늘',
};
LocaleConfig.defaultLocale = 'ko';

const CustomCalendar: React.FC<CustomCalendarProps> = ({
  selectedDate,
  tickets,
  onDayPress,
  onMonthChange,
}) => {
  // Format date to YYYY-MM-DD
  const formatDate = (date: Date) => date.toISOString().split('T')[0];

  // Mark dates with events
  const markedDates: { [key: string]: any } = tickets.reduce((acc, ticket) => {
    const date = formatDate(new Date(ticket.performedAt));
    return {
      ...acc,
      [date]: {
        marked: true,
        dotColor: '#B11515',
        selected: date === selectedDate,
        selectedColor: '#B11515',
      },
    };
  }, {} as { [key: string]: any });

  return (
    <View style={styles.calendarContainer}>
      <Calendar
        current={selectedDate}
        onDayPress={onDayPress}
        onMonthChange={onMonthChange}
        markedDates={{
          ...markedDates,
          [selectedDate]: {
            ...(markedDates[selectedDate] || {}),
            selected: true,
            selectedColor: '#B11515',
          },
        }}
        theme={{
          backgroundColor: '#ffffff',
          calendarBackground: '#ffffff',
          textSectionTitleColor: '#000000',
          selectedDayBackgroundColor: '#B11515',
          selectedDayTextColor: '#ffffff',
          todayTextColor: '#B11515',
          dayTextColor: '#000000',
          textDisabledColor: '#8E8E93',
          dotColor: '#B11515',
          selectedDotColor: '#ffffff',
          arrowColor: '#000000',
          monthTextColor: '#000000',
          textDayFontWeight: '400',
          textMonthFontWeight: '600',
          textDayHeaderFontWeight: '500',
          textDayFontSize: 17,
          textMonthFontSize: 20,
          textDayHeaderFontSize: 15,
        }}
        style={styles.calendar}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  calendarContainer: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    marginHorizontal: 20,
    marginBottom: 30,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  calendar: {
    borderRadius: 16,
  },
});

export default CustomCalendar;