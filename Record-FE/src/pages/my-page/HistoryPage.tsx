import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  FlatList,
} from 'react-native';
import {
  SafeAreaView,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';
import { useAtom } from 'jotai';
import { ticketsAtom } from '../../atoms';
import { Ticket } from '../../types/ticket';
import { isPlaceholderTicket } from '../../utils/isPlaceholder';
import { Colors, Typography, Spacing, BorderRadius, Shadows, ComponentStyles, Layout } from '../../styles/designSystem';

interface HistoryPageProps {
  navigation: any;
}

type FilterType = 'all' | 'recent' | 'thisMonth' | 'thisYear';

const HistoryPage: React.FC<HistoryPageProps> = ({ navigation }) => {
  const insets = useSafeAreaInsets();
  const [tickets] = useAtom(ticketsAtom);
  const [selectedFilter, setSelectedFilter] = useState<FilterType>('all');

  // 실제 티켓만 필터링
  const realTickets = tickets.filter(ticket => !isPlaceholderTicket(ticket));

  // 날짜별 필터링 함수
  const getFilteredTickets = () => {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();

    switch (selectedFilter) {
      case 'recent':
        // 최근 7일
        const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        return realTickets.filter(ticket => {
          const ticketDate = ticket.createdAt ? new Date(ticket.createdAt) : new Date();
          return ticketDate >= sevenDaysAgo;
        });
      
      case 'thisMonth':
        return realTickets.filter(ticket => {
          const ticketDate = ticket.createdAt ? new Date(ticket.createdAt) : new Date();
          return ticketDate.getMonth() === currentMonth && ticketDate.getFullYear() === currentYear;
        });
      
      case 'thisYear':
        return realTickets.filter(ticket => {
          const ticketDate = ticket.createdAt ? new Date(ticket.createdAt) : new Date();
          return ticketDate.getFullYear() === currentYear;
        });
      
      default:
        return realTickets;
    }
  };

  const filteredTickets = getFilteredTickets().sort((a, b) => {
    const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
    const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
    return dateB - dateA; // 최신순 정렬
  });

  const filterOptions = [
    { key: 'all', label: '전체', count: realTickets.length },
    { key: 'recent', label: '최근 7일', count: getFilteredTickets().length },
    { key: 'thisMonth', label: '이번 달', count: realTickets.filter(ticket => {
      const ticketDate = ticket.createdAt ? new Date(ticket.createdAt) : new Date();
      const now = new Date();
      return ticketDate.getMonth() === now.getMonth() && ticketDate.getFullYear() === now.getFullYear();
    }).length },
    { key: 'thisYear', label: '올해', count: realTickets.filter(ticket => {
      const ticketDate = ticket.createdAt ? new Date(ticket.createdAt) : new Date();
      return ticketDate.getFullYear() === new Date().getFullYear();
    }).length },
  ];

  const formatDate = (dateString?: string) => {
    if (!dateString) return '날짜 없음';
    const date = new Date(dateString);
    return `${date.getFullYear()}.${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getDate().toString().padStart(2, '0')}`;
  };

  const renderTicketItem = ({ item }: { item: Ticket }) => (
    <TouchableOpacity style={styles.ticketItem}>
      <View style={styles.ticketHeader}>
        <Text style={styles.ticketTitle} numberOfLines={1}>
          {item.title || '제목 없음'}
        </Text>
        <Text style={styles.ticketDate}>{formatDate(item.createdAt?.toString())}</Text>
      </View>
      <Text style={styles.ticketLocation} numberOfLines={1}>
        📍 {item.place || '장소 없음'}
      </Text>
      <Text style={styles.ticketTime}>
        🕐 {item.performedAt ? new Date(item.performedAt).toLocaleDateString() : '날짜 없음'}
      </Text>
      {item.review && (
        <Text style={styles.ticketReview} numberOfLines={2}>
          💭 {item.review.reviewText}
        </Text>
      )}
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.backButtonText}>←</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>히스토리</Text>
        <View style={styles.placeholder} />
      </View>

      {/* Filter Tabs */}
      <ScrollView 
        horizontal 
        showsHorizontalScrollIndicator={false}
        style={styles.filterContainer}
        contentContainerStyle={styles.filterContent}
      >
        {filterOptions.map((option) => (
          <TouchableOpacity
            key={option.key}
            style={[
              styles.filterTab,
              selectedFilter === option.key && styles.filterTabActive,
            ]}
            onPress={() => setSelectedFilter(option.key as FilterType)}
          >
            <Text
              style={[
                styles.filterTabText,
                selectedFilter === option.key && styles.filterTabTextActive,
              ]}
            >
              {option.label}
            </Text>
            <Text
              style={[
                styles.filterTabCount,
                selectedFilter === option.key && styles.filterTabCountActive,
              ]}
            >
              {selectedFilter === option.key ? getFilteredTickets().length : option.count}
            </Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      {/* Tickets List */}
      <View style={styles.content}>
        {filteredTickets.length > 0 ? (
          <FlatList
            data={filteredTickets}
            renderItem={renderTicketItem}
            keyExtractor={(item) => item.id}
            showsVerticalScrollIndicator={false}
            contentContainerStyle={styles.listContent}
          />
        ) : (
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyIcon}>📋</Text>
            <Text style={styles.emptyTitle}>히스토리가 없습니다</Text>
            <Text style={styles.emptySubtitle}>
              {selectedFilter === 'all' 
                ? '아직 등록된 티켓이 없습니다'
                : '선택한 기간에 등록된 티켓이 없습니다'
              }
            </Text>
          </View>
        )}
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.secondarySystemBackground,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: Spacing.lg,
    height: Layout.navigationBarHeight,
    backgroundColor: Colors.systemBackground,
    borderBottomWidth: 0.5,
    borderBottomColor: Colors.separator,
    position: 'relative',
  },
  backButton: {
    position: 'absolute',
    left: Spacing.lg,
    width: 44,
    height: 44,
    borderRadius: BorderRadius.round,
    backgroundColor: 'transparent',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1,
  },
  backButtonText: {
    ...Typography.body,
    color: Colors.systemBlue,
    fontWeight: '400',
  },
  headerTitle: {
    ...Typography.headline,
    color: Colors.label,
  },
  placeholder: {
    position: 'absolute',
    right: Spacing.lg,
    width: 44,
    height: 44,
  },
  filterContainer: {
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E9ECEF',
  },
  filterContent: {
    paddingHorizontal: 20,
    paddingVertical: 16,
    gap: 12,
  },
  filterTab: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: '#F8F9FA',
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  filterTabActive: {
    backgroundColor: '#B11515',
  },
  filterTabText: {
    fontSize: 14,
    fontWeight: '500',
    color: '#6C757D',
  },
  filterTabTextActive: {
    color: '#FFFFFF',
  },
  filterTabCount: {
    fontSize: 12,
    fontWeight: '600',
    color: '#ADB5BD',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 10,
    minWidth: 20,
    textAlign: 'center',
  },
  filterTabCountActive: {
    color: '#B11515',
  },
  content: {
    flex: 1,
  },
  listContent: {
    padding: 16,
    gap: 12,
  },
  ticketItem: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  ticketHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  ticketTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2C3E50',
    flex: 1,
    marginRight: 12,
  },
  ticketDate: {
    fontSize: 12,
    color: '#6C757D',
    fontWeight: '500',
  },
  ticketLocation: {
    fontSize: 14,
    color: '#495057',
    marginBottom: 4,
  },
  ticketTime: {
    fontSize: 14,
    color: '#495057',
    marginBottom: 8,
  },
  ticketReview: {
    fontSize: 14,
    color: '#6C757D',
    fontStyle: 'italic',
    backgroundColor: '#F8F9FA',
    padding: 8,
    borderRadius: 8,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 32,
  },
  emptyIcon: {
    fontSize: 64,
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#2C3E50',
    marginBottom: 8,
  },
  emptySubtitle: {
    fontSize: 14,
    color: '#6C757D',
    textAlign: 'center',
    lineHeight: 20,
  },
});

export default HistoryPage;
