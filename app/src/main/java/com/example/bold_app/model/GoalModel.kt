package com.example.bold_app.model

data class GoalModel(

    val dateStart: String = "",

    val dateToday: String = "",

    val goalId: Int = 0,

    val day: Int? = 1,

    val clItem1: Boolean? = false,
    val clItem2: Boolean? = false,
    val clItem3: Boolean? = false,
    val clItem4: Boolean? = false,
    val clItem5: Boolean? = false,
    val clItem6: Boolean? = false,
    val clItem7: Boolean? = false,
    val clItem8: Boolean? = false,

    var dataId: String? = null
)
