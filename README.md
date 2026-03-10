# Fund Transaction App

A modern Android fintech application built with Kotlin that enables users to manage their financial transactions with ease. Track deposits, withdrawals, and monitor your balance in real-time.

> **Note**: The repository name uses double hyphens (`Fund--Transaction-app`) as per the original naming convention.

## Overview

Fund Transaction App is a comprehensive financial management application that allows users to:
- Create and manage an account securely
- Add deposits and withdrawals
- View transaction history
- Monitor real-time balance updates
- Track recent transactions on dashboard

The app leverages Supabase for backend services, providing real-time database synchronization and secure authentication.

## Key Features

### User Authentication
- **Secure Registration**: Create an account with email and password
- **Login System**: Secure authentication using Supabase Auth
- **Session Management**: Persistent login sessions with secure token management

### Transaction Management
- **Add Transactions**: Easily add deposits or withdrawals
- **Real-time Updates**: Automatic balance recalculation
- **Transaction History**: View complete transaction history
- **Transaction Types**: Support for deposits and withdrawals

### Dashboard
- **Balance Overview**: View current balance at a glance
- **Recent Transactions**: Quick access to 5 most recent transactions
- **User Profile**: Display user information
- **Real-time Sync**: Live updates using Supabase Realtime

### User Interface
- **Material Design**: Modern UI following Material Design principles
- **Responsive Layout**: Optimized for various screen sizes
- **Intuitive Navigation**: Simple and clean user experience
- **Splash Screen**: Professional app launch experience

## Technology Stack

### Core Technologies
- **Language**: Kotlin
- **Platform**: Android (minSdk 24, targetSdk 35)
- **Build Tool**: Gradle with Kotlin DSL

### Android Components
- **AndroidX Core KTX**: Kotlin extensions for Android
- **AppCompat**: Backward compatibility support
- **Material Components**: Material Design UI components
- **ConstraintLayout**: Flexible layout system
- **RecyclerView & CardView**: List and card displays
- **ViewBinding**: Type-safe view access

### Architecture & Lifecycle
- **Lifecycle Components**: ViewModel and LiveData
- **Coroutines**: Asynchronous programming
- **Activity KTX**: Activity extensions

### Backend & Network
- **Supabase**: Backend-as-a-Service platform
  - **GoTrue**: Authentication
  - **PostgREST**: Database API
  - **Realtime**: WebSocket-based real-time updates
- **Ktor Client**: HTTP client with OkHttp engine
- **Kotlinx Serialization**: JSON serialization/deserialization

