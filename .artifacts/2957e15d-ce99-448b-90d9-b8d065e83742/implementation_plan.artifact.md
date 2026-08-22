# Implementation Plan - Font Fixes and UI Improvement

The user wants to fix font application issues, remove the system default font option, and move font selection to a dropdown menu in the settings.

## Proposed Changes

### Theme & Fonts

#### [MODIFY] [FontProvider.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/ui/theme/FontProvider.kt)
- Remove `AppFont.Default` from the enum.

#### [MODIFY] [Type.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/ui/theme/Type.kt)
- Update `getTypography` to apply the provided `fontFamily` to all Material 3 typography styles (display, headline, title, body, label).

#### [MODIFY] [Theme.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/ui/theme/Theme.kt)
- Update the default value for `appFont` in `BarghKarTheme` from `AppFont.Default` to `AppFont.Estedad`.

### Data Management

#### [MODIFY] [SettingsManager.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/SettingsManager.kt)
- Update default font references from `AppFont.Default` to `AppFont.Estedad`.

### UI Improvements

#### [MODIFY] [SettingsScreen.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/ui/screens/SettingsScreen.kt)
- Replace the `LazyColumn` font list with an `ExposedDropdownMenuBox` to save space and clean up the UI.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to the settings screen.
- Verify that the font selection is now in a dropdown menu.
- Verify that "Default" is no longer an option.
- Verify that changing the font applies it consistently across all text elements in the app (headers, body text, labels, etc.).
