package com.springml.spark.marketo

import java.util.{List, Map}

import com.springml.marketo.rest.client.MarketoClientFactory
import com.springml.marketo.rest.client.model.QueryResult
import com.springml.spark.marketo.model.MarketoInput
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger

import scala.collection.JavaConversions._

/**
  * Created by sam on 4/1/17.
  */
class MarketoReader() {
  @transient val logger = Logger.getLogger(classOf[MarketoReader])

  def read(marketoInput: MarketoInput) : List[Map[String, String]] = {
    var records : List[Map[String, String]] = null

    if (marketoInput.objectToBeQueried.equalsIgnoreCase("activities")) {
      return readActivities(marketoInput)
    } else if (marketoInput.objectToBeQueried.contains("leadchanges")) {
      return readLeadChangesActivities(marketoInput)
    } else if (marketoInput.objectToBeQueried.contains("deletedleads")) {
      return readDeletedLeadsActivities(marketoInput)
    } else if (marketoInput.objectToBeQueried.equals("opportunities")) {
      return readOpportunities(marketoInput)
    } else if (marketoInput.objectToBeQueried.equals("roles")) {
      return readOpportunitiesRoles(marketoInput)
    }

    if (marketoInput.filterType == null) {
      records = readAllRecords(marketoInput)
    } else {
      records = queryRecords(marketoInput)
    }

    records
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

  private def readOpportunitiesRoles(marketoInput: MarketoInput) : List[Map[String, String]] = {
    marketoInput.objectToBeQueried = "opportunities/roles"
    readOpportunities(marketoInput)
  }

  private def readOpportunities(marketoInput: MarketoInput) : List[Map[String, String]] = {
    if (StringUtils.isEmpty(marketoInput.filterType)) {
      sys.error("filterType is mandatory parameter to get Marketo opportunities. " +
        "Please provide searchableFields as filterType")
    }

    if (StringUtils.isEmpty(marketoInput.filterValues)) {
      sys.error("filterValues is mandatory parameter to get Marketo opportunities. " +
        "Please specify comma separated values")
    }

    queryRecords(marketoInput)
  }

  private def readLeadChangesActivities(marketoInput: MarketoInput) : List[Map[String, String]] = {
    if (StringUtils.isEmpty(marketoInput.sinceDateTime)) {
      sys.error("sinceDateTime is mandatory to query activities. It has to be ISO 8601 standard date notation")
    }

    if (StringUtils.isEmpty(marketoInput.modifiedFields)) {
      sys.error("modifiedFields is mandatory to query Lead Changes Activities. " +
        "Please specify it in comma separted format")
    }

    val leadClient = MarketoClientFactory.getLeadDatabaseClient(marketoInput.clientId, marketoInput.clientSecret,
      marketoInput.instanceUrl, marketoInput.apiVersion)

    leadClient.getLeadChangesActivites(marketoInput.sinceDateTime, marketoInput.modifiedFields.split(",").toList)
  }

  private def readDeletedLeadsActivities(marketoInput: MarketoInput) : List[Map[String, String]] = {
    if (StringUtils.isEmpty(marketoInput.sinceDateTime)) {
      sys.error("sinceDateTime is mandatory to query Deleted Lead Activities. It has to be ISO 8601 standard date notation")
    }

    val leadClient = MarketoClientFactory.getLeadDatabaseClient(marketoInput.clientId, marketoInput.clientSecret,
      marketoInput.instanceUrl, marketoInput.apiVersion)

    leadClient.getDeletedLeadsActivites(marketoInput.sinceDateTime)
  }

  private def readActivities(marketoInput: MarketoInput) : List[Map[String, String]] = {
    if (!"activityTypeIds".equalsIgnoreCase(marketoInput.filterType)) {
      sys.error("To query activities filterType should be activityTypeIds")
    }

    if (marketoInput.filterValues == null || marketoInput.filterValues.isEmpty) {
      sys.error("To query activities activityTypeIds should be passed in filterValues")
    }

    if (StringUtils.isEmpty(marketoInput.sinceDateTime)) {
      sys.error("sinceDateTime is mandatory to query activities. It has to be ISO 8601 standard date notation")
    }

    val leadClient = MarketoClientFactory.getLeadDatabaseClient(marketoInput.clientId, marketoInput.clientSecret,
      marketoInput.instanceUrl, marketoInput.apiVersion)

    leadClient.getActivities(marketoInput.sinceDateTime, marketoInput.filterValues.split(",").toList)
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
