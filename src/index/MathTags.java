/*
 * Copyright 2017 Dallas Fraser
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package index;

import java.util.HashSet;

/**
 * Holds a collection of math tags.
 *
 * @author Dallas
 */
public class MathTags {
  private final String[] tags = {
    "math",
    "m:maction",
    "m:maligngroup",
    "m:malignmark",
    "m:menclose",
    "m:merror",
    "m:mfenced",
    "m:mfrac",
    "m:mglyph",
    "m:mi",
    "m:mlabeledtr",
    "m:mlongdiv",
    "m:mmultiscripts",
    "m:mn",
    "m:mo",
    "m:mover",
    "m:mpadded",
    "m:mphantom",
    "m:mroot",
    "m:mrow",
    "m:ms",
    "m:mscarries",
    "m:mscarry",
    "m:msgroup",
    "m:mlongdiv",
    "m:msline",
    "m:mspace",
    "m:msqrt",
    "m:msrow",
    "m:mstack",
    "m:mstyle",
    "m:msub",
    "m:msup",
    "m:msubsup",
    "m:mtable",
    "m:mtd",
    "m:mtext",
    "m:mtr",
    "m:munder",
    "m:munderover",
    "m:semantics",
    "m:annotation",
    "m:annotation-xml",
  };
  private final HashSet<String> tagsSet;

  /** Constructor. */
  public MathTags() {
    this.tagsSet = new HashSet<String>();
    for (final String tag : this.tags) {
      this.tagsSet.add(tag);
    }
  }

  /** Returns a hash set for the math tags. */
  public HashSet<String> getTagsSet() {
    return this.tagsSet;
  }
}
