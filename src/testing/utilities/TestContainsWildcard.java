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
package testing.utilities;

import static org.junit.Assert.*;

import org.junit.Test;

import utilities.Functions;

public class TestContainsWildcard {

    @Test
    public void test() {
        assertEquals(Functions.containsWildcard("'*','!0','n'"), true);
        assertEquals(Functions.containsWildcard("v!x','!0','n'"), false);
    }

    @Test
    public void testHard(){
        assertEquals(Functions.containsWildcard("'/*','!0','n'"), false);
    }

    @Test
    public void testExtremelyHard(){
        assertEquals(Functions.containsWildcard("'*','/*','n'"), true);
    }
}
