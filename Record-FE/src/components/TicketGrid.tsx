import React from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  Image,
  Dimensions,
  ViewStyle,
} from 'react-native';
import { Ticket } from '../types/ticket';
import { Colors, Typography, Spacing, BorderRadius, Shadows } from '../styles/designSystem';

interface TicketGridProps {
  tickets: Ticket[];
  onTicketPress: (ticket: Ticket) => void;
  containerStyle?: ViewStyle;
  cardWidth?: number;
  cardAspectRatio?: number;
}

const TicketGrid: React.FC<TicketGridProps> = ({ 
  tickets, 
  onTicketPress,
  containerStyle,
  cardWidth: customCardWidth,
  cardAspectRatio = 1.4
}) => {
  const { width } = Dimensions.get('window');
  const defaultCardWidth = (width - 10) / 3; // 3 columns with padding (20px * 2 + 20px gaps)
  const cardWidth = customCardWidth || defaultCardWidth;
  const cardHeight = cardWidth * cardAspectRatio;
  const renderTicketCard = ({ item }: { item: Ticket }) => {
    const hasImages = item.images && item.images.length > 0;
    
    return (
      <TouchableOpacity
        style={[
          styles.ticketCard,
          { width: cardWidth, height: cardHeight },
          !hasImages && styles.ticketCardNoImage
        ]}
        onPress={() => onTicketPress(item)}
      >
        {hasImages ? (
          <Image source={{ uri: item.images![0] }} style={styles.ticketImage} />
        ) : (
          <View style={styles.ticketImagePlaceholder}>
            <Text style={styles.ticketTitle} numberOfLines={2}>
              {item.title}
            </Text>
          </View>
        )}
      </TouchableOpacity>
    );
  };

  if (tickets.length === 0) {
    return (
      <View style={styles.emptyContainer}>
        <Text style={styles.emptyText}>아직 티켓이 없습니다</Text>
      </View>
    );
  }

  return (
    <View style={[styles.gridContainer, containerStyle]}>
      <FlatList
        data={tickets}
        renderItem={renderTicketCard}
        keyExtractor={(item, index) => item.id || `ticket-${index}`}
        extraData={tickets.length}
        numColumns={3}
        scrollEnabled={false}
        contentContainerStyle={styles.gridContent}
        columnWrapperStyle={styles.gridRow}
        showsVerticalScrollIndicator={false}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  gridContainer: {
    flex: 1,
    paddingBottom: Spacing.xl,
  },
  gridContent: {
    paddingHorizontal: 2,
    paddingBottom: Spacing.sectionSpacing,
  },
  gridRow: {
    justifyContent: 'flex-start',
    gap: 3,
  },
  
  ticketCard: {
    backgroundColor: Colors.systemBackground,
    borderRadius: BorderRadius.md,
    marginBottom: 4,
    overflow: 'hidden',
  },
  ticketCardNoImage: {
    borderWidth: 0.5,
    borderColor: Colors.tertiaryLabel,
  },
  ticketImage: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  ticketImagePlaceholder: {
    flex: 1,
    padding: Spacing.md,
    alignItems: 'flex-start',
    backgroundColor: Colors.white,
  },
  ticketTitle: {
    ...Typography.caption1,
    fontWeight: 'bold',
    color: Colors.label,
    marginBottom: Spacing.xs,
  },
  ticketArtist: {
    ...Typography.caption2,
    color: Colors.secondaryLabel,
    textAlign: 'center',
  },

  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 60,
  },
  emptyText: {
    ...Typography.callout,
    color: Colors.systemGray,
    textAlign: 'center',
  },
});

export default TicketGrid;
