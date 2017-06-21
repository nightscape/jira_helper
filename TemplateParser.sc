val circeVersion = "0.8.0"

import $ivy.`io.circe::circe-core:0.8.0`
import $ivy.`io.circe::circe-generic:0.8.0`
import $ivy.`io.circe::circe-parser:0.8.0`
import $ivy.`io.circe::circe-yaml:0.6.1`
import $ivy.`io.circe::circe-yaml:0.6.1`
import $file.Model, Model._

import cats.syntax.either._
import io.circe._
import io.circe.generic.auto._
import io.circe.yaml

def readTemplateFromUrl(url: String) = {
  val yamlString = scala.io.Source.fromURL(url).mkString
  val content = yaml.parser.parse(yamlString)
  content
    .leftMap(err => err: Error)
    .flatMap(_.as[Issue])
    .valueOr(throw _)
}