### Testing
- **JUnit**: Unit testing framework
- **Espresso**: UI testing
- **AndroidX Test**: Android testing utilities

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
- **Android Studio**: Arctic Fox or newer (recommended: latest stable version)
- **JDK**: Java Development Kit 8 or higher
- **Android SDK**: API Level 24 or higher
- **Gradle**: 7.0 or higher (usually bundled with Android Studio)
- **Git**: For version control

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/Mwamba8701/Fund--Transaction-app.git
cd Fund--Transaction-app
```

### 2. Open in Android Studio
1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned repository folder
4. Click "OK" to open the project

### 3. Sync Gradle
Android Studio should automatically start syncing Gradle. If not:
1. Click on "File" → "Sync Project with Gradle Files"
2. Wait for the sync to complete

### 4. Supabase Configuration
The project comes pre-configured with Supabase credentials in `build.gradle.kts`:
```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://bhbszidhnpgputfrfysm.supabase.co\"")
buildConfigField("String", "SUPABASE_KEY", "\"...\"")
```

**⚠️ IMPORTANT SECURITY NOTE**: The credentials shown in the code are for demonstration purposes only. For production use:
1. **NEVER commit API keys directly** to version control
2. Create your own Supabase project at [supabase.com](https://supabase.com)
3. Store credentials in `local.properties` (which is git-ignored) or use environment variables
4. Replace the hardcoded values with references to your secure configuration
5. Set up the required database tables (see Database Schema section)

**Best Practice**: Use `local.properties`:
```properties
# In local.properties (git-ignored)
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_key
```

### 5. Build and Run
1. Connect an Android device or start an emulator
2. Click the "Run" button (▶️) in Android Studio
3. Select your target device
4. The app will build and install automatically

## 📱 Usage

### Getting Started

1. **Launch the App**
   - Open the app to see the splash screen
   - You'll be automatically redirected to the login screen

2. **Create an Account**
   - Click "Register" on the login screen
   - Enter your full name, email, and password
   - Click "Register" to create your account
   - Check your email for verification (if required)

3. **Login**
   - Enter your registered email and password
   - Click "Login" to access your dashboard

### Managing Transactions

1. **Add a Transaction**
   - From the dashboard, click "Add Transaction"
   - Enter the transaction amount
   - Select transaction type (Deposit or Withdraw)
   - Click "Save" to record the transaction

2. **View Dashboard**
   - See your current balance at the top
   - View your 5 most recent transactions
   - Balance updates automatically in real-time

3. **View Transaction History**
   - Click "Transaction History" from the dashboard
   - Browse through all your transactions
   - Transactions are sorted by date (newest first)

4. **Logout**
   - Click the "Logout" button to sign out securely

## 📁 Project Structure

```
Fund--Transaction-app/
├── src/
│   ├── main/
│   │   ├── java/com/example/fintechapp/
│   │   │   ├── MainActivity.kt                 # Splash screen activity
│   │   │   ├── data/
│   │   │   │   ├── model/
│   │   │   │   │   ├── Profile.kt             # User profile data model
│   │   │   │   │   └── TransactionDto.kt      # Transaction data model
│   │   │   │   └── network/
│   │   │   │       └── SupabaseClient.kt      # Supabase client configuration
│   │   │   └── ui/
│   │   │       ├── DashboardActivity.kt        # Main dashboard
│   │   │       ├── auth/
│   │   │       │   ├── LoginActivity.kt       # Login screen
│   │   │       │   └── RegisterActivity.kt    # Registration screen
│   │   │       └── transaction/
│   │   │           ├── AddTransactionActivity.kt    # Add transaction
│   │   │           ├── TransactionAdapter.kt        # RecyclerView adapter
│   │   │           └── TransactionsActivity.kt      # Transaction history
│   │   ├── res/
│   │   │   ├── layout/                        # XML layout files
│   │   │   ├── drawable/                      # Drawable resources
│   │   │   ├── values/                        # Strings, colors, themes
│   │   │   └── mipmap/                        # App icons
│   │   └── AndroidManifest.xml
│   ├── test/                                  # Unit tests
│   └── androidTest/                           # Instrumented tests
├── build.gradle.kts                           # App-level build configuration
├── settings.gradle.kts                        # Project settings
└── README.md                                  # This file
```

## Database Schema

The app uses Supabase PostgreSQL database with the following tables:

### Profiles Table
```sql
CREATE TABLE profiles (
  user_id UUID PRIMARY KEY REFERENCES auth.users,
  full_name TEXT NOT NULL,
  email TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### Transactions Table
```sql
CREATE TABLE transactions (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES auth.users,
  type TEXT NOT NULL CHECK (type IN ('deposit', 'withdraw')),
  amount NUMERIC NOT NULL CHECK (amount > 0),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

## Security Features

- **Secure Authentication**: Email/password authentication via Supabase Auth
- **Row Level Security**: Database access controlled by user authentication
- **API Key Management**: Secure storage of API credentials
- **Session Management**: Automatic session handling and token refresh
- **Input Validation**: Client-side validation for all user inputs

## Contributing

Contributions are welcome! Here's how you can help:

1. **Fork the Repository**
   - Click the "Fork" button on GitHub to create your own copy
   - Clone your forked repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/Fund--Transaction-app.git
   ```

2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make Your Changes**
   - Write clean, documented code
   - Follow Kotlin coding conventions
   - Add tests if applicable

4. **Commit Your Changes**
   ```bash
   git commit -m "Add: description of your changes"
   ```

5. **Push to Your Fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**
   - Go to the original repository
   - Click "New Pull Request"
   - Describe your changes clearly

### Code Style Guidelines
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Comment complex logic
- Keep functions small and focused
- Write unit tests for new features

## License

This project is available for educational and personal use. For commercial use, please contact the repository owner.

## Authors

- **Mwamba8701** - *Initial work* - [GitHub Profile](https://github.com/Mwamba8701)

## Acknowledgments

- **Supabase** - For providing an excellent backend-as-a-service platform
- **Material Design** - For UI/UX guidelines
- **Kotlin Community** - For excellent language support and libraries
- **Android Developers** - For comprehensive documentation

## Support

If you encounter any issues or have questions:
1. Check existing [Issues](https://github.com/Mwamba8701/Fund--Transaction-app/issues)
2. Create a new issue with detailed description
3. Contact the maintainer through GitHub

## Future Enhancements

Planned features for future releases:
- [ ] Transaction categories and tags
- [ ] Budget planning and tracking
- [ ] Expense analytics and charts
- [ ] Export transaction history (CSV/PDF)
- [ ] Dark mode support
- [ ] Biometric authentication
- [ ] Multi-currency support
- [ ] Recurring transactions
- [ ] Push notifications for transactions
- [ ] Data backup and restore

## Screenshots

*Coming soon - Screenshots of the app will be added here*

---

**Made with using Kotlin and Android**
