package com.zone.chatterz.Model

import com.google.firebase.database.PropertyName

data class Group(

    @set:PropertyName("groupName")
    @get:PropertyName("groupName")
    var groupName : String,
    @set:PropertyName("groupImgUrl")
    @get:PropertyName("groupImgUrl")
    var groupImgUrl : String

){
    constructor():this("","")
}