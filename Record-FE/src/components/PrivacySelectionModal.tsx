/**
 * 공유 범위 선택 모달 컴포넌트
 * BeReal 스타일의 공유 범위 선택 UI
 */
import React from 'react';
import {
  View,
  Text,
  Modal,
  TouchableOpacity,
  StyleSheet,
  TouchableWithoutFeedback,
  Dimensions,
} from 'react-native';
import { TicketStatus, TICKET_STATUS_LABELS } from '../atoms';

const { width } = Dimensions.get('window');

interface PrivacySelectionModalProps {
  visible: boolean;
  currentStatus: TicketStatus;
  onClose: () => void;
  onSelect: (status: TicketStatus) => void;
}

const PrivacySelectionModal: React.FC<PrivacySelectionModalProps> = ({
  visible,
  currentStatus,
  onClose,
  onSelect,
}) => {
  return (
    <Modal
      visible={visible}
      transparent={true}
      animationType="slide"
      onRequestClose={onClose}
    >
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.overlay}>
          <TouchableWithoutFeedback>
            <View style={styles.content}>
              <View style={styles.header}>
                <Text style={styles.title}>공유 범위를 선택하세요.</Text>
                <TouchableOpacity onPress={onClose} style={styles.closeButton}>
                  <Text style={styles.closeButtonText}>✕</Text>
                </TouchableOpacity>
              </View>

              <TouchableOpacity
                style={[
                  styles.option,
                  currentStatus === TicketStatus.PRIVATE && styles.optionSelected,
                ]}
                onPress={() => onSelect(TicketStatus.PRIVATE)}
              >
                <View style={styles.optionIcon}>
                  <Text style={styles.iconText}>🔒</Text>
                </View>
                <View style={styles.optionContent}>
                  <Text
                    style={[
                      styles.optionTitle,
                      currentStatus === TicketStatus.PRIVATE &&
                        styles.optionTitleSelected,
                    ]}
                  >
                    {TICKET_STATUS_LABELS[TicketStatus.PRIVATE]}
                  </Text>
                  <Text style={styles.optionDescription}>
                    나만 볼 수 있습니다.
                  </Text>
                </View>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.option,
                  currentStatus === TicketStatus.FRIENDS && styles.optionSelected,
                ]}
                onPress={() => onSelect(TicketStatus.FRIENDS)}
              >
                <View style={styles.optionIcon}>
                  <Text style={styles.iconText}>👤</Text>
                </View>
                <View style={styles.optionContent}>
                  <Text
                    style={[
                      styles.optionTitle,
                      currentStatus === TicketStatus.FRIENDS &&
                        styles.optionTitleSelected,
                    ]}
                  >
                    {TICKET_STATUS_LABELS[TicketStatus.FRIENDS]}
                  </Text>
                  <Text style={styles.optionDescription}>
                    친구에게만 공유됩니다.
                  </Text>
                </View>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.option,
                  currentStatus === TicketStatus.PUBLIC && styles.optionSelected,
                ]}
                onPress={() => onSelect(TicketStatus.PUBLIC)}
              >
                <View style={styles.optionIcon}>
                  <Text style={styles.iconText}>🌐</Text>
                </View>
                <View style={styles.optionContent}>
                  <Text
                    style={[
                      styles.optionTitle,
                      currentStatus === TicketStatus.PUBLIC &&
                        styles.optionTitleSelected,
                    ]}
                  >
                    {TICKET_STATUS_LABELS[TicketStatus.PUBLIC]}
                  </Text>
                  <Text style={styles.optionDescription}>
                    모든 사람과 공유하고, 프로필에서 볼 수 있습니다.
                  </Text>
                </View>
              </TouchableOpacity>
            </View>
          </TouchableWithoutFeedback>
        </View>
      </TouchableWithoutFeedback>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    justifyContent: 'flex-end',
  },
  content: {
    backgroundColor: '#1A1A1A',
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    padding: 20,
    paddingBottom: 40,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  title: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFF',
  },
  closeButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#2A2A2A',
    justifyContent: 'center',
    alignItems: 'center',
  },
  closeButtonText: {
    fontSize: 18,
    color: '#FFF',
  },
  option: {
    flexDirection: 'row',
    backgroundColor: '#2A2A2A',
    borderRadius: 16,
    padding: 16,
    marginBottom: 12,
    alignItems: 'flex-start',
  },
  optionSelected: {
    backgroundColor: '#FFF',
  },
  optionIcon: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#3A3A3A',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  iconText: {
    fontSize: 20,
  },
  optionContent: {
    flex: 1,
  },
  optionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFF',
    marginBottom: 4,
  },
  optionTitleSelected: {
    color: '#1A1A1A',
  },
  optionDescription: {
    fontSize: 13,
    color: '#999',
    lineHeight: 18,
  },
});

export default PrivacySelectionModal;