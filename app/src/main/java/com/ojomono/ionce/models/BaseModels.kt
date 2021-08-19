package com.ojomono.ionce.models

abstract class BaseModel(open val id: String = "") {
    override fun equals(other: Any?): Boolean {
        return (id == (other as BaseModel).id)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

abstract class BaseItemModel(override val id: String = "") : BaseModel()