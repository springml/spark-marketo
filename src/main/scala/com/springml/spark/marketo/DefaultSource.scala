package com.springml.spark.marketo

import com.springml.spark.marketo.model.MarketoInput
import org.apache.log4j.Logger
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}

import scala.collection.mutable

/**
  * DefaultSource for Spark Marketo Connector
  */
class DefaultSource extends RelationProvider with SchemaRelationProvider with CreatableRelationProvider {
  @transient val logger = Logger.getLogger(classOf[DefaultSource])

  override def createRelation(sqlContext: SQLContext,
                              parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext, parameters, null)
  }

  override def createRelation(sqlContext: SQLContext,
                              parameters: Map[String, String],
                              schema: StructType): BaseRelation = {
    val marketoInput = new MarketoInput
    marketoInput.clientId = param(parameters, "clientId")
    marketoInput.clientSecret = param(parameters, "clientSecret")
    marketoInput.instanceUrl = param(parameters, "instanceURL")
    marketoInput.objectToBeQueried = param(parameters, "object")
    marketoInput.filterType = paramValue(parameters, "filterType")
    marketoInput.filterValues = paramValue(parameters, "filterValues")
    marketoInput.customObject = parameters.getOrElse("customObject", "false")
    marketoInput.apiVersion = parameters.getOrElse("apiVersion", "v1")

    val pageSizeParam = parameters.getOrElse("pageSize", "300")
    marketoInput.pageSize = pageSizeParam.toInt


    val records : List[mutable.Map[String, String]] = null
    new DatasetRelation(records, sqlContext, schema)
  }

  override def createRelation(sqlContext: SQLContext,
                              mode: SaveMode,
                              parameters: Map[String, String],
                              data: DataFrame): BaseRelation = {
    logger.error("Save not supported by Marketo connector")
    throw new UnsupportedOperationException
  }

  private def param(parameters: Map[String, String],
                    paramName: String) : String = {
    val paramValue = parameters.getOrElse(paramName,
      sys.error(s"""'$paramName' must be specified for Spark Marketo package"""))

    if (!"clientSecret".equals(paramName)) {
      logger.debug("Param " + paramName + " value " + paramValue)
    }

    paramValue
  }

  private def paramValue(parameters: Map[String, String],
                    paramName: String) : String = {
    val paramValue = parameters.get(paramName)
    if (paramValue != null && paramValue.isDefined) {
      paramValue.get
    } else {
      null
    }
  }

}
