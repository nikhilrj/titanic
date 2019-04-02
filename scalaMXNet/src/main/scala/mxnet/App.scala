package mxnet

object App extends App {
  import org.apache.mxnet._
  import org.apache.mxnet.optimizer.SGD
  import scala.collection.JavaConversions._

  // model definition
  val data = Symbol.Variable("data")
  val fc1 = Symbol.api.FullyConnected(Some(data), num_hidden = 128, name = "fc1")
  val act1 = Symbol.api.Activation(Some(fc1), "relu", "relu1")
  val fc2 = Symbol.api.FullyConnected(Some(act1), num_hidden = 64, name = "fc2")
  val act2 = Symbol.api.Activation(Some(fc2), "relu", "relu2")
  val fc3 = Symbol.api.FullyConnected(Some(act2), num_hidden = 10, name = "fc3")
  val mlp = Symbol.api.SoftmaxOutput(Some(fc3), name = "sm")

  def dataPath = System.getenv("DataPath")
  if (dataPath == null || dataPath.length() == 0 ) {
    println(" Specify DataPath")
    System.exit(1)
  }


  // load MNIST dataset
  val trainDataIter = IO.MNISTIter(Map(
    "image" -> (dataPath + "/train-images-idx3-ubyte"),
    "label" -> (dataPath + "/train-labels-idx1-ubyte"),
    "data_shape" -> "(1, 28, 28)",
    "label_name" -> "sm_label",
    "batch_size" -> "50",
    "shuffle" -> "1",
    "flat" -> "0",
    "silent" -> "0",
    "seed" -> "10"))

  val valDataIter = IO.MNISTIter(Map(
    "image" -> (dataPath + "/train-images-idx3-ubyte"),
    "label" -> (dataPath + "/train-labels-idx1-ubyte"),
    "data_shape" -> "(1, 28, 28)",
    "label_name" -> "sm_label",
    "batch_size" -> "50",
    "shuffle" -> "1",
    "flat" -> "0", "silent" -> "0"))


  println("training model")

  // setup model and fit the training data
  val model = FeedForward.newBuilder(mlp)
    .setContext(Context.cpu())
    .setNumEpoch(10)
    .setOptimizer(new SGD(learningRate = 0.1f, momentum = 0.9f, wd = 0.0001f))
    .setTrainData(trainDataIter)
    .setEvalData(valDataIter)
    .build()


  println("making predictions")
  val probArrays = model.predict(valDataIter)
  // in this case, we do not have multiple outputs
  require(probArrays.length == 1)
  val prob = probArrays(0)

  // get real labels
  import scala.collection.mutable.ListBuffer
  valDataIter.reset()
  val labels = ListBuffer.empty[NDArray]
  while (valDataIter.hasNext) {
    val evalData = valDataIter.next()
    labels += evalData.label(0).copy()
  }
  val y = NDArray.concatenate(labels)

  // get predicted labels
  val predictedY = NDArray.argmax_channel(prob)
  require(y.shape == predictedY.shape)

  // calculate accuracy
  var numCorrect = 0
  var numTotal = 0
  for ((labelElem, predElem) <- y.toArray zip predictedY.toArray) {
    if (labelElem == predElem) {
      numCorrect += 1
    }
    numTotal += 1
  }
  val acc = numCorrect.toFloat / numTotal
  println(s"Final accuracy = $acc")

}