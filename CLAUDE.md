# CLAUDE.md - Salalads Project Context

## Project Overview

**Salalads** is a social game application built with Kotlin Multiplatform targeting Android and iOS. It's a friendship-building game where players cook salads using friends' ingredients, unlocking ingredients by answering questions about their friends.

## Visual Style
- **Smart Minimalism** - Clean, focused UI
- **New Year** - Festive color palette (red, green, gold)
- **Cartoon** - Playful rounded shapes, emoji-based avatars
- **Typography**: Winky Sans (fallback to system sans-serif)

## Technology Stack

### Core Framework
- **Kotlin Multiplatform** (v2.2.0) - Cross-platform code sharing
- **Compose Multiplatform** (v1.9.3) - UI framework for Android and iOS

### Architecture & State Management
- **Decompose** (v3.4.0) - Component library and navigation
- **MVIKotlin** (v4.3.0) - MVI pattern implementation (mandatory)
- **Essenty** (v2.5.0) - Lifecycle and state management

### Persistence & Data
- **Firebase Auth** (v2.4.0 via gitlive) - Anonymous authentication
- **Firebase Firestore** (v2.4.0 via gitlive) - All data storage

### Dependencies & DI
- **Koin** (v4.1.1) - Dependency injection
- **Kotlinx Serialization** (v1.9.0) - JSON serialization

### UI & Utilities
- **Tabler Icons** (compose.icons.TablerIcons) - Icon set
- **Coil** (v3.3.0) - Image loading
- **Kotlinx DateTime** (v0.7.1) - Date/time handling

## Project Structure

```
composeApp/src/commonMain/kotlin/com/alki/salalads/
├── data/
│   ├── models/              # Firebase data models
│   │   ├── StaticIngredient.kt
│   │   ├── StaticSalad.kt
│   │   ├── User.kt
│   │   ├── Friend.kt
│   │   ├── UserIngredient.kt
│   │   ├── ReceivedAnswer.kt
│   │   ├── FriendAnswer.kt
│   │   ├── MyAnswer.kt
│   │   ├── SaladProgress.kt
│   │   └── UserAttempt.kt
│   └── repository/          # Firebase repositories
│       ├── AuthRepository.kt
│       ├── StaticDataRepository.kt
│       └── UserRepository.kt
├── di/
│   └── AppModule.kt         # Koin DI configuration
├── navigation/
│   ├── RootComponent.kt     # Main navigation component
│   └── RootContent.kt       # Root Composable
├── features/
│   ├── splash/              # Loading screen
│   ├── onboarding/          # Name, avatar, salads, ingredients setup
│   ├── home/                # Main screen with salads list
│   ├── salad/               # Salad detail with ingredients
│   ├── ingredient/          # Ingredient with friends list
│   ├── question/            # Answer friend's question
│   ├── inventory/           # "My Storage" - user's ingredients
│   ├── answers/             # View friend answers to your questions
│   └── addfriend/           # Add friend by ID
└── ui/
    ├── theme/
    │   ├── Color.kt         # Color palette
    │   └── Theme.kt         # Material3 theme
    └── components/          # Reusable UI components
        ├── SalaladsButton.kt
        ├── SalaladsCard.kt
        ├── SalaladsTextField.kt
        └── EmojiPicker.kt
```

## Firebase Collections Schema

### Static Data (read-only)
- `static_ingredients/{ingredientId}` - Available ingredients
- `static_salads/{saladId}` - Available salads with required ingredients

### User Data
- `users/{uid}` - User profile (username, avatar, shareId)
- `users/{uid}/friends/{friendId}` - Friend list
- `users/{uid}/inventory/{ingredientId}` - User's ingredients with questions
- `users/{uid}/inventory/{ingredientId}/received_answers/{id}` - Answers from friends
- `users/{uid}/friends_answers/{id}` - All answer attempts from friends (question, answer, from_user)
- `users/{uid}/my_answers/{id}` - All my answer attempts (user_name, question, answer)
- `users/{uid}/salads_progress/{saladId}` - Salad completion progress
- `users/{uid}/attempts/{friendId_ingredientId}` - Failed answer attempts

## Game Rules

1. **Onboarding**: Enter name → Select avatar emoji → Choose favorite salads → Add ingredients with questions
2. **Ingredients**: Can use 1 own ingredient per salad, others must come from friends
3. **Friends**: Find friends by shareId, mutual friendship on add
4. **Answers**: 2 attempts per ingredient, 30-min block after failure
5. **Ingredients adding**: 6-hour cooldown between adding new ingredients

## Architecture Guidelines

### Required Patterns
- **MVI Pattern**: Use MVI with Decompose and Essenty
- **Component Architecture**: Decompose components for navigation
- **Repository Pattern**: All Firebase access through repositories

### Required Imports
```kotlin
import androidx.compose.runtime.collectAsState
import kotlin.time.Clock
import compose.icons.TablerIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.*
```

## Development Guidelines

- **Minimal Diffs**: Make smallest possible changes
- **No Formatting**: Avoid formatting changes unless necessary
- **Existing Libraries**: Use only libraries already in `libs.versions.toml`
- **Russian Comments**: Add comments in Russian for complex code logic
- **Firebase Only**: All data persistence through Firebase (no Room for user data)

## Entry Points

### Android
- `MainActivity.kt` → `RootComponent` → `RootContent`

### iOS
- `MainViewController.kt` → `RootComponent` → `RootContent`

## Screen Flow

```
Splash → [User exists?]
  ├─ No → Onboarding (Profile → Salads → Ingredients) → Home
  └─ Yes → Home → [Salad] → [Ingredient] → [Question]
                ├─ Inventory → Answers
                └─ AddFriend
```

## Color Palette (SalaladsColors)

- **ChristmasRed** `#E53935` - Primary actions
- **ChristmasGreen** `#43A047` - Success, secondary actions
- **ChristmasGold** `#FFD54F` - Highlights, selected items
- **SnowWhite** `#FAFAFA` - Backgrounds
- **Soft variants** for cards and containers

## Notes for Claude

1. **Language**: Code comments should be in Russian for complex logic
2. **Architecture**: Strictly follow MVI + Decompose + Essenty pattern
3. **Dependencies**: Only use libraries already declared in `libs.versions.toml`
4. **Firebase**: Use gitlive Firebase libraries for cross-platform support
5. **Imports**: Follow the specific import patterns listed above
6. **Game Context**: This is a social friendship game about cooking salads together