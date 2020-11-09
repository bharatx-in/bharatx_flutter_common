package tech.bharatx.bharatx_flutter_common

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import tech.bharatx.common.BharatXCommonUtilManager
import tech.bharatx.common.CreditAccessManager
import tech.bharatx.common.data_classes.CreditInfo
import tech.bharatx.securityhelpers.SecurityStorageManager
import tech.bharatx.securityhelpers.data_classes.BharatXTier

/** BharatxFlutterCommonPlugin */
class BharatxFlutterCommonPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
  private val signature = "flutter.bharatx.tech/common"
  private lateinit var channel: MethodChannel
  private lateinit var applicationContext: Context
  private lateinit var binaryMessenger: BinaryMessenger
  private var activity: FragmentActivity? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    this.binaryMessenger = flutterPluginBinding.binaryMessenger
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, signature)
    channel.setMethodCallHandler(this)
    applicationContext = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "registerCreditAccess" -> {
        CreditAccessManager.register(applicationContext)
        result.success(null)
      }
      "showBharatXProgressDialog" -> {
        if (activity != null) {
          BharatXCommonUtilManager.showBharatXProgressDialog(activity!!)
        } else {
          Log.v(javaClass.canonicalName, "Activity not attached")
        }
        result.success(null)
      }
      "closeBharatXProgressDialog" -> {
        BharatXCommonUtilManager.closeBharatXProgressDialog()
        result.success(null)
      }
      "getUserCreditInfo" -> {
        CreditAccessManager.getUserCreditInfo(applicationContext, object : CreditAccessManager.OnCreditInfoCompleteListener {
          override fun onComplete(creditInfo: CreditInfo) {
            activity?.runOnUiThread {
              result.success(HashMap<String, Long>().apply {
                put("creditTaken", creditInfo.creditTaken)
                put("creditLimit", creditInfo.creditLimit)
              })
            }
          }
        })
      }
      "confirmTransactionWithUser" -> {
        SecurityStorageManager.storePartnerTier(applicationContext, BharatXTier.STARTUP)
        val confirmTransactionWithUserChannel = MethodChannel(binaryMessenger, "${signature}/confirmTransactionWithUser")
        BharatXCommonUtilManager.confirmTransactionWithUser(activity!!,
            call.argument<Long>("amountInPaise")!!,
            object : BharatXCommonUtilManager.TransactionConfirmationListener {
              override fun onUserAcceptedPrivacyPolicy() {
                activity?.runOnUiThread {
                  confirmTransactionWithUserChannel.invokeMethod("onUserAcceptedPrivacyPolicy", null)
                }
              }

              override fun onUserCancelledTransaction() {
                activity?.runOnUiThread {
                  confirmTransactionWithUserChannel.invokeMethod("onUserCancelledTransaction", null)
                }
              }

              override fun onUserConfirmedTransaction() {
                activity?.runOnUiThread {
                  confirmTransactionWithUserChannel.invokeMethod("onUserConfirmedTransaction", null)
                }
              }
            })
      }
      "showTransactionStatusDialog" -> {
        val showTransactionStatusDialogChannel = MethodChannel(binaryMessenger, "${signature}/showTransactionStatusDialog")
        BharatXCommonUtilManager.showTransactionStatusDialog(activity!!, call.argument<Boolean>("isTransactionSuccessful")!!,
            object : BharatXCommonUtilManager.TransactionStatusShowListener {
              override fun onStatusDialogClose() {
                activity?.runOnUiThread {
                  showTransactionStatusDialogChannel.invokeMethod("onStatusDialogClose", null)
                }
              }
            })
      }
      "registerTransactionId" -> {
        val registerTransactionIdChannel = MethodChannel(binaryMessenger, "${signature}/registerTransactionId")
        CreditAccessManager.registerTransactionId(activity!!, call.argument<String>("transactionId")!!,
            object : CreditAccessManager.RegisterTransactionListener {
              override fun onRegistered() {
                activity?.runOnUiThread {
                  registerTransactionIdChannel.invokeMethod("onRegistered", null)
                }
              }

              override fun onFailure() {
                activity?.runOnUiThread {
                  registerTransactionIdChannel.invokeMethod("onFailure", null)
                }
              }
            })
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun onActivityChange(activity: Activity) {
    this.activity = activity as FragmentActivity
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    onActivityChange(binding.activity)
  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onActivityChange(binding.activity)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }
}
