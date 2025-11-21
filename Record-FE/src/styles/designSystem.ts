export const Colors = {
  // Primary Colors
  primary: '#B11515',           // App accent color (red)
  primaryLight: '#D32F2F',      // Lighter variant
  primaryDark: '#8B0000',       // Darker variant
  
  // System Colors
  systemBackground: '#FFFFFF',   // Pure white background
  secondarySystemBackground: '#F2F2F7',  // Light gray background
  tertiarySystemBackground: '#FFFFFF',   // White for cards
  
  // Basic Colors
  white: '#FFFFFF',
  black: '#000000',
  
  // Text Colors
  label: '#000000',             // Primary text (black)
  secondaryLabel: '#3C3C43',    // Secondary text (60% opacity)
  tertiaryLabel: '#3C3C4399',   // Tertiary text (30% opacity)
  quaternaryLabel: '#3C3C432E', // Quaternary text (18% opacity)
  placeholderText: '#3C3C434D', // Placeholder text (30% opacity)
  
  // Separator Colors
  separator: '#3C3C434A',       // Light separator (29% opacity)
  opaqueSeparator: '#C6C6C8',   // Opaque separator
  
  // Fill Colors
  systemFill: '#78788033',      // Primary fill (20% opacity)
  secondarySystemFill: '#78788028', // Secondary fill (16% opacity)
  tertiarySystemFill: '#7676801E',  // Tertiary fill (12% opacity)
  quaternarySystemFill: '#74748014', // Quaternary fill (8% opacity)
  
  // System Gray Colors
  systemGray: '#8E8E93',
  systemGray2: '#AEAEB2',
  systemGray3: '#C7C7CC',
  systemGray4: '#D1D1D6',
  systemGray5: '#E5E5EA',
  systemGray6: '#F2F2F7',
  
  // Semantic Colors
  systemBlue: '#007AFF',
  systemGreen: '#34C759',
  systemIndigo: '#5856D6',
  systemOrange: '#FF9500',
  systemPink: '#FF2D92',
  systemPurple: '#AF52DE',
  systemRed: '#FF3B30',
  systemTeal: '#30B0C7',
  systemYellow: '#FFCC00',
  
  // Status Colors
  success: '#34C759',
  warning: '#FF9500',
  error: '#FF3B30',
  info: '#007AFF',
};

export const Typography = {
  // Large Title
  largeTitle: {
    fontSize: 34,
    fontWeight: '400' as const,
    lineHeight: 41,
  },
  
  // Title Styles
  title1: {
    fontSize: 28,
    fontWeight: '400' as const,
    lineHeight: 34,
  },
  title2: {
    fontSize: 22,
    fontWeight: '400' as const,
    lineHeight: 28,
  },
  title3: {
    fontSize: 20,
    fontWeight: '400' as const,
    lineHeight: 25,
  },
  
  // Headline
  headline: {
    fontSize: 18,
    fontWeight: '600' as const,
    lineHeight: 22,
  },
  
  // Body Styles
  body: {
    fontSize: 17,
    fontWeight: '400' as const,
    lineHeight: 22,
  },
  callout: {
    fontSize: 16,
    fontWeight: '400' as const,
    lineHeight: 21,
  },
  subheadline: {
    fontSize: 15,
    fontWeight: '400' as const,
    lineHeight: 20,
  },
  footnote: {
    fontSize: 13,
    fontWeight: '400' as const,
    lineHeight: 18,
  },
  
  // Caption Styles
  caption1: {
    fontSize: 12,
    fontWeight: '400' as const,
    lineHeight: 16,
  },
  caption2: {
    fontSize: 11,
    fontWeight: '400' as const,
    lineHeight: 13,
  },
};

export const Spacing = {
  xs: 4,    // 4pt
  sm: 8,    // 8pt
  md: 12,   // 12pt
  lg: 16,   // 16pt
  xl: 20,   // 20pt
  xxl: 24,  // 24pt
  xxxl: 32, // 32pt
  
  cardPadding: 16,
  screenPadding: 24,
  sectionSpacing: 24,
  buttonPadding: 16,
  inputPadding: 16,
};

export const BorderRadius = {
  xs: 4,
  sm: 6,
  md: 8,
  lg: 12,
  xl: 16,
  xxl: 20,
  round: 50, // For circular elements
};

export const Shadows = {
  // iOS-style shadows
  small: {
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
  },
  medium: {
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
  },
  large: {
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.12,
    shadowRadius: 8,
  },
  card: {
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  button: {
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
};

export const Layout = {
  // Safe area and navigation
  tabBarHeight: 83,
  navigationBarHeight: 44,
  statusBarHeight: 44,
  
  // Common dimensions
  buttonHeight: 50,
  inputHeight: 50,
  cardMinHeight: 60,
  
  // Breakpoints (if needed for responsive design)
  screenSizes: {
    small: 320,
    medium: 375,
    large: 414,
    extraLarge: 768,
  },
};

// Common component styles
export const ComponentStyles = {
  // Button styles
  primaryButton: {
    backgroundColor: Colors.primary,
    borderRadius: BorderRadius.lg,
    paddingVertical: Spacing.lg,
    paddingHorizontal: Spacing.xl,
    ...Shadows.button,
  },
  
  secondaryButton: {
    backgroundColor: Colors.secondarySystemBackground,
    borderRadius: BorderRadius.lg,
    paddingVertical: Spacing.lg,
    paddingHorizontal: Spacing.xl,
    ...Shadows.small,
  },
  
  // Card styles
  card: {
    backgroundColor: Colors.systemBackground,
    borderRadius: BorderRadius.lg,
    padding: Spacing.cardPadding,
    ...Shadows.card,
  },
  
  // Input styles
  input: {
    backgroundColor: Colors.systemBackground,
    borderWidth: 1,
    borderColor: Colors.systemGray5,
    borderRadius: BorderRadius.md,
    paddingVertical: Spacing.inputPadding,
    paddingHorizontal: Spacing.inputPadding,
    ...Typography.body,
    color: Colors.label,
    ...Shadows.small,
  },
  
  // Header styles
  header: {
    backgroundColor: Colors.systemBackground,
    paddingHorizontal: Spacing.screenPadding,
    paddingVertical: Spacing.lg,
    ...Shadows.small,
  },
};

// Helper functions
export const getTextColor = (variant: 'primary' | 'secondary' | 'tertiary' | 'quaternary' = 'primary') => {
  switch (variant) {
    case 'primary':
      return Colors.label;
    case 'secondary':
      return Colors.secondaryLabel;
    case 'tertiary':
      return Colors.tertiaryLabel;
    case 'quaternary':
      return Colors.quaternaryLabel;
    default:
      return Colors.label;
  }
};

export const getBackgroundColor = (variant: 'primary' | 'secondary' | 'tertiary' = 'primary') => {
  switch (variant) {
    case 'primary':
      return Colors.systemBackground;
    case 'secondary':
      return Colors.secondarySystemBackground;
    case 'tertiary':
      return Colors.tertiarySystemBackground;
    default:
      return Colors.systemBackground;
  }
};
