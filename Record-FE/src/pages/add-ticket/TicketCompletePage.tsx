import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Dimensions,
  StatusBar,
  Image,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAtom } from 'jotai';
import { TicketStatus, basePromptAtom } from '../../atoms';
import { ticketService } from '../../services/api';
import { TicketCreateRequest, TicketResponse } from '../../types/api/tickets';
import {
  Colors,
  Typography,
  Spacing,
  BorderRadius,
  Shadows,
} from '../../styles/designSystem';

interface TicketCompletePageProps {
  navigation: any;
  route?: {
    params?: {
      ticketData?: any;
      reviewData?: {
        reviewText?: string;
        text?: string;
        isPublic?: boolean;
      };
      images?: string[];
    };
  };
}

const { width, height } = Dimensions.get('window');

const TicketCompletePage: React.FC<TicketCompletePageProps> = ({ navigation, route }) => {
  const ticketData = route?.params?.ticketData;
  const reviewData = route?.params?.reviewData;
  const images = route?.params?.images ?? [];
  const [, setBasePrompt] = useAtom(basePromptAtom);
  const [isSaving, setIsSaving] = useState(false);
  const [saveResult, setSaveResult] = useState<TicketResponse | null>(null);

  /** 표시될 이미지 선택 */
  const ticketImage =
    images.length > 0
      ? images[0]
      : ticketData?.images?.length > 0
      ? ticketData.images[0]
      : null;

  console.log('=== TicketCompletePage 이미지 디버깅 ===');
  console.log('전달받은 images:', images);
  console.log('ticketData.images:', ticketData?.images);
  console.log('최종 표시할 이미지:', ticketImage);

  useEffect(() => {
    if (!ticketData) {
      console.warn('ticketData가 없습니다!');
      return;
    }

    let isMounted = true;
    let timer: ReturnType<typeof setTimeout> | null = null;

    const saveTicket = async () => {
      try {
        setIsSaving(true);

        const performedAt =
          ticketData?.performedAt instanceof Date
            ? ticketData.performedAt.toISOString()
            : ticketData?.performedAt ?? new Date().toISOString();

        const payload: TicketCreateRequest = {
          title: ticketData?.title || 'Untitled Ticket',
          venue: ticketData?.venue || '',
          genre: ticketData?.genre || '',
          performedAt,
          imageUrl: images?.[0],
          reviewText: reviewData?.reviewText || reviewData?.text || '',
          isPublic: reviewData?.isPublic === false ? false : true,
        };

        console.log('📤 티켓 저장 요청:', payload);
        const result = await ticketService.createTicket(payload);

        if (!isMounted) return;

        if (result.success) {
          console.log('✅ 티켓 저장 성공:', result.data);
          setSaveResult(result.data);
          setBasePrompt(null);

          timer = setTimeout(() => {
            navigation.reset({
              index: 0,
              routes: [{ name: 'MainTabs' }],
            });
          }, 2500);
        } else {
          console.error('❌ 티켓 저장 실패:', result.error);
          Alert.alert(
            '티켓 저장 실패',
            result.error?.message || '티켓을 저장하지 못했습니다. 잠시 후 다시 시도해주세요.'
          );
        }
      } catch (error) {
        console.error('티켓 저장 중 오류:', error);
        Alert.alert('티켓 저장 실패', '서버 통신 중 문제가 발생했습니다.');
      } finally {
        if (isMounted) {
          setIsSaving(false);
        }
      }
    };

    saveTicket();

    return () => {
      isMounted = false;
      if (timer) clearTimeout(timer);
    };
  }, [navigation, ticketData, reviewData, images, setBasePrompt]);

  const handleBackPress = () => {
    navigation.reset({
      index: 0,
      routes: [{ name: 'MainTabs' }],
    });
  };

  return (
    <SafeAreaView style={styles.container} edges={['left', 'right']}>
      <StatusBar barStyle="dark-content" backgroundColor="#F8F9FA" />

      <View style={styles.content}>
        {/* Title */}
        <Text style={styles.title}>새로운 티켓 생성 완료</Text>
        <Text style={styles.subtitle}>하나의 추억을 저장했어요</Text>

        {/* Ticket Card */}
        <View style={styles.ticketCard}>
          <View style={styles.ticketHeader}>
            <Text style={styles.ticketHeaderText}>{ticketData?.title}</Text>
          </View>

          <View style={styles.ticketMain}>
            {ticketImage ? (
              <Image
                source={{ uri: ticketImage }}
                style={styles.ticketImage}
                resizeMode="cover"
                onError={e => {
                  console.error('이미지 로드 실패:', e.nativeEvent.error);
                }}
              />
            ) : (
              <View style={styles.ticketPlaceholder}>
                <Text style={styles.noImageText}>이미지 없음</Text>
              </View>
            )}
          </View>

          <View style={styles.ticketFooter}>
            <Text style={styles.footerSubtext}>
              {ticketData?.venue} •{' '}
              {ticketData?.performedAt
                ? new Date(ticketData.performedAt).toLocaleDateString('ko-KR', {
                    month: 'long',
                    day: 'numeric',
                  })
                : ''}
              {' '}• 8PM
            </Text>
          </View>
        </View>

        {isSaving && (
          <View style={styles.statusWrapper}>
            <ActivityIndicator size="small" color="#B11515" />
            <Text style={styles.statusText}>티켓을 저장하는 중입니다...</Text>
          </View>
        )}

        {saveResult && (
          <Text style={styles.successText}>
            RDS 저장 완료 (티켓 ID: {saveResult.id})
          </Text>
        )}
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F8F9FA' },
  content: { flex: 1, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 20 },
  title: { fontSize: 24, fontWeight: 'bold', color: '#2C3E50', textAlign: 'center', marginBottom: 8 },
  subtitle: { fontSize: 16, color: '#7F8C8D', textAlign: 'center', marginBottom: 40 },
  statusWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 16,
    gap: 8,
  },
  statusText: {
    fontSize: 14,
    color: '#7F8C8D',
  },
  successText: {
    marginTop: 16,
    fontSize: 14,
    color: '#2ECC71',
    fontWeight: '600',
  },

  ticketCard: {
    width: width - 60,
    height: height * 0.6,
    backgroundColor: '#ffffff',
    borderRadius: 20,
    position: 'relative',
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
  },

  ticketHeader: { padding: 20 },
  ticketHeaderText: { fontSize: 18, fontWeight: 'bold', color: '#2C3E50', letterSpacing: 2 },

  ticketMain: { flex: 1, justifyContent: 'center', alignItems: 'center', paddingHorizontal: 20 },
  ticketImage: { width: '100%', height: '100%', borderRadius: 12 },

  ticketPlaceholder: {
    width: '100%',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
  },
  noImageText: { fontSize: 14, color: '#7F8C8D' },

  ticketFooter: { padding: 20, alignItems: 'flex-end' },
  footerSubtext: { fontSize: 12, color: '#2C3E50' },
});

export default TicketCompletePage;
