import 'package:flutter/material.dart';
import '../services/api.dart';

class ReportPage extends StatefulWidget {
  const ReportPage({super.key});
  @override
  State<ReportPage> createState() => _ReportPageState();
}

class _ReportPageState extends State<ReportPage> {
  final _batchNo = TextEditingController();
  Map<String, dynamic>? _trace;
  Map<String, dynamic>? _subsidy;
  bool _loading = false;

  Future<void> _load() async {
    if (_batchNo.text.isEmpty) return;
    setState(() => _loading = true);
    try {
      _trace = await ApiService.trace(_batchNo.text.trim());
      _subsidy = null;
    } catch (e) {
      _trace = null;
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('查询失败：$e')));
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _loadSubsidy() async {
    if (_trace == null) return;
    final id = _trace!['id'];
    try {
      _subsidy = await ApiService.subsidy(id.toString());
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('暂无补贴材料')));
      }
    }
    if (mounted) setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final carbon = _trace?['carbonReport'];
    final list = _trace?['farmingList'] as List? ?? [];
    return Scaffold(
      appBar: AppBar(title: const Text('碳足迹报告')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            TextField(
                controller: _batchNo,
                decoration: const InputDecoration(labelText: '批次编号')),
            const SizedBox(height: 12),
            ElevatedButton(onPressed: _loading ? null : _load, child: const Text('查询')),
            const SizedBox(height: 16),
            if (_trace != null) ...[
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(_trace!['productName'] ?? '',
                          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                      Text('批次：${_trace!['batchNo']}'),
                      Text('产地：${_trace!['originProvince'] ?? ''}${_trace!['originCity'] ?? ''}'),
                      Text(_trace!['batchVerified'] == true ? '批次验真：已上链 ✅' : '批次验真：未验真',
                          style: TextStyle(
                              color: _trace!['batchVerified'] == true ? Colors.green : Colors.red)),
                    ],
                  ),
                ),
              ),
              if (carbon != null)
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('碳足迹标签', style: TextStyle(fontWeight: FontWeight.bold)),
                        Text('总排放：${carbon['totalEmission']} kgCO2e'),
                        Text('单位排放：${carbon['unitEmission']} kgCO2e/kg'),
                        Text('报告存证：${_trace!['carbonVerified'] == true ? '已存证 ✅' : '未存证'}'),
                        const SizedBox(height: 8),
                        Text('AI 低碳建议：${carbon['aiAdvice'] ?? ''}'),
                      ],
                    ),
                  ),
                ),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('农事时间线', style: TextStyle(fontWeight: FontWeight.bold)),
                      ...list.map((f) => Padding(
                            padding: const EdgeInsets.symmetric(vertical: 4),
                            child: Text(
                                '${f['farmingTypeDesc'] ?? f['farmingType']} · ${f['materialName']} ${f['amount']}${f['unit']}（${f['workDate']}）'),
                          )),
                      if (list.isEmpty) const Text('暂无农事记录'),
                    ],
                  ),
                ),
              ),
              ElevatedButton(onPressed: _loadSubsidy, child: const Text('查看补贴申报材料')),
              if (_subsidy != null)
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Text(_subsidy!['contentMd'] ?? '', style: const TextStyle(height: 1.5)),
                  ),
                ),
            ],
          ],
        ),
      ),
    );
  }
}
