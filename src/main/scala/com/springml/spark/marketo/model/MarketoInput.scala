package com.springml.spark.marketo.model

/**
  * Created by sam on 4/1/17.
  */
class MarketoInput {
  var clientId : String = null
  var clientSecret : String = null
  var instanceUrl : String = null
  var objectToBeQueried : String = null
  var filterType : String = null
  var filterValues : String = null
  var customObject : String = null
  var apiVersion : String = null
  var pageSize : Integer = 300
}