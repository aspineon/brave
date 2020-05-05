/*
 * Copyright 2013-2020 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package brave.propagation.w3c;

import brave.propagation.Propagation.Setter;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Injector;

import static brave.propagation.B3SingleFormat.writeB3SingleFormat;
import static brave.propagation.w3c.TraceparentFormat.writeTraceparentFormat;

final class TraceContextInjector<R, K> implements Injector<R> {
  final TracestateFormat tracestateFormat;
  final Setter<R, K> setter;
  final K traceparentKey, tracestateKey;

  TraceContextInjector(TraceContextPropagation<K> propagation, Setter<R, K> setter) {
    this.tracestateFormat = new TracestateFormat(propagation.tracestateKey);
    this.traceparentKey = propagation.traceparent;
    this.tracestateKey = propagation.tracestate;
    this.setter = setter;
  }

  @Override public void inject(TraceContext traceContext, R request) {

    setter.put(request, traceparentKey, writeTraceparentFormat(traceContext));

    CharSequence otherState = null;
    for (int i = 0, length = traceContext.extra().size(); i < length; i++) {
      Object next = traceContext.extra().get(i);
      if (next instanceof Tracestate) {
        otherState = ((Tracestate) next).otherEntries;
        break;
      }
    }

    String tracestate = tracestateFormat.write(writeB3SingleFormat(traceContext), otherState);
    setter.put(request, tracestateKey, tracestate);
  }
}
