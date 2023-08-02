package org.cache2k.io;

/*-
 * #%L
 * cache2k API
 * %%
 * Copyright (C) 2000 - 2022 headissue GmbH, Munich
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.cache2k.CacheEntry;
import org.cache2k.annotation.NonNull;

/**
 * Relevant information of a load attempt that generated an exception. This is used by the exception
 * propagator and the resilience policy.
 *
 * <p>Compatibility: This interface is not intended for implementation or extension by a client and
 * may get additional methods in a new minor release.
 *
 * <p>Rationale: The information does not contain the time of the original expiry of the cache
 * value. This is intentional. There is no information field to record the time of expiry past the
 * expiry itself. The expiry time information is reset as soon as the expiry time is reached. To
 * produce no overhead, the information provided by this interface is captured only in case an
 * exception happens.
 *
 * @see ExceptionPropagator
 * @see ResiliencePolicy
 * @author Jens Wilke
 * @since 2
 */
public interface LoadExceptionInfo<K, V> extends CacheEntry<K, V> {

  /** The key of the entry. Inherited from {@link CacheEntry} */
  @Override
  K getKey();

  /**
   * Always throws exception based on exception propagator semantics. Inherited from {@link
   * CacheEntry}
   */
  @Override
  V getValue();

  /**
   * The original exception generated by the last recent loader call. Inherited from {@link
   * CacheEntry}
   */
  @Override
  @NonNull
  Throwable getException();

  /** Returns ourselves. Useful because this implements {@link CacheEntry} */
  @Override
  default LoadExceptionInfo<K, V> getExceptionInfo() {
    return this;
  }

  /** Generate the exception to propagate with the exception propagator */
  default RuntimeException generateExceptionToPropagate() {
    return getExceptionPropagator().propagateException(this);
  }

  /** The exception propagator in effect. */
  ExceptionPropagator<K, V> getExceptionPropagator();

  /**
   * Number of retry attempts to load the value for the requested key. The value is starting 0 for
   * the first load attempt that yields an exception. The counter is incremented for each
   * consecutive loader exception. After a successful attempt to load the value is reset.
   *
   * @return counter starting at 0 for the first load attempt that yields an exception.
   */
  int getRetryCount();

  /**
   * Start time of the load that generated the first exception.
   *
   * @return Time in millis since epoch or as defined by {@link
   *     org.cache2k.operation.TimeReference}.
   */
  long getSinceTime();

  /**
   * Start time of the load operation that generated the recent exception.
   *
   * @return Time in millis since epoch or as defined by {@link
   *     org.cache2k.operation.TimeReference}.
   */
  long getLoadTime();

  /**
   * Time in millis until the next retry attempt. This property is only set in the context of the
   * {@link ExceptionPropagator}.
   *
   * @return Time in millis since epoch or as defined by {@link
   *     org.cache2k.operation.TimeReference}.
   */
  long getUntil();
}