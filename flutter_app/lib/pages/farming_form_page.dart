import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/api.dart';
import '../services/local_cache.dart';

class FarmingFormPage extends StatefulWidget {
  const FarmingFormPage({super.key});
  @override
  State<FarmingFormPage> createState() => _FarmingFormPageState();
}

class _FarmingFormPageState extends State<FarmingFormPage> {
  final _types = const [
    'FERTILIZE',
    'IRRIGATE',
    'MACHINE',
    'PESTICIDE',
    'FILM',
    'HARVEST',
    'PACKAGE',
    'TRANSPORT'
  ];
  final _units = const ['kg', 'L', 'kWh', 'mu'];

  final _batchId = TextEditingController();
  final _material = TextEditingController();
  final _amount = TextEditingController();
  final _area = TextEditingController();
  final _remark = TextEditingController();

  int _typeIdx = 0;
  int _unitIdx = 0;
  DateTime _workDate = DateTime.now();
  bool _busy = false;

  Future<void> _pickDate() async {
    final d = await showDatePicker(
        context: context,
        initialDate: _workDate,
        firstDate: DateTime(2020),
        lastDate: DateTime.now());
    if (d != null) setState(() => _workDate = d);
  }

  Map<String, dynamic> _buildPayload() => {
        'batchId': int.tryParse(_batchId.text) ?? 0,
        'farmingType': _types[_typeIdx],
        'materialName': _material.text,
        'amount': double.tryParse(_amount.text) ?? 0,
        'unit': _units[_unitIdx],
        'workDate': DateFormat('yyyy-MM-dd').format(_workDate),
        'areaMu': double.tryParse(_area.text),
        'remark': _remark.text,
      };

  Future<void> _saveOffline() async {
    await LocalCache.addPending(_buildPayload());
    if (mounted) {
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text('已离线保存，联网后可同步')));
      Navigator.pop(context);
    }
  }

  Future<void> _submit() async {
    setState(() => _busy = true);
    try {
      await ApiService.submitFarming(_buildPayload());
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(const SnackBar(content: Text('提交成功，已触发碳核算')));
        Navigator.pop(context);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text('提交失败，可离线保存：$e')));
      }
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _syncPending() async {
    final list = await LocalCache.load();
    if (list.isEmpty) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('无待同步记录')));
      }
      return;
    }
    int ok = 0;
    for (final rec in list) {
      try {
        await ApiService.submitFarming(rec);
        await LocalCache.removePending(rec['syncToken']);
        ok++;
      } catch (_) {}
    }
    if (mounted) {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('已同步 $ok/${list.length} 条')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('录入农事记录')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            TextField(
                controller: _batchId,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(labelText: '批次ID')),
            DropdownButtonFormField(
                value: _typeIdx,
                items: _types
                    .asMap()
                    .entries
                    .map((e) => DropdownMenuItem(value: e.key, child: Text(e.value)))
                    .toList(),
                onChanged: (v) => setState(() => _typeIdx = v!),
                decoration: const InputDecoration(labelText: '农事类型')),
            TextField(
                controller: _material,
                decoration: const InputDecoration(labelText: '物料名称（如 尿素/柴油）')),
            TextField(
                controller: _amount,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: const InputDecoration(labelText: '用量')),
            DropdownButtonFormField(
                value: _unitIdx,
                items: _units
                    .asMap()
                    .entries
                    .map((e) => DropdownMenuItem(value: e.key, child: Text(e.value)))
                    .toList(),
                onChanged: (v) => setState(() => _unitIdx = v!),
                decoration: const InputDecoration(labelText: '单位')),
            ListTile(
                title: Text('作业日期：${DateFormat('yyyy-MM-dd').format(_workDate)}'),
                trailing: const Icon(Icons.calendar_today),
                onTap: _pickDate),
            TextField(
                controller: _area,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: const InputDecoration(labelText: '作业面积(亩)')),
            TextField(
                controller: _remark,
                decoration: const InputDecoration(labelText: '备注')),
            const SizedBox(height: 16),
            ElevatedButton(
                onPressed: _busy ? null : _submit,
                child: _busy
                    ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                    : const Text('提交')),
            const SizedBox(height: 12),
            OutlinedButton(
                onPressed: _saveOffline,
                child: const Text('离线保存（弱网）')),
            const SizedBox(height: 12),
            OutlinedButton(
                onPressed: _syncPending,
                child: const Text('联网同步离线记录')),
          ],
        ),
      ),
    );
  }
}
