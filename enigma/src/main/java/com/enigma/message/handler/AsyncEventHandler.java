/*
 * Copyright (C) 2015 Mr.Simple <bboyfeiyu@gmail.com>
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

package com.enigma.message.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 事件的异步处理,将事件的处理函数执行在子线程中
 *
 */
public class AsyncEventHandler implements EventHandler {

    /**
     * 事件分发线程
     */
    ExecutorService service = new ThreadPoolExecutor(1,Integer.MAX_VALUE,60, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());


    public AsyncEventHandler() {
     }

    /**
     * 将订阅的函数执行在异步线程中
     *
     */
    public void handleEvent(Runnable runnable) {
        service.execute(runnable);
    }


}
