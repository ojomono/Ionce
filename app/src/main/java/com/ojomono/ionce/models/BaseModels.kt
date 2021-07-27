package com.ojomono.ionce.models

abstract class BaseModel(open val id: String = "")

abstract class BaseItemModel(override val id: String = "") : BaseModel()