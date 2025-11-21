import { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Image,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Colors, Typography, Spacing, BorderRadius, Shadows, ComponentStyles, Layout } from '../../styles/designSystem';
import ModalHeader from '../../components/ModalHeader';

interface SentRequest {
  id: string;
  nickname: string;
  profileImage: string;
  isCancelled: boolean;
}

interface SentRequestsPageProps {
  navigation: any;
}

const SentRequestsPage: React.FC<SentRequestsPageProps> = ({ navigation }) => {
  const handleBack = () => {
    navigation.goBack();
  };

  // 더미 보낸 친구 요청 데이터
  const [sentRequests, setSentRequests] = useState<SentRequest[]>([
    {
      id: '@9rmmy',
      nickname: '9RMMY',
      profileImage: '👩🏻‍💼',
      isCancelled: false,
    },
    {
      id: '@alice',
      nickname: 'Alice',
      profileImage: '👩🏻‍🎤',
      isCancelled: false,
    },
  ]);

  // 요청 상태 토글
  const handleToggleRequest = (requestId: string) => {
    setSentRequests(prev =>
      prev.map(req =>
        req.id === requestId ? { ...req, isCancelled: !req.isCancelled } : req
      )
    );
  };
  

  return (
    <SafeAreaView style={styles.container}>
      <ModalHeader 
        title="보낸 요청"
        onBack={() => navigation.goBack()}
      />

      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        {/* 보낸 요청 섹션 */}
        <Text style={styles.sectionTitle}>
          보낸 요청 ({sentRequests.filter(r => !r.isCancelled).length})
        </Text>

        {/* 보낸 요청 목록 */}
        {sentRequests.map((request, index) => (
          <View 
            key={request.id} 
            style={[
              styles.requestItem,
              index === sentRequests.length - 1 && styles.lastRequestItem
            ]}
          >
            {/* 프로필 클릭 가능 */}
            <TouchableOpacity
              style={styles.requestInfo}
              onPress={() =>
                navigation.navigate('FriendProfile', { friend: request })
              }
            >
              <View style={styles.avatar}>
                <Text style={styles.avatarText}>{request.profileImage}</Text>
              </View>
              <View style={styles.requestDetails}>
                <Text style={styles.requestName}>{request.nickname}</Text>
                <Text style={styles.requestHandle}>{request.id}</Text>
              </View>
            </TouchableOpacity>

            <TouchableOpacity
              style={[
                styles.toggleButton,
                request.isCancelled ? styles.requestButton : styles.cancelButton,
              ]}
              onPress={() => handleToggleRequest(request.id)}
            >
              <Text style={styles.toggleButtonText}>
                {request.isCancelled ? '요청' : '취소'}
              </Text>
            </TouchableOpacity>
          </View>
        ))}
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.secondarySystemBackground,
  },
  content: {
    flex: 1,
    backgroundColor: Colors.secondarySystemBackground,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#2C3E50',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#FFFFFF',
    marginHorizontal: 16,
    marginTop: 16,
    borderTopLeftRadius: 12,
    borderTopRightRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  requestItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#FFFFFF',
    marginHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F3F4',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  requestInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  avatar: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: '#E9ECEF',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15,
  },
  avatarText: {
    fontSize: 24,
  },
  requestDetails: {
    flex: 1,
  },
  requestName: {
    fontSize: 16,
    fontWeight: '500',
    color: '#2C3E50',
    marginBottom: 2,
  },
  requestHandle: {
    fontSize: 14,
    color: '#6C757D',
  },
  toggleButton: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
  },
  cancelButton: {
    backgroundColor: '#DC3545',
  },
  requestButton: {
    backgroundColor: '#B11515',
  },
  toggleButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  lastRequestItem: {
    borderBottomLeftRadius: 12,
    borderBottomRightRadius: 12,
    borderBottomWidth: 0,
  },
});

export default SentRequestsPage;
