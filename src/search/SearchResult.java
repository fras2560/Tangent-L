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
package search;

import java.io.BufferedWriter;
import java.io.IOException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import query.MathQuery;

public class SearchResult {
    public TopDocs results;
    public MathQuery mathQuery;
    public Query query;
    public int size;

    public SearchResult(TopDocs results, MathQuery mathQuery, int size, Query query){
        this.results = results;
        this.mathQuery = mathQuery;
        this.size = size;
        this.query = query;
    }

    public int getSize(){
        return this.size;
    }

    public TopDocs getResults(){
        return this.results;
    }

    public MathQuery getMathQuery(){
        return this.mathQuery;
    }

    public Query getQuery(){
        return this.query;
    }

    public void explainResults(IndexSearcher searcher) throws IOException{
        if (this.results != null){
            ScoreDoc[] hits = this.results.scoreDocs;
            for (ScoreDoc hit : hits){
                System.out.println(hit.toString() + ":" + searcher.explain(this.query, hit.doc));
            }
        }else{
            System.out.println("Query had no terms or results");
        }
    }

    public void explainResults(IndexSearcher searcher, BufferedWriter output) throws IOException{
        if (this.results != null){
            ScoreDoc[] hits = this.results.scoreDocs;
            for (ScoreDoc hit : hits){
                output.write(hit.toString() + ":" + searcher.explain(this.query, hit.doc));
                output.newLine();
            }
        }else{
            output.write("Query had no terms or results");
        }
    }

}
