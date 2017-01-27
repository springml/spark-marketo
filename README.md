# Spark Marketo Library

A library for getting Marketo Lead database data into Spark. This uses [Marketo Lead Database REST API](http://developers.marketo.com/rest-api/lead-database/) to get the data from Marketo 

## Requirements

This library requires Spark 1.6+

## Linking
You can link against this library in your program at the following ways:

### Maven Dependency
```
<dependency>
    <groupId>com.springml</groupId>
    <artifactId>spark-marketo_2.10</artifactId>
    <version>1.0.0</version>
</dependency>
```

### SBT Dependency
```
libraryDependencies += "com.springml" % "spark-marketo_2.10" % "1.0.0"
```

## Using with Spark shell
This package can be added to Spark using the `--packages` command line option.  For example, to include it when starting the spark shell:

```
$ bin/spark-shell --packages com.springml:spark-marketo_2.10:1.0.0
```

## Feature
* **Construct Spark Dataframe using Marketo data** - User has to provide Marketo client Id, client secret and Marketo instance Url.

### Options
* `clientId`: Marketo account clientId
* `clientSecret`: Marketo account clientSecret
* `instanceURL`: Instance URL to be used to access Marketo. It has to be specified without /rest. i.e it should be like https://119-AAA-888.mktorest.com
* `object`: Object to be queried from Marketo. ex. leads
* `filterType`: Filter field to be used. Optional for leads - If not provided all leads will be fetched
* `filterValues`: Comma separated filter values to be applied (Optional - Needed only if filterType is specified)
* `sinceDatetime` : (Optional) Datatime from which the data has to be fetched. It has to be in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format
* `customObject`: (Optional) Boolean to specify if the specified object is custom object, Default value is false
* `apiVersion`: (Optional) API Version to be used. Default value is `v1`
* `modifiedFields`: (Optional) Fields to be considered for leadChanges. It has to be comma separated field names
* `schema`: (Optional) Schema to be used for constructing dataframes. If not provided all fields will be of type String

### Scala API
Spark 1.6+:
```scala
import org.apache.spark.sql.SQLContext

// Query leads using Id
// Here we are querying leads with Id 1,2,3
// Maximum of 300 values can be specified
val df = sqlContext.read.
    format("com.springml.spark.marketo").
    option("clientId", "a13b5932-69d4-1e25-4975-1d8788438662").
    option("client_secret", "3jFFseHLihsCZWq6PEB4JDt5GcbTDBSg").
    option("instanceURL", "https://119-AAA-888.mktorest.com").
    option("object", "leads").
    option("filterType", "Id").
    option("filterValues", "1,2,3").
    load()

// Read all leads
val df = sqlContext.read.
    format("com.springml.spark.marketo").
    option("clientId", "a13b5932-69d4-1e25-4975-1d8788438662").
    option("client_secret", "3jFFseHLihsCZWq6PEB4JDt5GcbTDBSg").
    option("instanceURL", "https://119-AAA-888.mktorest.com").
    option("object", "leads").
    load()

// Query activities 
// Here we are querying acivities with activity types 1 and 12
// And fetching acitivies after 2016-10-06
val df = sqlContext.read.
    format("com.springml.spark.marketo").
    option("clientId", "a13b5932-69d4-1e25-4975-1d8788438662").
    option("client_secret", "3jFFseHLihsCZWq6PEB4JDt5GcbTDBSg").
    option("instanceURL", "https://119-AAA-888.mktorest.com").
    option("object", "activities").
    option("filterType", "activityTypeIds").
    option("filterValues", "1,12").
    option("sinceDateTime", "2016-10-06").
    load()
```


### R API
Spark 1.6+:
```r
# Query all leads
df <- read.df(sqlContext, 
      source="com.springml.spark.marketo", 
      clientId="a13b5932-69d4-1e25-4975-1d8788438662", 
      clientSecret="3jFFseHLihsCZWq6PEB4JDt5GcbTDBSg", 
      instanceURL="https://119-AAA-888.mktorest.com",
      object="leads")

# Query activities 
# Here we are querying acivities with activity types 1 and 12
# And fetching acitivies after 2016-10-06
df <- read.df(sqlContext, 
      source="com.springml.spark.marketo", 
      clientId="a13b5932-69d4-1e25-4975-1d8788438662", 
      clientSecret="3jFFseHLihsCZWq6PEB4JDt5GcbTDBSg", 
      instanceURL="https://119-AAA-888.mktorest.com",
      filterType="activityTypeIds",
      filterValues="1,12",
      sinceDateTime="2016-10-06",
      object="activities")

# Query leadchanges based on the fields specified in modifiedFields
# Here fetching leadchanges from 2016-10-06 if there are any changes in firstname and email
df <- read.df(sqlContext, 
      source="com.springml.spark.marketo", 
      clientId="a13b5932-69d4-1e25-4975-1d8788438662", 
      clientSecret="3jFFseHLihsCZWq6PEB4JDt5GcbTDBSg", 
      instanceURL="https://119-AAA-888.mktorest.com",
      sinceDateTime="2016-10-06",
      modifiedFields="firstname,email",
      object="leadchanges")

# Query deleted leads
df <- read.df(sqlContext, 
      source="com.springml.spark.marketo", 
      clientId="a13b5932-69d4-1e25-4975-1d8788438662", 
      clientSecret="3jFFseHLihsCZWq6PEB4JDt5GcbTDBSg", 
      instanceURL="https://119-AAA-888.mktorest.com",
      sinceDateTime="2016-10-06",
      object="deletedleads")

# Query opportunity roles
df <- read.df(sqlContext, 
      source="com.springml.spark.marketo", 
      clientId="a13b5932-69d4-1e25-4975-1d8788438662", 
      clientSecret="3jFFseHLihsCZWq6PEB4JDt5GcbTDBSg", 
      instanceURL="https://119-AAA-888.mktorest.com",
      filterType="marketoGUID",
      filterValues="755a1902-640b-4a05-ba30-7e6e20d21502",
      object="roles")

# Query custom objects
# Here cars is custom object
# 'type' is a field in cars custom object 
df <- read.df(sqlContext, 
      source="com.springml.spark.marketo", 
      clientId="a13b5932-69d4-1e25-4975-1d8788438662", 
      clientSecret="3jFFseHLihsCZWq6PEB4JDt5GcbTDBSg", 
      instanceURL="https://119-AAA-888.mktorest.com",
      filterType="type",
      filterValues="saloon,cross",
      customObject="true",
      object="cars_c")

# Query Companies
df <- read.df(sqlContext, 
      source="com.springml.spark.marketo", 
      clientId="a13b5932-69d4-1e25-4975-1d8788438662", 
      clientSecret="3jFFseHLihsCZWq6PEB4JDt5GcbTDBSg", 
      instanceURL="https://119-AAA-888.mktorest.com",
      filterType="company",
      filterValues="SpringML",
      object="companies")

```


## Building From Source
This library is built with [SBT](http://www.scala-sbt.org/0.13/docs/Command-Line-Reference.html), which is automatically downloaded by the included shell script. To build a JAR file simply run `sbt/sbt package` from the project root. The build configuration includes support for both Scala 2.10 and 2.11.
