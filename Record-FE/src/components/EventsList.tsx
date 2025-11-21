import React from 'react';
import { View, Text, ScrollView, StyleSheet } from 'react-native';
import { Ticket } from '../types/ticket';
import EventCard from './EventCard';
import { Colors, Typography, Spacing } from '../styles/designSystem';

interface EventsListProps {
  selectedEvents: Ticket[];
  onTicketPress: (ticket: Ticket) => void;
}

const EventsList: React.FC<EventsListProps> = ({ selectedEvents, onTicketPress }) => {
  // 날짜를 한 번만 표시하기 위한 로직
  const eventDate = selectedEvents.length > 0 ? new Date(selectedEvents[0].performedAt) : null;
  const dateString = eventDate ? `${eventDate.getMonth() + 1}월 ${eventDate.getDate()}일` : '';

  return (
    <ScrollView style={styles.eventsContainer}>
      {selectedEvents.length > 0 ? (
        <>
          <View style={styles.eventSection}>
            <Text style={styles.eventDateTitle}>{dateString}</Text>
            {selectedEvents.map(ticket => (
              <EventCard key={ticket.id} ticket={ticket} onPress={onTicketPress} />
            ))}
          </View>
        </>
      ) : (
        <View style={styles.noEvents}>
          <Text style={styles.noEventsText}>선택한 날짜에 일정이 없습니다.</Text>
        </View>
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  eventsContainer: {
    flex: 1,
    paddingHorizontal: Spacing.screenPadding,
  },
  eventSection: {
    marginBottom: Spacing.sectionSpacing,
    gap: Spacing.md,
  },
  eventDateTitle: {
    ...Typography.title3,
    fontWeight: '700',
    color: Colors.label,
    marginTop: Spacing.md,
    marginBottom: Spacing.xs, // 날짜와 카드 사이 간격 감소
  },
  noEvents: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: Spacing.xxxl + Spacing.sm,
  },
  noEventsText: {
    ...Typography.body,
    color: Colors.systemGray,
    textAlign: 'center',
  },
});

export default EventsList;