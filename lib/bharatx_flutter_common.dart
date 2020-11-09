import 'dart:async';

import 'package:flutter/services.dart';

class BharatXCommonUtilManager {
  static const String _signature = "flutter.bharatx.tech/common";
  static const MethodChannel _channel = const MethodChannel(_signature);

  static Future<void> registerCreditAccess() async {
    await _channel.invokeMethod('registerCreditAccess');
  }

  static Future<void> showBharatXProgressDialog() async {
    await _channel.invokeMethod('showBharatXProgressDialog');
  }

  static Future<void> closeBharatXProgressDialog() async {
    await _channel.invokeMethod('closeBharatXProgressDialog');
  }

  static Future<CreditInfo> get userCreditInfo async {
    return CreditInfo.fromJson(
        await _channel.invokeMethod('getUserCreditInfo'));
  }

  static void confirmTransactionWithUser(
      int amountInPaise,
      void onUserConfirmedTransaction(),
      void onUserAcceptedPrivacyPolicy(),
      void onUserCancelledTransaction()) {
    _channel.invokeMethod(
        'confirmTransactionWithUser', {"amountInPaise": amountInPaise});
    const MethodChannel confirmTransactionWithUserChannel =
        const MethodChannel("$_signature/confirmTransactionWithUser");
    confirmTransactionWithUserChannel.setMethodCallHandler((call) {
      switch (call.method) {
        case "onUserConfirmedTransaction":
          {
            onUserConfirmedTransaction();
            break;
          }
        case "onUserAcceptedPrivacyPolicy":
          {
            onUserAcceptedPrivacyPolicy();
            break;
          }
        case "onUserCancelledTransaction":
          {
            onUserCancelledTransaction();
            break;
          }
      }
      return null;
    });
  }

  static void showTransactionStatusDialog(
      bool isTransactionSuccessful, void onStatusDialogClose()) {
    _channel.invokeMethod('showTransactionStatusDialog',
        {"isTransactionSuccessful": isTransactionSuccessful});
    const MethodChannel showTransactionStatusDialogChannel =
        const MethodChannel("$_signature/showTransactionStatusDialog");
    showTransactionStatusDialogChannel.setMethodCallHandler((call) {
      switch (call.method) {
        case "onStatusDialogClose":
          {
            onStatusDialogClose();
            break;
          }
      }
      return null;
    });
  }

  static void registerTransactionId(
      String transactionId, void onRegistered(), void onFailure()) {
    _channel.invokeMethod(
        'registerTransactionId', {"transactionId": transactionId});
    const MethodChannel registerTransactionIdChannel =
        const MethodChannel("$_signature/registerTransactionId");
    registerTransactionIdChannel.setMethodCallHandler((call) {
      switch (call.method) {
        case "onRegistered":
          {
            onRegistered();
            break;
          }
        case "onFailure":
          {
            onFailure();
            break;
          }
      }
      return null;
    });
  }
}

class CreditInfo {
  int creditTaken, creditLimit;

  CreditInfo(this.creditTaken, this.creditLimit);

  static CreditInfo fromJson(dynamic json) {
    return CreditInfo(json['creditTaken'], json['creditLimit']);
  }
}
