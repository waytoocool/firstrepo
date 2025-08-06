# SMS Classifier - Lightweight SMS Classification App MVP

A modern Android app that automatically classifies SMS messages into categories (OTP, Spam, Promotional, Transactional, Personal) using rule-based classification and provides smart notifications.

## 🎯 MVP Features

### ✅ Completed Features

- **SMS Reception & Classification**: Automatically receives and classifies incoming SMS messages
- **5 Category Classification**: OTP, Spam, Promotional, Transactional, Personal
- **Rule-Based Classifier**: Uses regex patterns and keyword matching for accurate classification
- **Category-Based Notifications**: Different notification channels with appropriate priorities
- **Modern UI**: Jetpack Compose UI with Material Design 3
- **Tabbed Interface**: Browse messages by category with unread count badges
- **OTP Actions**: Copy and share OTP codes directly from notifications and app
- **Permission Management**: Comprehensive permission handling for SMS and notifications

### 🔧 Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Repository pattern
- **Data Storage**: In-memory (ready for Room database expansion)
- **Classification**: Rule-based with regex patterns
- **Notifications**: Android NotificationCompat with channels

## 📁 Project Structure

```
app/
├── src/main/java/com/smsclassifier/app/
│   ├── MainActivity.kt                 # Main activity
│   ├── classifier/
│   │   └── SmsClassifier.kt           # Rule-based SMS classifier
│   ├── model/
│   │   └── SmsMessage.kt              # SMS message data model
│   ├── receiver/
│   │   └── SmsReceiver.kt             # SMS broadcast receiver
│   ├── repository/
│   │   └── SmsRepository.kt           # In-memory message storage
│   ├── service/
│   │   ├── NotificationService.kt     # Notification handling
│   │   └── NotificationActionReceiver.kt # Notification actions
│   ├── ui/
│   │   ├── component/
│   │   │   └── MessageList.kt         # Message list component
│   │   ├── screen/
│   │   │   ├── MainScreen.kt          # Main app screen
│   │   │   └── PermissionScreen.kt    # Permission request screen
│   │   └── theme/                     # App theming
│   └── viewmodel/
│       └── MainViewModel.kt           # Main view model
└── src/main/res/                      # Resources (layouts, strings, etc.)
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 26 (Android 8.0) or higher
- Kotlin 1.9.10 or later

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd sms-classifier
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

### Required Permissions

The app requires the following permissions:
- `RECEIVE_SMS`: To intercept incoming SMS messages
- `READ_SMS`: To access SMS content for classification
- `POST_NOTIFICATIONS`: To show category-based notifications (Android 13+)

### Setup Notes

**Missing Icon Resources**: You'll need to add actual PNG icon files to the following directories:
- `app/src/main/res/mipmap-hdpi/ic_launcher.png` (72x72px)
- `app/src/main/res/mipmap-mdpi/ic_launcher.png` (48x48px)
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png` (96x96px)
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png` (144x144px)
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` (192x192px)

And the corresponding `ic_launcher_round.png` files for each density.

## 📱 How It Works

### SMS Classification

The app uses a sophisticated rule-based classifier with:

1. **OTP Detection**: Recognizes numeric codes, "OTP" keywords, verification patterns
2. **Spam Detection**: Identifies typical spam phrases like "free", "winner", "claim"
3. **Promotional Classification**: Detects offers, discounts, sales, deals
4. **Transactional Recognition**: Bank alerts, payment confirmations, account notifications
5. **Personal Messages**: Default category for messages from known contacts

### Notification System

- **5 Notification Channels**: Each category has its own channel with appropriate priority
- **Smart Actions**: OTP messages include copy and share actions
- **Priority Levels**: OTP and Transactional (High), Personal (Default), Promotional (Low), Spam (Min)

### User Interface

- **Category Tabs**: Quick access to each message type
- **Unread Badges**: Visual indicators for new messages
- **Message Cards**: Clean design with category color coding
- **OTP Actions**: One-tap copy and share for OTP codes

## 🔬 Testing

### Sample Messages

The app includes sample messages for testing:
- OTP from ICICI Bank
- Spam promotional message
- Amazon delivery confirmation
- Flipkart promotional offer
- Personal message from contact

### Manual Testing

1. **SMS Simulation**: Use Android Emulator's SMS feature or ADB commands
2. **Permission Testing**: Revoke and grant permissions to test permission flow
3. **Notification Testing**: Verify notifications appear with correct categories
4. **OTP Actions**: Test copy and share functionality

## 🔮 Future Enhancements (Post-MVP)

### Planned Features

1. **Machine Learning Classification**
   - Replace rule-based system with ML model
   - Continuous learning from user corrections

2. **Cloud Sync & Backup**
   - Firebase integration for message backup
   - Cross-device synchronization

3. **Advanced Features**
   - Message search and filtering
   - Auto-delete spam after X days
   - Smart auto-actions (auto-copy OTP)

4. **Analytics & Insights**
   - Visual charts of message patterns
   - Spam detection statistics
   - OTP usage tracking

5. **UI/UX Improvements**
   - Dark mode support
   - Custom themes
   - Advanced settings

### Technical Improvements

1. **Database Integration**: Replace in-memory storage with Room
2. **Background Processing**: Optimize for battery life
3. **Performance**: Implement message pagination
4. **Security**: Enhanced OTP security features

## 🛠️ Development

### Key Components

1. **SmsClassifier**: Heart of the classification logic
2. **SmsRepository**: Manages message storage and retrieval
3. **NotificationService**: Handles smart notifications
4. **MainViewModel**: Coordinates UI state and data

### Adding New Classification Rules

To add new patterns for classification:

1. Open `SmsClassifier.kt`
2. Add new regex patterns to the appropriate category list
3. Adjust scoring logic if needed
4. Test with sample messages

### Customizing UI

- Modify colors in `Color.kt`
- Adjust typography in `Type.kt`
- Update category icons in `MainScreen.kt`

## 📄 License

This project is part of an MVP development plan for a lightweight SMS classifier application.

## 🤝 Contributing

This is an MVP implementation. Future contributions will focus on:
- ML model integration
- Performance optimizations
- Additional classification categories
- Enhanced user experience features

---

**Note**: This is a Minimum Viable Product (MVP) focused on core functionality. The codebase is designed to be easily extensible for future enhancements like ML integration, cloud sync, and advanced UI features.
