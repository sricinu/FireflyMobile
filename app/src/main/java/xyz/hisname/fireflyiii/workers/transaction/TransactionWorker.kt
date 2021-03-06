package xyz.hisname.fireflyiii.workers.transaction

import android.content.Context
import androidx.work.*
import com.google.gson.Gson
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.BaseWorker

class TransactionWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val channelName = "Transactions"
    private val channelIcon = R.drawable.ic_refresh

    override suspend fun doWork(): Result {
       // val notif = NotificationUtils(context)
        val transactionType = inputData.getString("transactionType") ?: ""
        val transactionDescription = inputData.getString("description") ?: ""
        val transactionDate = inputData.getString("date") ?: ""
        val transactionAmount = inputData.getString("amount") ?: ""
        val transactionCurrency = inputData.getString("currency") ?: ""
        val destinationName = inputData.getString("destinationName") ?: ""
        val sourceName = inputData.getString("sourceName") ?: ""
        val piggyBank = inputData.getString("piggyBankName")
        val billName = inputData.getString("billName")
        val category = inputData.getString("categoryName")
        val tags = inputData.getString("tags")
        val budget = inputData.getString("budgetName")
        val interestDate  = inputData.getString("interestDate")
        val bookDate = inputData.getString("bookDate")
        val processDate = inputData.getString("processDate")
        val dueDate = inputData.getString("dueDate")
        val paymentDate = inputData.getString("paymentDate")
        val invoiceDate = inputData.getString("invoiceDate")
        genericService?.create(TransactionService::class.java)?.addTransaction(convertString(transactionType), transactionDescription, transactionDate, piggyBank,
                transactionAmount,sourceName, destinationName, transactionCurrency, category,
                tags, budget, interestDate, bookDate, processDate, dueDate, paymentDate,
                invoiceDate)?.enqueue(
                retrofitCallback({ response ->
                    var errorBody = ""
                    if (response.errorBody() != null) {
                        errorBody = String(response.errorBody()?.bytes()!!)
                    }
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    if (response.isSuccessful) {
                        context.displayNotification("Transaction added successfully!", transactionType,
                                Constants.TRANSACTION_CHANNEL, channelIcon)
                        Result.success()
                    } else {
                        var error = ""
                        when {
                            gson.errors.transactions_destination_name != null -> {
                                error = gson.errors.transactions_destination_name[0]
                            }
                            gson.errors.transactions_currency != null -> {
                                error = gson.errors.transactions_currency[0]
                            }
                            gson.errors.transactions_source_name != null -> {
                                error = gson.errors.transactions_source_name[0]
                            }
                        }
                        context.displayNotification(error, "Error Adding $transactionType",
                                Constants.TRANSACTION_CHANNEL, channelIcon)
                        Result.failure()
                    }
                })
                { throwable ->
                    context.displayNotification(throwable.message.toString(),
                            "Error Adding $transactionType", Constants.TRANSACTION_CHANNEL, channelIcon)
                    Result.failure()
                })
        return Result.success()
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

    companion object {
        fun initWorker(context: Context, data: Data.Builder, type: String) {
            val transactionWork = OneTimeWorkRequest.Builder(TransactionWorker::class.java)
                    .setInputData(data.putString("transactionType", type).build())
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()
            WorkManager.getInstance(context).enqueue(transactionWork)
        }
    }
}