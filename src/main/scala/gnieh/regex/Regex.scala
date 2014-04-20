/*
* This file is part of the regex project.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package gnieh.regex

import compiler._
import vm._

import scala.util.Failure

class Regex(val source: String) {

  private val (saved, compiled) = {
    new Parser(source).parsed map {
      case parsed => Compiler.compile(parsed)
    } recoverWith {
      case exn: RegexParserException =>
        Failure(new RuntimeException(s"""${exn.getMessage}
                                        |${source.substring(exn.offset)}
                                        |^""".stripMargin))
    }
  }.get

  private val vm = new VM(compiled, saved)

  /** Tells whether this regular expression is matched by the given input */
  def isMatchedBy(input: String): Boolean =
    vm.exec(input).fold(false) {
      case (start, end, _) =>
        start == 0 && end == input.length - 1
    }

  /** Finds the first match of this regular expression in the input.
   *  If nothing matches, returns `None`*/
  def findFirstIn(input: String): Option[String] =
    vm.exec(input).map { case (start, end, _) => input.substring(start, end - start + 1) }

  /** Finds all matches of this regular expression in the input. */
  def findAllIn(input: String): Iterator[String] = {
    def loop(input: String): Stream[String] =
      vm.exec(input) match {
        case Some((start, end, _)) =>
          input.substring(start, end - start + 1) #:: loop(input.substring(end + 1))
        case None if input.nonEmpty =>
          loop(input.tail)
        case None =>
          Stream.empty
      }
    loop(input).iterator
  }

  def unapplySeq(input: String): Option[List[String]] =
    for {
      (start, end, saved) <- vm.exec(input)
      if start == 0 && end == input.length - 1 && saved.length % 2 == 0
    } yield
      (for(Vector(s, e) <- saved.grouped(2))
        yield if(s == -1 || e == -1)
          null
        else
          input.substring(s, e - s + 1)).toList

  override def toString =
    source

}

object Regex {

  def apply(str: String): Regex =
    new Regex(str)

}

