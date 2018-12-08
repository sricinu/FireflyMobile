package xyz.hisname.fireflyiii.repository.models.budget.limit

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class BudgetLimitModel(
        @Embedded
        @SerializedName("data")
        val budgetLimitData: List<BudgetLimitData>,
        val meta: Meta
)