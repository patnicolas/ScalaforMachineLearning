/**
  * Copyright (c) 2013-2017  Patrick Nicolas - Scala for Machine Learning - All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License") you may not use this file
  * except in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * The source code in this file is provided by the author for the sole purpose of illustrating the
  * concepts and algorithms presented in "Scala for Machine Learning 2nd edition".
  * ISBN: 978-1-783355-874-2 Packt Publishing.
  *
  * Version 0.99.2
  */
package org.scalaml.core

import org.scalaml.Logging
import org.scalatest.{FlatSpec, Matchers}
import scala.util.{Failure, Success, Try}


/**
  * Unit test for the ITransform
  */
final class ITransformTest extends FlatSpec with Matchers with Logging {
  override val name = "Implicit transformation"

  it should s"$name evaluation" in {
    /**
      * A simple data transformation using a model derived from a training set, x
      * @param x input data
      */
    class ExpTransform(x: Vector[Float]) extends ITransform[Float, Vector[Double]] {
      override def |> : PartialFunction[Float, Try[Vector[Double]]] = {
        case y: Float if Math.abs(y) < 100 => Try(x.map(t => Math.exp(t * y)))
      }
    }

    // instantiate the transformation
    val input = Vector[Float](1.6F, 8.0F, -0.4F)
    val expTransform = new ExpTransform(input)

    // Get the new ITransform and compare the values generated by
    // the Vector.map and -iTransform.map
    val expected = input.map(_ * 2.0)
    val doubleValue = (x: Vector[Double]) => x.map(_ * 2.0)
    val found: Try[Vector[Double]] = expTransform.map(doubleValue) |> 2.0F

    found.toOption.isDefined should be (true)
    (found.get)(0) should be(49.1 +- 0.1)

    // Apply the transformation exp(factor * _) to the input vector
    val factor = 2.5F
    expTransform |> factor match {
      case Success(res) =>
        show(s"Vector( ${input.mkString(", ")} ) by exp($factor * _)\n  = Vector( ${res.mkString(", ")} )")
      case Failure(e) => error(e.toString)
    }
  }

  it should s"$name composition" in {

    object _ITransformApp extends App {
      val it1 = new ITransform[Double, Double] {
        def |> : PartialFunction[Double, Try[Double]] = {
          case x: Double if x > 0.0 => Try[Double](x * 2.0)
        }
      }

      val it2 = new ITransform[Double, Int] {
        def |> : PartialFunction[Double, Try[Int]] = {
          case y: Double if y > 1 => Try[Int]((y + 8.0).toInt)
        }
      }

      val value = for {
        n1 <- it1
        n2 <- it2
      } yield { n2 }

      show(s"$name value ${value.|>(3.0)}")

      val combined: Try[Int] = it1.compose(it2).|>(3.0)
      show(s"$name combined $combined")
    }
  }
}
