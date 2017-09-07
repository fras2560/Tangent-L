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
package naiveMathIndexer.index;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;


public class TestConvert {
    @Test
    public void test() {
        Path p = Paths.get(System.getProperty("user.dir"), "resources", "temp", "32 (number).mml");
        System.out.println(p.toString());
        ConvertMathML math =  new ConvertMathML(p);
        try {
            Path result = math.convert();
            Path expect = Paths.get(System.getProperty("user.dir"), "resources", "temp", "32 (number)_temp.mml");
            System.out.println("Result: " + result.toString());
            System.out.println("Expect: " + expect.toString());
            assertEquals(result, expect);
            assertEquals(result.toFile().exists(), true);
            assertEquals(result.toFile().delete(), true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.toString());
            System.out.println(e.getMessage());
            fail("Threw IOException");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            System.out.println(e.toString());
            System.out.println(e.getMessage());
            fail("Threw InterruptException");
        }
    }
}
