package com.example.sakura.accountdemo

import io.realm.RealmObject

open class Person: RealmObject {

    constructor(firstName: String?, lastName: String?) : super() {
        this.firstName = firstName
        this.lastName = lastName
    }

    constructor() : super()

    var firstName: String? = null
    var lastName: String? =null
}