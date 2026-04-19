## 1. Domain and import pipeline

- [x] 1.1 Add shared domain models for imported specifications, requirement cards, scenarios, review modes, and aggregated statistics
- [x] 1.2 Implement OpenSpec markdown parsing for `spec.md` files into requirement cards with grouped scenarios
- [x] 1.3 Add import orchestration that scans a selected root folder, loads valid specification subfolders, and activates the latest collection
- [x] 1.4 Bundle the demo specification asset and route it through the same import pipeline used for custom folders

## 2. Persistence and review state

- [x] 2.1 Add local persistence for imported collection metadata, swipe history entries, and latest card status lookup
- [x] 2.2 Implement stable card identity generation so unchanged requirements keep their review history across re-imports
- [x] 2.3 Build review session state for `unreviewed`, `approved-only`, and `rejected-only` deck modes

## 3. Mobile UI and interaction flow

- [x] 3.1 Create the import/empty-state experience with demo entry and custom folder selection
- [x] 3.2 Implement the Soft Consumer Minimalism review card UI showing spec name, requirement title, scenario blocks, and requirement description
- [x] 3.3 Add left/right swipe gestures, decision feedback, and automatic progression through eligible cards
- [x] 3.4 Build the completed-review screen with replay actions for rejected-only and approved-only passes

## 4. Analytics and platform integration

- [x] 4.1 Implement aggregate and per-specification statistics including reviewed counts, completion rate, and rejection percentage
- [x] 4.2 Add sorting or highlighting so the highest-rejection specifications are easy to identify
- [ ] 4.3 Integrate platform-specific folder picker and file access adapters for Android and iOS
- [x] 4.4 Add tests for markdown parsing, card identity stability, replay filtering, and statistics calculations
