# Food Delivery Intelligence Platform

A comprehensive, AI-ready food delivery platform built with Kotlin and Jetpack Compose for Android, backed by Firebase and a Python FastAPI backend. The platform supports three distinct user roles: Customers, Vendors, and Administrators, with specialized features for Ethiopian food intelligence.

## Features

### 🍽️ Customer App
- **Browse & Order**: Discover meals with rich metadata (Cuisine Type, Spice Level, Protein Level, Fasting/Vegan friendly).
- **Order Tracking**: Real-time order status tracking with timeline updates.
- **Push Notifications**: Instant alerts when orders are accepted, preparing, ready, or delivered.
- **Seamless Payments**: Integrated with Chapa for secure online transactions.
- **Support System**: Built-in support ticketing system to resolve order issues directly with admins.

### 🏪 Vendor Dashboard
- **Menu Management**: Add and edit meals with detailed dietary and nutritional AI metadata tags.
- **Order Management**: Real-time order reception with an intuitive state-machine for order fulfillment (Accept -> Prepare -> Ready -> Send).
- **Analytics**: Track daily revenue, top-selling items, and overall performance.

### 🛡️ Admin Portal
- **User & Vendor Management**: Approve, reject, or suspend vendor applications. Manage user access.
- **Platform Analytics**: Global view of revenue, total orders, and platform health.
- **Support Resolution**: Handle customer support tickets and provide real-time updates.

## Tech Stack

### Android (Frontend)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture (Presentation, Domain, Data layers) + MVVM
- **Asynchronous**: Kotlin Coroutines & Flow
- **Dependency Injection**: Manual DI
- **Push Notifications**: Firebase Cloud Messaging (FCM)

### Backend & Cloud
- **Database**: Firebase Firestore (Realtime NoSQL)
- **Authentication**: Firebase Auth
- **Payment Gateway Backend**: Python FastAPI (Handles Chapa Webhooks & Security)
- **Notifications**: Firestore triggers / FCM

## Project Structure

```text
app/src/main/java/com/example/food/
├── core/           # Utilities, standard resource wrappers, UI themes
├── data/           # Models, Repositories (Firestore), Remote Services
├── domain/         # Use Cases (Business logic, state machines)
├── ui/             # Jetpack Compose Screens, ViewModels, Navigation
└── MainActivity.kt # Entry point
```

## Setup & Installation

### Android Setup
1. Clone the repository and open it in Android Studio.
2. Connect your project to Firebase:
   - Add your `google-services.json` file to the `app/` directory.
   - Ensure Authentication (Email/Password) and Firestore are enabled in your Firebase console.
3. Build and run the app on an emulator or physical device running Android API 24+.

### Backend Setup (Payment Webhooks)
1. Navigate to the `backend/` directory.
2. Install Python dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Copy `.env.example` to `.env` and fill in your Chapa API keys and Firebase credentials.
4. Run the FastAPI server:
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```

## Key Architectures

### AI-Ready Food Intelligence
The meal entity is structured to support future AI recommendation engines. It includes highly specific metadata such as `CuisineType`, `SpiceLevel`, `ProteinLevel`, and arrays of tags, allowing for complex vector searches and personalized user diets (e.g., Ethiopian Fasting menus).

### Realtime State Synchronization
The app relies heavily on `Flow` and `callbackFlow` to listen to Firestore snapshot changes. This ensures that when a vendor accepts an order, the customer's UI updates instantly without requiring a manual refresh.

### Exhaustive State Machines
Order lifecycles and notification types are strictly governed by exhaustive `when` expressions, ensuring that edge-case states cannot cause UI crashes or silent failures.
