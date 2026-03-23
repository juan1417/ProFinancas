class User {
  const User({
    required this.id,
    required this.email,
    required this.username,
    this.firstName = '',
    this.lastName = '',
  });

  final String id;
  final String email;
  final String username;
  final String firstName;
  final String lastName;

  String get fullName => '$firstName $lastName'.trim();
}
