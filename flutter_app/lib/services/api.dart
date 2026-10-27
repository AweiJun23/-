import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;

/// 后端网关 API 封装（含 JWT 注入与 X-Source=APP 标记）。
class ApiService {
  static const String baseUrl = 'http://localhost:9000';
  static String? token;

  static Map<String, String> _headers() => {
        'Content-Type': 'application/json',
        'Authorization': token != null ? 'Bearer $token' : '',
        'X-Source': 'APP',
      };

  static dynamic _parse(http.Response res) {
    final d = jsonDecode(res.body);
    if (d is Map && d.containsKey('code')) {
      if (d['code'] == 0 || d['success'] == true) return d['data'];
      if (d['code'] == 401) throw Exception(d['message'] ?? '未登录');
      throw Exception(d['message'] ?? '请求失败');
    }
    return d;
  }

  static Future<Map<String, dynamic>> login(String username, String password) async {
    final res = await http.post(Uri.parse('$baseUrl/api/auth/login'),
        headers: _headers(), body: jsonEncode({'username': username, 'password': password}));
    return _parse(res) as Map<String, dynamic>;
  }

  static Future<Map<String, dynamic>> wechatLogin(String code) async {
    final res = await http.post(Uri.parse('$baseUrl/api/auth/wechat/login'),
        headers: _headers(), body: jsonEncode({'code': code}));
    return _parse(res) as Map<String, dynamic>;
  }

  static Future<dynamic> submitFarming(Map<String, dynamic> payload) async {
    final res = await http.post(Uri.parse('$baseUrl/api/farming/record'),
        headers: _headers(), body: jsonEncode(payload));
    return _parse(res);
  }

  static Future<dynamic> trace(String batchNo) async {
    final res = await http.get(Uri.parse('$baseUrl/api/batch/trace/$batchNo'),
        headers: _headers());
    return _parse(res);
  }

  static Future<dynamic> subsidy(String batchId) async {
    final res = await http.get(Uri.parse('$baseUrl/api/report/latest/$batchId'),
        headers: _headers());
    return _parse(res);
  }
}
