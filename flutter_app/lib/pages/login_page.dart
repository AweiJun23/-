import 'package:flutter/material.dart';
import '../services/api.dart';
import 'home_page.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});
  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _user = TextEditingController(text: 'farmer01');
  final _pwd = TextEditingController(text: '123456');
  bool _loading = false;

  Future<void> _login() async {
    setState(() => _loading = true);
    try {
      final data = await ApiService.login(_user.text.trim(), _pwd.text);
      ApiService.token = data['token'];
      if (mounted) {
        Navigator.pushReplacement(
            context, MaterialPageRoute(builder: (_) => HomePage(user: data)));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text('登录失败：$e')));
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('农户登录')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            TextField(
                controller: _user,
                decoration: const InputDecoration(labelText: '账号')),
            TextField(
                controller: _pwd,
                decoration: const InputDecoration(labelText: '密码'),
                obscureText: true),
            const SizedBox(height: 16),
            ElevatedButton(
                onPressed: _loading ? null : _login,
                child: _loading
                    ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(strokeWidth: 2))
                    : const Text('登录')),
            const SizedBox(height: 8),
            const Text('演示账号 farmer01 / 123456',
                style: TextStyle(color: Colors.grey)),
          ],
        ),
      ),
    );
  }
}
