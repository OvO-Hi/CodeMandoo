/**
 * 이벤트 카드 컴포넌트
 * 캘린더 화면에서 선택된 날짜의 공연 목록을 표시하는 카드
 * 공연 제목, 장소, 아티스트 정보를 간략하게 보여줌
 */
import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Ticket } from '../types/ticket';
import { Colors, Typography, Spacing, BorderRadius, Shadows, ComponentStyles } from '../styles/designSystem';
import { EventCardProps } from '../types/componentProps';

const EventCard: React.FC<EventCardProps> = ({ ticket, onPress }) => {
  return (
    <TouchableOpacity
      style={styles.eventCard}
      onPress={() => onPress(ticket)}
    >
      {/* 이벤트 정보 영역 */}
      <View style={styles.eventContent}>
        {/* 공연 제목 */}
        <Text style={styles.eventTitle}>{ticket.title}</Text>
        {/* 공연 장소와 아티스트 정보 */}
        <Text style={styles.eventDetails}>
          @{ticket.place || '장소미정'} {ticket.artist ? ticket.artist : ''}
        </Text>
      </View>
      {/* 오른쪽 화살표 아이콘 */}
      <Text style={styles.chevron}>›</Text>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  eventCard: {
    ...ComponentStyles.card,
    flexDirection: 'row',
    alignItems: 'center',
    padding: Spacing.xl,
  },
  eventContent: {
    flex: 1,
  },
  eventTitle: {
    ...Typography.headline,
    color: Colors.label,
    marginBottom: Spacing.xs,
  },
  eventDetails: {
    ...Typography.subheadline,
    color: Colors.systemGray,
  },
  chevron: {
    fontSize: 32,
    color: Colors.systemGray3,
    fontWeight: '300',
  },
});

export default EventCard;
