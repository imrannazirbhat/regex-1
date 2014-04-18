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

class Regex(str: String) {

  private val compiled = {
    new Parser(str).parsed map {
      case (options, parsed) => new Compiler(options).compile(parsed).andThen(MatchFound)
    }
  }

}

object Regex {

  def apply(str: String): Regex =
    new Regex(str)

}

