package naiveMathIndexer.index;

import java.util.HashSet;

public class MathTags {
	private String tags[] = {	"math",
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
	private HashSet<String> tagsSet; 
	public MathTags(){
		this.tagsSet = new HashSet<String>();
		for (String tag: this.tags){
			tagsSet.add(tag);
		}
	}

	public HashSet getTagsSet(){
		return this.tagsSet;
	}
}
