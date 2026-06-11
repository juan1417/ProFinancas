import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/widgets/app_card.dart';
import '../../../../core/widgets/pro_app_bar.dart';
import '../../domain/entities/user.dart';
import '../providers/auth_provider.dart';

/// Account & security screen. Reachable from the avatar icon in [ProAppBar]
/// of any tab. Allows the user to:
///   - See their basic info (email, username)
///   - Edit their first name, last name, and username
///   - Change their password
///   - Log out
///
/// Each action is independent and shows a snackbar with the result. No
/// optimistic updates — server is the source of truth.
class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    final user = auth.currentUser;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: const ProAppBar(title: 'Profile'),
      body: user == null
          ? const Center(
              child: CircularProgressIndicator(color: AppColors.primary),
            )
          : ListView(
              padding:
                  const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
              children: [
                _ProfileHeader(user: user),
                const SizedBox(height: 24),
                _EditProfileCard(),
                const SizedBox(height: 16),
                const _ChangePasswordCard(),
                const SizedBox(height: 24),
                _LogoutButton(),
              ],
            ),
    );
  }
}

// ── Header ────────────────────────────────────────────────────────────────────
class _ProfileHeader extends StatelessWidget {
  const _ProfileHeader({required this.user});
  final User user;

  @override
  Widget build(BuildContext context) {
    final display = user.fullName.isNotEmpty ? user.fullName : user.username;
    final initials = display.isEmpty
        ? '?'
        : display
            .trim()
            .split(RegExp(r'\s+'))
            .map((s) => s.isEmpty ? '' : s[0])
            .take(2)
            .join()
            .toUpperCase();
    return Center(
      child: Column(
        children: [
          CircleAvatar(
            radius: 48,
            backgroundColor: AppColors.primary100,
            child: Text(
              initials,
              style: AppTextStyles.headline.copyWith(
                color: AppColors.primary,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
          const SizedBox(height: 12),
          Text(display, style: AppTextStyles.headlineSmall),
          const SizedBox(height: 4),
          Text(user.email, style: AppTextStyles.bodySecondary),
        ],
      ),
    );
  }
}

// ── Edit profile ──────────────────────────────────────────────────────────────
class _EditProfileCard extends StatefulWidget {
  const _EditProfileCard();

  @override
  State<_EditProfileCard> createState() => _EditProfileCardState();
}

class _EditProfileCardState extends State<_EditProfileCard> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _firstName;
  late final TextEditingController _lastName;
  late final TextEditingController _username;

  @override
  void initState() {
    super.initState();
    final user = context.read<AuthProvider>().currentUser!;
    _firstName = TextEditingController(text: user.firstName);
    _lastName = TextEditingController(text: user.lastName);
    _username = TextEditingController(text: user.username);
  }

  @override
  void dispose() {
    _firstName.dispose();
    _lastName.dispose();
    _username.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    final auth = context.read<AuthProvider>();
    final ok = await auth.updateProfile(
      firstName: _firstName.text.trim(),
      lastName: _lastName.text.trim(),
      username: _username.text.trim(),
    );
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(ok ? 'Profile updated' : (auth.error ?? 'Update failed')),
        backgroundColor: ok ? AppColors.income : AppColors.expense,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return AppCard(
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Edit profile', style: AppTextStyles.headline),
            const SizedBox(height: 16),
            TextFormField(
              controller: _firstName,
              textCapitalization: TextCapitalization.words,
              decoration: const InputDecoration(labelText: 'First name'),
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _lastName,
              textCapitalization: TextCapitalization.words,
              decoration: const InputDecoration(labelText: 'Last name'),
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _username,
              decoration: const InputDecoration(labelText: 'Username'),
              validator: (v) {
                if (v == null || v.trim().length < 3) {
                  return 'At least 3 characters';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),
            Align(
              alignment: Alignment.centerRight,
              child: FilledButton(
                onPressed: _save,
                child: const Text('Save changes'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Change password ───────────────────────────────────────────────────────────
class _ChangePasswordCard extends StatefulWidget {
  const _ChangePasswordCard();

  @override
  State<_ChangePasswordCard> createState() => _ChangePasswordCardState();
}

class _ChangePasswordCardState extends State<_ChangePasswordCard> {
  final _formKey = GlobalKey<FormState>();
  final _oldPwd = TextEditingController();
  final _newPwd = TextEditingController();
  final _confirmPwd = TextEditingController();
  bool _obscure = true;

  @override
  void dispose() {
    _oldPwd.dispose();
    _newPwd.dispose();
    _confirmPwd.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    if (_newPwd.text != _confirmPwd.text) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('New passwords do not match'),
          backgroundColor: AppColors.expense,
        ),
      );
      return;
    }
    final auth = context.read<AuthProvider>();
    final ok = await auth.changePassword(
      oldPassword: _oldPwd.text,
      newPassword: _newPwd.text,
    );
    if (!mounted) return;
    if (ok) {
      _oldPwd.clear();
      _newPwd.clear();
      _confirmPwd.clear();
    }
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(ok
            ? 'Password changed'
            : (auth.error ?? 'Could not change password')),
        backgroundColor: ok ? AppColors.income : AppColors.expense,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return AppCard(
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Change password', style: AppTextStyles.headline),
            const SizedBox(height: 16),
            TextFormField(
              controller: _oldPwd,
              obscureText: _obscure,
              decoration: const InputDecoration(
                  labelText: 'Current password'),
              validator: (v) =>
                  v == null || v.isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _newPwd,
              obscureText: _obscure,
              decoration: const InputDecoration(labelText: 'New password'),
              validator: (v) {
                if (v == null || v.length < 8) {
                  return 'At least 8 characters';
                }
                return null;
              },
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _confirmPwd,
              obscureText: _obscure,
              decoration:
                  const InputDecoration(labelText: 'Confirm new password'),
              validator: (v) {
                if (v == null || v.isEmpty) return 'Required';
                if (v != _newPwd.text) return 'Does not match';
                return null;
              },
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Checkbox(
                  value: _obscure,
                  onChanged: (v) => setState(() => _obscure = v ?? true),
                ),
                Text('Hide passwords', style: AppTextStyles.bodySecondary),
                const Spacer(),
                FilledButton(
                  onPressed: _save,
                  child: const Text('Update password'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

// ── Logout ────────────────────────────────────────────────────────────────────
class _LogoutButton extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return OutlinedButton.icon(
      style: OutlinedButton.styleFrom(
        foregroundColor: AppColors.expense,
        side: const BorderSide(color: AppColors.expense),
        minimumSize: const Size(double.infinity, 52),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
      onPressed: () => _confirmLogout(context),
      icon: const Icon(Icons.logout, size: 18),
      label: const Text('Log out'),
    );
  }

  Future<void> _confirmLogout(BuildContext context) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Log out?'),
        content: const Text(
            'You will need to sign in again to access your account.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: AppColors.expense),
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Log out'),
          ),
        ],
      ),
    );
    if (confirmed != true || !context.mounted) return;
    await context.read<AuthProvider>().logout();
    // AuthGate handles the navigation; nothing else to do.
  }
}
