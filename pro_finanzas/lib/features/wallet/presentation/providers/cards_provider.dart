import 'package:flutter/foundation.dart';
import '../../../../core/services/card_storage_service.dart';

/// State container for the wallet's saved cards.
///
/// Loads from [CardStorageService] on construction, holds the list in
/// memory for snappy UI updates, and writes through to storage on every
/// mutation. The list is keyed by `card.id` (a string timestamp) so the
/// UI can use `ValueKey(card.id)` for stable animations.
class CardsProvider extends ChangeNotifier {
  CardsProvider({CardStorageService? storage})
      : _storage = storage ?? CardStorageService.instance;

  final CardStorageService _storage;

  List<StoredCard> _cards = const [];
  bool isLoading = false;
  String? error;

  List<StoredCard> get cards => _cards;

  Future<void> load() async {
    _setLoading(true);
    try {
      _cards = await _storage.readAll();
      error = null;
    } catch (e) {
      error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  /// Add a card and persist. The caller is responsible for building the
  /// `StoredCard` (so this provider stays a dumb state container).
  Future<bool> addCard(StoredCard card) async {
    final next = [..._cards, card];
    return _persist(next);
  }

  /// Remove a card by id. Returns true if the card was present and removed.
  Future<bool> removeCard(String id) async {
    final next = _cards.where((c) => c.id != id).toList(growable: false);
    if (next.length == _cards.length) return false;
    return _persist(next);
  }

  Future<bool> _persist(List<StoredCard> next) async {
    try {
      await _storage.writeAll(next);
      _cards = next;
      error = null;
      notifyListeners();
      return true;
    } catch (e) {
      error = e.toString();
      notifyListeners();
      return false;
    }
  }

  void _setLoading(bool value) {
    isLoading = value;
    notifyListeners();
  }
}
