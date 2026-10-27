import 'package:flutter/material.dart';
import '../services/local_cache.dart';
import 'farming_form_page.dart';
import 'report_page.dart';

class HomePage extends StatefulWidget {
  final Map<String, dynamic> user;
  const HomePage({super.key, required this.user});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _pending = 0;

  @override
  void initState() {
    super.initState();
    _refreshPending();
  }

  Future<void> _refreshPending() async {
    _pending = await LocalCache.pendingCount();
    if (mounted) setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final user = widget.user;
    return Scaffold(
      appBar: AppBar(title: const Text('农户首页')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('农户：${user['realName'] ?? user['username']}',
                        style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                    Text('角色：${user['role']}　合作社：${user['coopId'] ?? '-'}'),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            ElevatedButton.icon(
                onPressed: () async {
                  await Navigator.push(context,
                      MaterialPageRoute(builder: (_) => const FarmingFormPage()));
                  _refreshPending();
                },
                icon: const Icon(Icons.add),
                label: const Text('录入农事记录')),
            const SizedBox(height: 12),
            ElevatedButton.icon(
                onPressed: () => Navigator.push(context,
                    MaterialPageRoute(builder: (_) => const ReportPage())),
                icon: const Icon(Icons.eco),
                label: const Text('查看碳足迹报告 / 补贴材料')),
            const SizedBox(height: 12),
            Text('待同步离线记录：$_pending 条',
                style: const TextStyle(color: Colors.orange)),
          ],
        ),
      ),
    );
  }
}
