
// COMMAND ----------

spark.conf.set(
  "fs.azure.account.key."+dbutils.widgets.get("StorageAccount")+".blob.core.windows.net",
  dbutils.widgets.get("StorageAccountKey"))

// COMMAND ----------

// File location and type
val file_location = "/FileStore/tables/train.csv"
val file_type = "csv"

// CSV options
val infer_schema = "true"
val first_row_is_header = "true"
val delimiter = ","

import org.apache.spark.sql.functions.{col, substring}

// The applied options are for CSV files. For other file types, these will be ignored.
val df = spark.read.format(file_type) 
  .option("inferSchema", infer_schema) 
  .option("header", first_row_is_header) 
  .option("sep", delimiter) 
  .load(file_location)
  .withColumn("CabinLevel", substring(col("Cabin"),0,1))

// COMMAND ----------

display(df)

// COMMAND ----------

display(df)

// COMMAND ----------

display(df)

// COMMAND ----------

display(df)

// COMMAND ----------

display(df)

// COMMAND ----------

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.feature.Bucketizer
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.feature.OneHotEncoderEstimator


val ageSplits = Array(0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0)
val fareSplits = Array(0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0, 200.0, 300.0, 500.0, 1000.0)

val sexIndexer = new StringIndexer()
  .setInputCol("sex")
  .setOutputCol("SexVector")

val embarkedIndexer = new StringIndexer()
  .setInputCol("embarked")
  .setOutputCol("EmbarkedIndex")
  .setHandleInvalid("keep")

val cabinLevelIndexer = new StringIndexer()
  .setInputCol("CabinLevel")
  .setOutputCol("CabinLevelIndex")
  .setHandleInvalid("keep")

val ageBucketizer = new Bucketizer()
  .setInputCol("age")
  .setOutputCol("AgeBuckets")
  .setSplits(ageSplits)

val fareBucketizer = new Bucketizer()
  .setInputCol("fare")
  .setOutputCol("FareBuckets")
  .setSplits(fareSplits)

val encoder = new OneHotEncoderEstimator()
  .setInputCols(Array("pclass", "AgeBuckets", "FareBuckets", "sibsp", "parch", "EmbarkedIndex", "CabinLevelIndex"))
  .setOutputCols(Array("PclassVec", "AgeBucketsVec", "FareBucketsVec", "SibSpVec", "ParchVec", "EmbarkedVec", "CabinLevelVec"))

val assembler = new VectorAssembler()
  .setInputCols(Array("SexVector", "PclassVec", "AgeBucketsVec", "FareBucketsVec", "SibSpVec", "ParchVec", "EmbarkedVec", "CabinLevelVec"))
  .setOutputCol("features")
  .setHandleInvalid("skip")

val pipeline = new Pipeline()
  .setStages(Array(sexIndexer, embarkedIndexer, cabinLevelIndexer, ageBucketizer, fareBucketizer, encoder, assembler))

// Fit the pipeline to training documents.
val model = pipeline.fit(df)
val train = model.transform(df).select("Survived", "features")
display(train)

// COMMAND ----------

import org.apache.spark.ml.classification.LogisticRegression

val lr = new LogisticRegression()
  .setMaxIter(10)
  .setRegParam(0.3)
  .setElasticNetParam(0.8)
  .setLabelCol("Survived")

// Fit the model
val lrModel = lr.fit(train)
println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")


// COMMAND ----------

// display(lrModel, train)

// COMMAND ----------

// display(lrModel, train, "ROC")

// COMMAND ----------

import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.sql.functions.max

// Extract the summary from the returned LogisticRegressionModel instance trained in the earlier
// example
val trainingSummary = lrModel.binarySummary

// Obtain the receiver-operating characteristic as a dataframe and areaUnderROC.
val roc = trainingSummary.roc

println(s"areaUnderROC: ${trainingSummary.areaUnderROC}")
trainingSummary.accuracy

// COMMAND ----------


// Set the model threshold to maximize F-Measure
val fMeasure = trainingSummary.fMeasureByThreshold
val maxFMeasure = fMeasure.select(max("F-Measure")).head().getDouble(0)
val bestThreshold = fMeasure.where($"F-Measure" === maxFMeasure)
  .select("threshold").head().getDouble(0)
lrModel.setThreshold(bestThreshold)

// COMMAND ----------

import org.apache.spark.sql.functions.udf
import org.apache.spark.ml.linalg.Vector

// Get size of the vector
val n = train.first.getAs[org.apache.spark.ml.linalg.Vector](1).size

// Simple helper to convert vector to array<double> 
val vecToSeq = udf((v: Vector) => v.toArray)
val exprs = (0 until n).map(i => $"_tmp".getItem(i).alias(s"f$i"))
train
  .select(col("Survived"),vecToSeq($"features").alias("_tmp"))
  .select(exprs:_*)
  .write
  .mode("overwrite")
  .csv("wasbs://tdhackfestcontainer@" + dbutils.widgets.get("StorageAccount") + ".blob.core.windows.net/dciborow/train_vector_full.csv")

// COMMAND ----------

display(spark.read.csv("wasbs://tdhackfestcontainer@" + dbutils.widgets.get("StorageAccount") + ".blob.core.windows.net/dciborow/train_vector_full.csv"))

// COMMAND ----------

