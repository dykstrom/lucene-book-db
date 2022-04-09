/*
 * Copyright 2022 Johan Dykström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.dykstrom.lucene.client.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;

public final class MoreBindings {

    private MoreBindings() { }

    public static BooleanBinding isNotBlank(final ObservableStringValue op) {
        return Bindings.createBooleanBinding(() -> !op.get().isBlank(), op);
    }

    public static BooleanBinding or(final ObservableBooleanValue first, final ObservableBooleanValue... rest) {
        BooleanBinding result = Bindings.createBooleanBinding(first::getValue, first);
        for (ObservableBooleanValue op : rest) {
            result = Bindings.or(result, op);
        }
        return result;
    }
}