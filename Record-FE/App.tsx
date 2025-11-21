/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */
import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Text, View, Image } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNavigation } from '@react-navigation/native';


// Auth pages
import LoginPage from './src/pages/auth/LoginPage';
import SignupPage from './src/pages/auth/SignupPage';
import FindIdPage from './src/pages/auth/recovery/FindIdPage';
import FindPasswordPage from './src/pages/auth/recovery/FindPasswordPage';
// Home pages
import MainPage from './src/pages/home/MainPage';

// Add ticket pages
import AddTicketPage from './src/pages/add-ticket/AddTicketPage';
import OCRPage from './src/pages/add-ticket/OCRPage';
import ImageOptions from './src/pages/add-ticket/ImageOptions';
import AddReviewPage from './src/pages/add-ticket/AddReviewPage';
import AIImageResults from './src/pages/add-ticket/AIImageResults';
import TicketCompletePage from './src/pages/add-ticket/TicketCompletePage';

// Calendar pages
import CalendarScreen from './src/pages/calendar/CalendarScreen';

// My page related
import MyPage from './src/pages/my-page/MyPage';
import FriendsListPage from './src/pages/my-page/FriendsListPage';
import SentRequestsPage from './src/pages/my-page/SentRequestsPage';
import AddFriendPage from './src/pages/my-page/AddFriendPage';
import SettingsPage from './src/pages/my-page/SettingsPage';
import PersonalInfoEditPage from './src/pages/my-page/PersonalInfoEditPage';
import HistoryPage from './src/pages/my-page/HistoryPage';

// Components
import FriendProfilePage from './src/components/FriendProfilePage';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

// Wrapper component for Add Tickets tab that opens modal
function AddTicketTabWrapper({ navigation }: { navigation: any }) {
  useEffect(() => {
    const unsubscribe = navigation.addListener('tabPress', (e: any) => {
      e.preventDefault();
      navigation.navigate('AddTicket', {
        isFirstTicket: false,
        fromAddButton: true
      });
    });

    return unsubscribe;
  }, [navigation]);

  // Return empty view since this tab should only trigger modal
  return <View style={{ flex: 1, backgroundColor: '#FFFFFF' }} />;
}

function MainTabs() {
  const insets = useSafeAreaInsets();

  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        
        tabBarStyle: {
          position: 'absolute',
          backgroundColor: 'rgba(35, 35, 35, 1)',
          paddingBottom: insets.bottom,
          paddingTop: 8,
          height: 60 + insets.bottom,
          borderTopLeftRadius: 20,
          borderTopRightRadius: 20,
          borderTopWidth: 0,
          overflow: 'hidden',
        },
        tabBarActiveTintColor: '#ffffffff',
        tabBarInactiveTintColor: '#7F8C8D',
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: '600',
        },
      }}
    >
      <Tab.Screen
        name="Home"
        component={MainPage}
        options={{
          tabBarLabel: '홈',
          tabBarIcon: ({ color }: { color: string }) => (
            <View style={{ alignItems: 'center', justifyContent: 'center' }}>
              <Image
                source={require('./src/assets/ticket_icon.png')}
                style={{ width: 24, height: 24, tintColor: color }}
                resizeMode="contain"
              />
            </View>
          ),
        }}
      />
      <Tab.Screen
        name="Add Tickets"
        component={AddTicketTabWrapper}
        options={{
          tabBarLabel: '티켓 추가',
          tabBarIcon: ({ color }: { color: string }) => (
            <View style={{ alignItems: 'center', justifyContent: 'center' }}>
              <Image
                source={require('./src/assets/add_icon.png')}
                style={{ width: 24, height: 24, tintColor: color }}
                resizeMode="contain"
              />
            </View>
          ),
        }}
      />
      <Tab.Screen
        name="CalendarScreen"
        component={CalendarScreen}
        options={{
          tabBarLabel: '캘린더',
          tabBarIcon: ({ color }: { color: string }) => (
            <View style={{ alignItems: 'center', justifyContent: 'center' }}>
              <Image
                source={require('./src/assets/calendar_icon.png')}
                style={{ width: 24, height: 24, tintColor: color }}
                resizeMode="contain"
              />
            </View>
          ),
        }}
      />
      <Tab.Screen
        name="MyTickets"
        component={MyPage}
        options={{
          tabBarLabel: '내 티켓',
          tabBarIcon: ({ color }: { color: string }) => (
            <View style={{ alignItems: 'center', justifyContent: 'center' }}>
              <Image
                source={require('./src/assets/user_icon.png')}
                style={{ width: 24, height: 24, tintColor: color }}
                resizeMode="contain"
              />
            </View>
          ),
        }}
      />
    </Tab.Navigator>
  );
}

function App(): React.JSX.Element {
  return (
    <NavigationContainer>
      <Stack.Navigator
        screenOptions={{
          headerShown: false,
        }}
        initialRouteName="Login"
      >
        <Stack.Screen 
          name="Login" 
          component={LoginPage}
          options={{
            headerShown: false,
          }}
        />
        <Stack.Screen 
          name="Signup" 
          component={SignupPage}
          options={{
            headerShown: false,
          }}
        />
                <Stack.Screen 
          name="FindId" 
          component={FindIdPage}
          options={{
            headerShown: false,
          }}
        />
                <Stack.Screen 
          name="FindPassword" 
          component={FindPasswordPage}
          options={{
            headerShown: false,
          }}
        />
        <Stack.Screen name="MainTabs" component={MainTabs} />
        <Stack.Screen
          name="AddTicket"
          component={AddTicketPage}
          options={{
            presentation: 'modal',
          }}
        />
        <Stack.Screen
          name="OCR"
          component={OCRPage}
          options={{
            presentation: 'modal',
          }}
        />
        <Stack.Screen
          name="AddReview"
          component={AddReviewPage as any}
          options={{
            presentation: 'modal',
          }}
        />
        <Stack.Screen
          name="ImageOptions"
          component={ImageOptions}
          options={{
            presentation: 'modal',
          }}
        />
        <Stack.Screen
          name="AIImageResults"
          component={AIImageResults}
          options={{
            presentation: 'modal',
          }}
        />
        <Stack.Screen
          name="TicketComplete"
          component={TicketCompletePage}
          options={{
            presentation: 'fullScreenModal',
            gestureEnabled: false,
          }}
        />
        <Stack.Screen
          name="FriendsList"
          component={FriendsListPage}
          options={{
            presentation: 'modal',
          }}
        />
        <Stack.Screen
          name="SentRequests"
          component={SentRequestsPage}
          options={{
            presentation: 'modal',
          }}
        />
        <Stack.Screen
          name="AddFriend"
          component={AddFriendPage}
          options={{
            presentation: 'modal',
          }}
        />
        <Stack.Screen
          name="FriendProfile"
          component={FriendProfilePage as any}
          options={{
            presentation: 'card',
            headerShown: false,
          }}
        />
        <Stack.Screen
          name="Settings"
          component={SettingsPage}
          options={{
            presentation: 'modal',
          }}
        />
        <Stack.Screen
          name="PersonalInfoEdit"
          component={PersonalInfoEditPage}
          options={{
            presentation: 'modal',
          }}
        />
        <Stack.Screen
          name="History"
          component={HistoryPage}
          options={{
            presentation: 'modal',
          }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default App;
