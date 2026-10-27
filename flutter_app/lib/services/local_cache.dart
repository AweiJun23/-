import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

/// APP 离线缓存：弱网环境下农户先保存农事表单，
/// 联网后由「同步」按钮批量回传，避免数据丢失。
class LocalCache {
  static const String _key = 'pending_farming';

  static Future<List<Map<String, dynamic>>> load() async {
    final sp = await SharedPreferences.getInstance();
    final raw = sp.getString(_key);
    if (raw == null) return [];
    final list = jsonDecode(raw) as List;
    return list.cast<Map<String, dynamic>>();
  }

  static Future<void> _save(List<Map<String, dynamic>> list) async {
    final sp = await SharedPreferences.getInstance();
    await sp.setString(_key, jsonEncode(list));
  }

  /// 保存一条待同步记录（附带 syncToken 用于幂等）
  static Future<void> addPending(Map<String, dynamic> rec) async {
    final list = await load();
    list.add({
      ...rec,
      'syncToken': DateTime.now().millisecondsSinceEpoch.toString(),
    });
    await _save(list);
  }

  static Future<void> removePending(String syncToken) async {
    final list = await load();
    list.removeWhere((e) => e['syncToken'] == syncToken);
    await _save(list);
  }

  static Future<int> pendingCount() async => (await load()).length;
}
