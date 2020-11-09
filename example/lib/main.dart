import 'package:bharatx_flutter_common/bharatx_flutter_common.dart';
import 'package:bharatx_flutter_securityhelpers/bharatx_flutter_securityhelpers.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  void confirmTransactionWithUser() async {
    try {
      await BharatXSecurityHelpers.storePartnerId("testPartnerId");
      await BharatXSecurityHelpers.storePartnerApiKey("testApiKey");
      CreditInfo creditInfo = await BharatXCommonUtilManager.userCreditInfo;
      print("Credit Info ${creditInfo.creditTaken}/${creditInfo.creditLimit}");
      BharatXCommonUtilManager.confirmTransactionWithUser(10000, () {
        print("User Confirmed Transaction");
        BharatXCommonUtilManager.registerTransactionId("transactionId", () {
          BharatXCommonUtilManager.showTransactionStatusDialog(true, () {
            print("Closed");
          });
        }, () {
          print("Failed to register transaction ID");
        });
      }, () {
        print("User Accepted Privacy Policy");
      }, () {
        print("User Cancelled Transaction");
      });
    } on PlatformException {
      print("Platform Exception");
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: TextButton(
            onPressed: confirmTransactionWithUser,
            child: Text("Click Here"),
          ),
        ),
      ),
    );
  }
}
