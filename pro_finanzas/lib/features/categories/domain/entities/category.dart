// Re-export the shared Category entity from the transactions feature.
// Categories are first-class citizens of the transactions domain (you
// categorize transactions), so defining them there and importing from
// here keeps the language consistent and avoids entity duplication.
//
// Consumers of this feature can write
//   import '.../categories/domain/entities/category.dart';
// and get the `Category` class directly. If that import ever breaks
// (e.g. the entity moves to a shared `core` package), the fix is here.
export '../../../transactions/domain/entities/category.dart';
