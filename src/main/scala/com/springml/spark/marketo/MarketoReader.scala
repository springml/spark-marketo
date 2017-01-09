package com.springml.spark.marketo

import java.util.{List, Map}

import scala.collection.JavaConversions._

import com.springml.marketo.rest.client.MarketoClientFactory
import com.springml.marketo.rest.client.model.{Error, QueryResult}
import com.springml.spark.marketo.model.MarketoInput
import org.apache.log4j.Logger

/**
  * Created by sam on 4/1/17.
  */
class MarketoReader() {
  @transient val logger = Logger.getLogger(classOf[MarketoReader])

  def read(marketoInput: MarketoInput) : List[Map[String, String]] = {
    if (marketoInput.filterType == null) {
      readAllRecords(marketoInput)
    } else {
      queryRecords(marketoInput)
    }
  }

  def readAllRecords(marketoInput: MarketoInput): List[Map[String, String]] = {
    val leadClient = MarketoClientFactory.getLeadDatabaseClient(marketoInput.clientId, marketoInput.clientSecret,
      marketoInput.instanceUrl, marketoInput.apiVersion)
    leadClient.getAllRecords(marketoInput.objectToBeQueried, marketoInput.fields)
  }

  def queryRecords(marketoInput: MarketoInput): List[Map[String, String]] = {
    val leadClient = MarketoClientFactory.getLeadDatabaseClient(marketoInput.clientId, marketoInput.clientSecret,
      marketoInput.instanceUrl, marketoInput.apiVersion)
    var queryResult = leadClient.query(marketoInput.objectToBeQueried, marketoInput.filterType,
      marketoInput.filterValues, marketoInput.fields)

    handleError(queryResult)
    val records = queryResult.getResult
    while (queryResult.getNextPageToken != null) {
      queryResult = leadClient.fetchNextPage(queryResult)
      handleError(queryResult)
      records.addAll(queryResult.getResult)
    }

    records
  }

  private def handleError(queryResult: QueryResult) : Unit = {
    if (!queryResult.isSuccess) {
      logger.error("Error while querying data from Marketo")
      val errors = queryResult.getErrors

      for (marketoError <- errors) {
        logger.error("Error code : " + marketoError.getCode)
        logger.error("Error message : " + marketoError.getMessage)
      }
      sys.error("Error while querying data from Marketo")
    }
  }
}
