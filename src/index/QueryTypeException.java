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
/**
 * A Exception thrown when  the Query Type given to the ConvertConfig is incompatible 
 * @author Dallas Fraser
 * @since 2017-11-06 
 */
public class QueryTypeException extends Exception{
    public QueryTypeException(String message){
        super(message);
    }
}
